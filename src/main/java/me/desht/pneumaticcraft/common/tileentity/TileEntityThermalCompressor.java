package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerThermalCompressor;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class TileEntityThermalCompressor extends TileEntityPneumaticBase implements IHeatExchanger, IHeatTinted, IRedstoneControlled, INamedContainerProvider {
    private static final double AIR_GEN_MULTIPLIER = 0.05;  // mL per degree of difference

    private double[] generated = new double[2];

    @GuiSynced
    private final IHeatExchangerLogic[] heatExchangers = new IHeatExchangerLogic[4];
    private final IHeatExchangerLogic connector1;
    private final IHeatExchangerLogic connector2;

    private final IHeatExchangerLogic dummyExchanger;  // never does anything; gets returned from the "null" face

    @DescSynced
    private int[] heatLevel = new int[4];  // S-W-N-E
    @GuiSynced
    private int redstoneMode;

    public TileEntityThermalCompressor() {
        super(ModTileEntities.THERMAL_COMPRESSOR.get(), PneumaticValues.DANGER_PRESSURE_THERMAL_COMPRESSOR, PneumaticValues.MAX_PRESSURE_THERMAL_COMPRESSOR, PneumaticValues.VOLUME_THERMAL_COMPRESSOR, 4);

        for (int i = 0; i < heatExchangers.length; i++) {
            heatExchangers[i] = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
            heatExchangers[i].setThermalCapacity(2);
        }

        connector1 = makeConnector(Direction.NORTH);
        connector2 = makeConnector(Direction.EAST);

        dummyExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    }

    private IHeatExchangerLogic makeConnector(Direction side) {
        IHeatExchangerLogic connector = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
        connector.setThermalResistance(PNCConfig.Common.Machines.thermalCompressorThermalResistance);
        connector.addConnectedExchanger(heatExchangers[side.getHorizontalIndex()]);
        connector.addConnectedExchanger(heatExchangers[side.getOpposite().getHorizontalIndex()]);
        return connector;
    }

    @Override
    protected void initializeIfHeatExchanger() {
        super.initializeIfHeatExchanger();

        for (int i = 0; i < heatExchangers.length; i++) {
            initializeHeatExchanger(heatExchangers[i], Direction.byHorizontalIndex(i));
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            for (IHeatExchangerLogic heatExchanger : heatExchangers) {
                heatExchanger.tick();
            }

            if (redstoneAllows()) {
                connector1.setThermalResistance(PNCConfig.Common.Machines.thermalCompressorThermalResistance);
                connector2.setThermalResistance(PNCConfig.Common.Machines.thermalCompressorThermalResistance);
            } else {
                connector1.setThermalResistance(PNCConfig.Common.Machines.thermalCompressorThermalResistance * 100);
                connector2.setThermalResistance(PNCConfig.Common.Machines.thermalCompressorThermalResistance * 100);
            }

            connector1.tick();
            connector2.tick();

            if (redstoneAllows()) {
                generatePressure(0);  // south and north
                generatePressure(1);  // west and east
            }

            for (int i = 0; i < 4; i++) {
                heatLevel[i] = HeatUtil.getHeatLevelForTemperature(heatExchangers[i].getTemperature());
            }
        }
    }


    private void generatePressure(int side) {
        double diff = Math.abs(heatExchangers[side].getTemperature() - heatExchangers[side + 2].getTemperature());
        generated[side] += diff * AIR_GEN_MULTIPLIER;

        if (generated[side] > 1.0) {
            int toAdd = (int) generated[side];
            addAir(toAdd);
            generated[side] -= toAdd;
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side.getAxis() == Direction.Axis.Y;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        if (side == null)
            return dummyExchanger;
        else
            return side.getAxis() == Direction.Axis.Y ? null : heatExchangers[side.getHorizontalIndex()];
    }

    @Override
    public int getHeatLevelForTintIndex(int tintIndex) {
        if (tintIndex >= 0 && tintIndex <= 3) {
            return heatLevel[tintIndex];
        } else {
            return 10;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        for (int i = 0; i < 4; i++) {
            tag.put("side" + i, heatExchangers[i].serializeNBT());
        }
        tag.putInt("redstoneMode", redstoneMode);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        for (int i = 0; i < 4; i++) {
            heatExchangers[i].deserializeNBT(tag.getCompound("side" + i));
        }
        redstoneMode = tag.getInt("redstoneMode");
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void handleGUIButtonPress(String guiID, PlayerEntity player) {
        if (guiID.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerThermalCompressor(i, playerInventory, getPos());
    }
}
