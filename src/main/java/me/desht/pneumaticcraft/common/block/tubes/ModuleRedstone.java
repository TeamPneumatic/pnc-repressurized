package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketOpenTubeModuleGui;
import me.desht.pneumaticcraft.common.network.PacketSyncRedstoneModuleToClient;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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

    public ModuleRedstone(ItemTubeModule itemTubeModule) {
        super(itemTubeModule);
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public void onNeighborBlockUpdate() {
        updateInputLevel();
    }

    @Override
    public double getWidth() {
        return 8D;
    }

    @Override
    protected double getHeight() {
        return 4D;
    }

    @Override
    public void update() {
        super.update();

        if (!pressureTube.getWorld().isRemote) {
            byte[] levels = new byte[16];

            if (redstoneDirection == EnumRedstoneDirection.OUTPUT) {
                for (TubeModule module : ModuleNetworkManager.getInstance(getTube().getWorld()).getConnectedModules(this)) {
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
        byte s2 = levels[otherColor];

        switch (operation) {
            case PASSTHROUGH:
                return s1;
            case AND:
                return s1 > 0 && s2 > 0 ? 15 : 0;
            case OR:
                return s1 > 0 || s2 > 0 ? 15 : 0;
            case XOR:
                return s1 == 0 && s2 == 0 || s1 > 0 && s2 > 0 ? 0 : 15;
            case COMPARATOR:
                return s1 > s2 ? 15 : 0;
            case SUBTRACT:
                return MathHelper.clamp(s1 - s2, 0, 15);
            case COMPARE:
                return s1 > constantVal ? 15 : 0;
            case CLOCK:
                return s1 == 0 && getTube().getWorld().getGameTime() % constantVal < 2 ? 15 : 0;
            case TOGGLE:
                if (s1 > prevLevels[getColorChannel()]) {
                    return lastOutput > 0 ? 0 : 15;
                } else {
                    return lastOutput;
                }
            case CONSTANT:
                return MathHelper.clamp(constantVal, 0, 15);
            case COUNTER:
                if (s1 > prevLevels[getColorChannel()]) {
                    lastOutput++;
                    return lastOutput > Math.min(15, constantVal) ? 0 : lastOutput;
                } else {
                    return lastOutput;
                }
            default:
                return 0;
        }
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);

        tag.putBoolean("input", redstoneDirection == EnumRedstoneDirection.INPUT);
        tag.putByte("channel", (byte) colorChannel);
        tag.putByte("outputLevel", (byte) outputLevel);
        tag.putString("op", operation.toString());
        tag.putByte("color2", (byte) otherColor);
        tag.putByte("const", (byte) constantVal);
        tag.putBoolean("invert", inverted);
        tag.putLong("prevLevels", encodeLevels(prevLevels));
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
        constantVal = tag.getByte("const");
        inverted = tag.getBoolean("invert");
        decodeLevels(tag.getLong("prevLevels"), prevLevels);
    }

    private long encodeLevels(byte[] l) {
        return IntStream.range(0, l.length)
                .mapToLong(i -> l[i] << 4 * i)
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
            s = s.appendString(" (").append(PneumaticCraftUtils.xlate(PneumaticCraftUtils.dyeColorDesc(otherColor)).appendString(")"));
        }
        if (operation.useConst) {
            s = s.appendString(" (" + constantVal + ")");
        }
        curInfo.add(s);
        if (inverted) curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.redstoneModule.inverted"));
    }

    @Override
    public boolean onActivated(PlayerEntity player, Hand hand) {
        ItemStack heldStack = player.getHeldItem(hand);
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
        } else if (!getTube().getWorld().isRemote) {
            NetworkHandler.sendToPlayer(new PacketOpenTubeModuleGui(getType(), pressureTube.getPos()), (ServerPlayerEntity) player);
            return true;
        }
        return false;
    }

    private boolean updateInputLevel() {
        int newInputLevel = redstoneDirection == EnumRedstoneDirection.INPUT ?
                pressureTube.getWorld().getRedstonePower(pressureTube.getPos().offset(getDirection()), getDirection()) : 0;

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
        PASSTHROUGH(false, false),
        AND(true, false),
        OR(true, false),
        XOR(true, false),
        CLOCK(false, true),
        COMPARATOR(true, false),
        SUBTRACT(true, false),
        COMPARE(false, true),
        TOGGLE(false, false),
        CONSTANT(false, true),
        COUNTER(false, true);

        private final boolean useOtherColor;
        private final boolean useConst;

        Operation(boolean useOtherColor, boolean useConst) {
            this.useOtherColor = useOtherColor;
            this.useConst = useConst;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.redstoneModule.operation_" + this.toString().toLowerCase(Locale.ROOT);
        }

        public boolean useOtherColor() {
            return useOtherColor;
        }

        public boolean useConst() {
            return useConst;
        }
    }
}
