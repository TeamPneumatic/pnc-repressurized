/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncRedstoneModuleToClient;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

// since this is both a receiver and an emitter, we won't use either redstone superclass here
public class RedstoneModule extends AbstractTubeModule implements INetworkedModule {
    private EnumRedstoneDirection redstoneDirection = EnumRedstoneDirection.OUTPUT;
    private int inputLevel = -1;
    private int outputLevel;
    private int colorChannel;
    private Operation operation = Operation.PASSTHROUGH;
    private boolean inverted = false;
    private int otherColor = 0;   // for advanced modules
    private int constantVal = 0;  // for advanced modules
    private final byte[] prevLevels = new byte[16];

    // for client-side rendering the redstone connector
    public float extension = 1.0f;
    public float lastExtension;
    private boolean comparatorInput;  // input acts like a vanilla comparator?

    public RedstoneModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public void onNeighborBlockUpdate() {
        if (!comparatorInput) updateInputLevel();
    }

    @Override
    public void onNeighborTileUpdate() {
        if (comparatorInput) updateInputLevel();
    }

    @Override
    public double getWidth() {
        return 9D;
    }

    @Override
    protected double getHeight() {
        return 5D;
    }

    @Override
    public Item getItem() {
        return ModItems.REDSTONE_MODULE.get();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        byte[] levels = new byte[16];

        if (redstoneDirection == EnumRedstoneDirection.OUTPUT) {
            for (AbstractTubeModule module : ModuleNetworkManager.getInstance(getTube().nonNullLevel()).getConnectedModules(this)) {
                if (module instanceof RedstoneModule mr) {
                    if (mr.getRedstoneDirection() == EnumRedstoneDirection.INPUT && mr.getInputLevel() > levels[mr.getColorChannel()])
                        levels[mr.getColorChannel()] = (byte) mr.inputLevel;
                }
                if (module instanceof ThermostatModule mr) {
                    if (mr.getLevel() > levels[mr.getColorChannel()])
                        levels[mr.getColorChannel()] = (byte) mr.getLevel();
                }
            }

            int out = computeOutputSignal(outputLevel, levels);
            if (inverted) out = out > 0 ? 0 : 15;
            if (setOutputLevel(out)) {
                NetworkHandler.sendToAllTracking(PacketSyncRedstoneModuleToClient.create(this), getTube());
            }
        } else {
            if (inputLevel < 0) updateInputLevel();  // first update
        }
        System.arraycopy(levels, 0, prevLevels, 0, 16);
    }

    @Override
    public void tickClient() {
        super.tickClient();

        lastExtension = extension;
        if (redstoneDirection == EnumRedstoneDirection.OUTPUT) {
            extension = Math.min(1.0f, extension + 0.125f);
        } else {
            extension = Math.max(0.0f, extension - 0.125f);
        }
    }

    private int computeOutputSignal(int lastOutput, byte[] levels) {
        byte s1 = levels[getColorChannel()];
        byte s1prev = prevLevels[getColorChannel()];
        byte s2 = levels[otherColor];

        return operation.signalFunction.compute(lastOutput, s1, s2, getTube().nonNullLevel().getGameTime(), constantVal, s1 > s1prev);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);

