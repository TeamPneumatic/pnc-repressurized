package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileEntityThermalCompressor extends TileEntityPneumaticBase implements IHeatExchanger, IHeatTinted, IRedstoneControlled {
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
        super(PneumaticValues.DANGER_PRESSURE_THERMAL_COMPRESSOR, PneumaticValues.MAX_PRESSURE_THERMAL_COMPRESSOR, PneumaticValues.VOLUME_THERMAL_COMPRESSOR, 4);

        for (int i = 0; i < heatExchangers.length; i++) {
            heatExchangers[i] = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
            heatExchangers[i].setThermalCapacity(2);
        }

        connector1 = makeConnector(EnumFacing.NORTH);
        connector2 = makeConnector(EnumFacing.EAST);

        dummyExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    }

    private IHeatExchangerLogic makeConnector(EnumFacing side) {
        IHeatExchangerLogic connector = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
        connector.setThermalResistance(ConfigHandler.machineProperties.thermalCompressorThermalResistance);
        connector.addConnectedExchanger(heatExchangers[side.getHorizontalIndex()]);
        connector.addConnectedExchanger(heatExchangers[side.getOpposite().getHorizontalIndex()]);
        return connector;
    }

    @Override
    protected void initializeIfHeatExchanger() {
        super.initializeIfHeatExchanger();

        for (int i = 0; i < heatExchangers.length; i++) {
            initializeHeatExchanger(heatExchangers[i], EnumFacing.byHorizontalIndex(i));
        }
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            for (IHeatExchangerLogic heatExchanger : heatExchangers) {
                heatExchanger.update();
            }

            if (redstoneAllows()) {
                connector1.setThermalResistance(ConfigHandler.machineProperties.thermalCompressorThermalResistance);
                connector2.setThermalResistance(ConfigHandler.machineProperties.thermalCompressorThermalResistance);
            } else {
                connector1.setThermalResistance(ConfigHandler.machineProperties.thermalCompressorThermalResistance * 100);
                connector2.setThermalResistance(ConfigHandler.machineProperties.thermalCompressorThermalResistance * 100);
            }

            connector1.update();
            connector2.update();

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
    public boolean isConnectedTo(EnumFacing side) {
        return side.getAxis() == EnumFacing.Axis.Y;
    }

    @Override
    public String getName() {
        return Blockss.THERMAL_COMPRESSOR.getTranslationKey();
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        if (side == null)
            return dummyExchanger;
        else
            return side.getAxis() == EnumFacing.Axis.Y ? null : heatExchangers[side.getHorizontalIndex()];
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
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 4; i++) {
            NBTTagCompound t1 = new NBTTagCompound();
            heatExchangers[i].writeToNBT(t1);
            tag.setTag("side" + i, t1);
        }
        tag.setInteger("redstoneMode", redstoneMode);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 4; i++) {
            heatExchangers[i].readFromNBT(tag.getCompoundTag("side" + i));
        }
        redstoneMode = tag.getInteger("redstoneMode");
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
        if (guiID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }
}
