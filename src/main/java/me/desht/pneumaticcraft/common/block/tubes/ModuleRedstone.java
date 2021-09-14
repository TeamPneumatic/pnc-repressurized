package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncRedstoneModuleToClient;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

// since this is both a receiver and an emitter, we won't use either redstone superclass here
public class ModuleRedstone extends TubeModule implements INetworkedModule {
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

    public ModuleRedstone(ItemTubeModule itemTubeModule) {
        super(itemTubeModule);
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
    public void update() {
        super.update();

        if (!getTube().getLevel().isClientSide) {
            byte[] levels = new byte[16];

            if (redstoneDirection == EnumRedstoneDirection.OUTPUT) {
                for (TubeModule module : ModuleNetworkManager.getInstance(getTube().getLevel()).getConnectedModules(this)) {
                    if (module instanceof ModuleRedstone) {
                        ModuleRedstone mr = (ModuleRedstone) module;
                        if (mr.getRedstoneDirection() == EnumRedstoneDirection.INPUT && mr.getInputLevel() > levels[mr.getColorChannel()])
                            levels[mr.getColorChannel()] = (byte) mr.inputLevel;
                    }
                }

                int out = computeOutputSignal(outputLevel, levels);
                if (inverted) out = out > 0 ? 0 : 15;
                if (setOutputLevel(out)) {
                    NetworkHandler.sendToAllTracking(new PacketSyncRedstoneModuleToClient(this), getTube());
                }
            } else {
                if (inputLevel < 0) updateInputLevel();  // first update
            }
            System.arraycopy(levels, 0, prevLevels, 0, 16);
        } else {
            lastExtension = extension;
            if (redstoneDirection == EnumRedstoneDirection.OUTPUT) {
                extension = Math.min(1.0f, extension + 0.125f);
            } else {
                extension = Math.max(0.0f, extension - 0.125f);
            }
        }
    }

    private int computeOutputSignal(int lastOutput, byte[] levels) {
        byte s1 = levels[getColorChannel()];
        byte s1prev = prevLevels[getColorChannel()];
        byte s2 = levels[otherColor];

        return operation.signalFunction.compute(lastOutput, s1, s2, getTube().getLevel().getGameTime(), constantVal, s1 > s1prev);
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag) {
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
    public void readFromNBT(CompoundNBT tag) {
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
        if (tag.contains("const", Constants.NBT.TAG_BYTE)) {
            // TODO: remove legacy code in 1.17
            constantVal = tag.getByte("const");
        } else {
            constantVal = tag.getInt("const");
        }
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
    }

    @Override
    public int getRedstoneLevel() {
        return redstoneDirection == EnumRedstoneDirection.OUTPUT ? outputLevel : 0;
    }

    public boolean setOutputLevel(int level) {
        level = MathHelper.clamp(level, 0, 15);
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
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean isComparatorInput() {
        return comparatorInput;
    }

    public void setComparatorInput(boolean comparatorInput) {
        this.comparatorInput = comparatorInput;
    }

    @Override
    public void addInfo(List<ITextComponent> curInfo) {
        super.addInfo(curInfo);
        if (getRedstoneDirection() == EnumRedstoneDirection.INPUT) {
            curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.redstoneModule.receiving", inputLevel));
        } else {
            curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.redstoneModule.emitting", outputLevel));
            if (upgraded) addAdvancedInfo(curInfo);
        }
    }

    private void addAdvancedInfo(List<ITextComponent> curInfo) {
        IFormattableTextComponent s = new TranslationTextComponent("pneumaticcraft.waila.redstoneModule.op", PneumaticCraftUtils.xlate(operation.getTranslationKey()));
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
    public boolean onActivated(PlayerEntity player, Hand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        DyeColor dyeColor = DyeColor.getColor(heldStack);
        if (dyeColor != null) {
            int colorId = dyeColor.getId();
            setColorChannel(colorId);
            if (PNCConfig.Common.General.useUpDyesWhenColoring && !player.isCreative()) {
                heldStack.shrink(1);
            }
            return true;
        } else if (ModdedWrenchUtils.getInstance().isWrench(heldStack)) {
            redstoneDirection = redstoneDirection == EnumRedstoneDirection.INPUT ? EnumRedstoneDirection.OUTPUT : EnumRedstoneDirection.INPUT;
            updateNeighbors();
            if (!updateInputLevel()) {
                NetworkHandler.sendToAllTracking(new PacketSyncRedstoneModuleToClient(this), getTube());
            }
            return true;
        } else {
            return super.onActivated(player, hand);
        }
    }

    private boolean updateInputLevel() {
        int newInputLevel = redstoneDirection == EnumRedstoneDirection.INPUT ? readInputLevel() : 0;

        newInputLevel = Math.max(newInputLevel,  pressureTube.tubeModules()
                .filter(tm -> tm instanceof TubeModuleRedstoneEmitting)
                .max(Comparator.comparingInt(TubeModule::getRedstoneLevel))
                .map(TubeModule::getRedstoneLevel)
                .orElse(0));

        if (newInputLevel != inputLevel) {
            inputLevel = newInputLevel;
            NetworkHandler.sendToAllTracking(new PacketSyncRedstoneModuleToClient(this), getTube());
            return true;
        }
        return false;
    }

    private int readInputLevel() {
        World world = Objects.requireNonNull(pressureTube.getLevel());
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
                MathHelper.clamp(s1 - s2, 0, 15)),
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