        tag.putBoolean("input", redstoneDirection == EnumRedstoneDirection.INPUT);
        tag.putByte("channel", (byte) colorChannel);
        tag.putByte("outputLevel", (byte) outputLevel);
        tag.putString("op", operation.toString());
        tag.putByte("color2", (byte) otherColor);
        tag.putInt("const", (byte) constantVal);
        tag.putBoolean("invert", inverted);
        tag.putLong("prevLevels", encodeLevels(prevLevels));
        tag.putBoolean("comparatorInput", comparatorInput);

        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);

        redstoneDirection = tag.getBoolean("input") ? EnumRedstoneDirection.INPUT : EnumRedstoneDirection.OUTPUT;
        colorChannel = tag.getByte("channel");
        outputLevel = tag.getByte("outputLevel"); // for sync'ing to clients on login
        try {
            operation = tag.contains("op") ? Operation.valueOf(tag.getString("op")) : Operation.PASSTHROUGH;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            operation = Operation.PASSTHROUGH;
        }
        otherColor = tag.getByte("color2");
        constantVal = tag.getInt("const");
        inverted = tag.getBoolean("invert");
        decodeLevels(tag.getLong("prevLevels"), prevLevels);
        comparatorInput = tag.getBoolean("comparatorInput");
    }

    private long encodeLevels(byte[] l) {
        return IntStream.range(0, l.length)
                .mapToLong(i -> (long) l[i] << 4 * i)
                .reduce(0, (a, b) -> a | b);
    }

    private void decodeLevels(long l, byte[] res) {
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) (l >> (4 * i) | 0xf);
        }
    }

    public EnumRedstoneDirection getRedstoneDirection() {
        return redstoneDirection;
    }

    public void setRedstoneDirection(EnumRedstoneDirection redstoneDirection) {
        this.redstoneDirection = redstoneDirection;
        setChanged();
    }

    @Override
    public int getRedstoneLevel() {
        return redstoneDirection == EnumRedstoneDirection.OUTPUT ? outputLevel : 0;
    }

    public boolean setOutputLevel(int level) {
        level = Mth.clamp(level, 0, 15);
        if (level != outputLevel) {
            outputLevel = level;
            updateNeighbors();
            return true;
        } else {
            return false;
        }
    }

    public int getInputLevel() {
        return inputLevel;
    }

    public void setInputLevel(int level) {
        // used by clientside sync and also to invalidate the cached signal level (e.g. see pressure gauge)
        inputLevel = level;
    }

    @Override
    public int getColorChannel() {
        return colorChannel;
    }

    @Override
    public void setColorChannel(int channel) {
        this.colorChannel = channel;
        setChanged();
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        setChanged();
    }

    public boolean isComparatorInput() {
        return comparatorInput;
    }

    public void setComparatorInput(boolean comparatorInput) {
        this.comparatorInput = comparatorInput;
        setChanged();
    }

    @Override
    public void addInfo(List<Component> curInfo) {
        super.addInfo(curInfo);
        if (getRedstoneDirection() == EnumRedstoneDirection.INPUT) {
            curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.redstoneModule.receiving", inputLevel));
        } else {
            curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.redstoneModule.emitting", outputLevel));
            if (upgraded) addAdvancedInfo(curInfo);
        }
    }

    private void addAdvancedInfo(List<Component> curInfo) {
        MutableComponent s = Component.translatable("pneumaticcraft.waila.redstoneModule.op", PneumaticCraftUtils.xlate(operation.getTranslationKey()));
        if (operation.useOtherColor) {
            s = s.append(" (").append(PneumaticCraftUtils.dyeColorDesc(otherColor)).append(")");
        }
        if (operation.useConst) {
            s = s.append(" (" + constantVal + ")");
        }
        curInfo.add(s);
        if (inverted) curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.redstoneModule.inverted"));
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        DyeColor dyeColor = DyeColor.getColor(heldStack);
        if (dyeColor != null) {
            int colorId = dyeColor.getId();
            setColorChannel(colorId);
            if (ConfigHelper.common().general.useUpDyesWhenColoring.get() && !player.isCreative()) {
                heldStack.shrink(1);
            }
            return true;
        } else if (ModdedWrenchUtils.getInstance().isWrench(heldStack)) {
            redstoneDirection = redstoneDirection == EnumRedstoneDirection.INPUT ? EnumRedstoneDirection.OUTPUT : EnumRedstoneDirection.INPUT;
            updateNeighbors();
            if (!updateInputLevel()) {
                NetworkHandler.sendToAllTracking(PacketSyncRedstoneModuleToClient.create(this), getTube());
            }
            return true;
        } else {
            return super.onActivated(player, hand);
        }
    }

    private boolean updateInputLevel() {
        int newInputLevel = redstoneDirection == EnumRedstoneDirection.INPUT ? readInputLevel() : 0;

        newInputLevel = Math.max(newInputLevel,  pressureTube.tubeModules()
                .filter(tm -> tm instanceof AbstractRedstoneEmittingModule)
                .max(Comparator.comparingInt(AbstractTubeModule::getRedstoneLevel))
                .map(AbstractTubeModule::getRedstoneLevel)
                .orElse(0));

        if (newInputLevel != inputLevel) {
            inputLevel = newInputLevel;
            NetworkHandler.sendToAllTracking(PacketSyncRedstoneModuleToClient.create(this), getTube());
            return true;
        }
        return false;
    }

    private int readInputLevel() {
        Level world = Objects.requireNonNull(pressureTube.getLevel());
        if (comparatorInput && upgraded) {
            BlockPos pos2 = pressureTube.getBlockPos().relative(getDirection());
            BlockState state = world.getBlockState(pos2);
            return state.hasAnalogOutputSignal() ? state.getAnalogOutputSignal(world, pos2) : 0;
        } else {
            return world.getSignal(pressureTube.getBlockPos().relative(getDirection()), getDirection());
        }
    }

    public Operation getOperation() {
        return operation;
    }

    public int getOtherColor() {
        return otherColor;
    }

    public int getConstantVal() {
        return constantVal;
    }

    public void setOperation(Operation operation, int otherColor, int constantVal) {
        // 4 tick interval is smallest sensible value
        if (operation == Operation.CLOCK) constantVal = Math.max(4, constantVal);

        this.operation = operation;
        this.otherColor = otherColor;
        this.constantVal = constantVal;

        setChanged();
    }

    public enum EnumRedstoneDirection implements ITranslatableEnum {
        INPUT, OUTPUT;

        public EnumRedstoneDirection toggle() {
            return this == INPUT ? OUTPUT : INPUT;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.redstoneModule." + toString().toLowerCase(Locale.ROOT);
        }

        public boolean isInput() {
            return this == INPUT;
        }
    }

    public enum Operation implements ITranslatableEnum {
        PASSTHROUGH(false, false, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1),
        AND(true, false, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1 > 0 && s2 > 0 ? 15 : 0),
        OR(true, false, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1 > 0 || s2 > 0 ? 15 : 0),
        XOR(true, false, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1 == 0 && s2 == 0 || s1 > 0 && s2 > 0 ? 0 : 15),
        CLOCK(false, true, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1 == 0 && timer % constant < 2 ? 15 : 0, 4, Integer.MAX_VALUE),
        COMPARATOR(true, false, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1 > s2 ? 15 : 0),
        SUBTRACT(true, false, (lastOutput, s1, s2, timer, constant, s1rising) ->
                Mth.clamp(s1 - s2, 0, 15)),
        COMPARE(false, true, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1 > constant ? 15 : 0, 0, 15),
        TOGGLE(false, false, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1rising ? (lastOutput > 0 ? 0 : 15) : lastOutput),
        CONSTANT(false, true, (lastOutput, s1, s2, timer, constant, s1rising) ->
                constant, 0, 15),
        COUNTER(false, true, (lastOutput, s1, s2, timer, constant, s1rising) ->
                s1rising ? (lastOutput + 1 > Math.min(15, constant) ? 0 : lastOutput + 1) : lastOutput, 0, 15);

        private final boolean useOtherColor;
        private final boolean useConst;
        private final int constMin;
        private final int constMax;
        private final SignalFunction signalFunction;

        Operation(boolean useOtherColor, boolean useConst, SignalFunction signalFunction) {
            this(useOtherColor, useConst, signalFunction, 0, 0);
        }

        Operation(boolean useOtherColor, boolean useConst, SignalFunction signalFunction, int constMin, int constMax) {
            this.useOtherColor = useOtherColor;
            this.useConst = useConst;
            this.signalFunction = signalFunction;
            this.constMin = constMin;
            this.constMax = constMax;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.redstoneModule.operation_" + this.toString().toLowerCase(Locale.ROOT);
        }

        public boolean useOtherColor() {
            return useOtherColor;
        }

        public boolean useConstant() {
            return useConst;
        }

        public int getConstMin() {
            return constMin;
        }

        public int getConstMax() {
            return constMax;
        }
    }

    @FunctionalInterface
    private interface SignalFunction {
        /**
         * Compute the output signal for this function
         *
         * @param lastOutput output signal from the last computation
         * @param s1 input signal 1 (0..15)
         * @param s2 input signal 2 (0..15)
         * @param timer the world time
         * @param constant the constant value
         * @param s1rising true if input 1 is higher than on the last computation
         * @return the desired output signal (0..15 - will also be lastOutput the next time this is called)
         */
        int compute(int lastOutput, byte s1, byte s2, long timer, int constant, boolean s1rising);
    }
}
