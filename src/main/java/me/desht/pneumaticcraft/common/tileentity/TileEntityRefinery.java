package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityRefinery extends TileEntityBase implements IHeatExchanger, IRedstoneControlled, IComparatorSupport {

    @GuiSynced
    @DescSynced
    @LazySynced
    private final OilTank oilTank = new OilTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    @DescSynced
    @LazySynced
    private final FluidTank outputTank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @DescSynced
    private int oilTankAmount, outputTankAmount;//amount divided by 100 to decrease network load.
    @GuiSynced
    private int redstoneMode;
    private int workTimer = 0;
    private int comparatorValue;

    private final RefineryFluidHandler refineryFluidHandler = new RefineryFluidHandler();

    /**
     * The amounts of LPG, Gasoline, Kerosine and Diesel produced per 10mL Oil, depending on how many refineries are stacked on top of eachother.
     * Type \ Refineries | 2 | 3 | 4
     * ------------------------------
     * LPG               | 2 | 2 | 2
     * Gasoline          | - | - | 3
     * Kerosene          | - | 3 | 3
     * Diesel            | 4 | 2 | 2
     */
    public static final int[][] REFINING_TABLE = new int[][]{{4, 0, 0, 2}, {2, 3, 0, 2}, {2, 3, 3, 2}};
    private final Fluid[] refiningFluids = getRefiningFluids();

    public TileEntityRefinery() {

    }

    public static Fluid[] getRefiningFluids() {
        return new Fluid[]{Fluids.DIESEL, Fluids.KEROSENE, Fluids.GASOLINE, Fluids.LPG};
    }

    @Override
    public String getName() {
        return Blockss.REFINERY.getUnlocalizedName();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            oilTankAmount = oilTank.getFluidAmount() / 100;
            outputTankAmount = outputTank.getFluidAmount() / 100;

            if (isMaster()) {
                List<TileEntityRefinery> refineries = getRefineries();
                if (redstoneAllows() && oilTank.getFluidAmount() >= 10) {
                    if (refineries.size() > 1 && refineries.size() <= refiningFluids.length && refine(refineries, true)) {
                        int progress = Math.max(0, ((int) heatExchanger.getTemperature() - 343) / 30);
                        progress = Math.min(5, progress);
                        heatExchanger.addHeat(-progress);
                        workTimer += progress;
                        while (workTimer >= 20 && oilTank.getFluidAmount() >= 10) {
                            workTimer -= 20;

                            refine(refineries, false);
                            oilTank.drain(10, true);
                            for (int i = 0; i < 5; i++)
                                NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + getWorld().rand.nextDouble(), getPos().getY() + refineries.size(), getPos().getZ() + getWorld().rand.nextDouble(), 0, 0, 0), getWorld());

                        }
                    } else {
                        workTimer = 0;
                    }
                }
                updateComparatorValue(refineries);
            }
        }
    }

    private List<TileEntityRefinery> getRefineries() {
        List<TileEntityRefinery> refineries = new ArrayList<>();
        refineries.add(this);
        TileEntityRefinery refinery = this;
        while (refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity();
            refineries.add(refinery);
        }
        return refineries;
    }

    public boolean refine(List<TileEntityRefinery> refineries, boolean simulate) {
        int[] outputTable = REFINING_TABLE[refineries.size() - 2];

        int i = 0;
        for (TileEntityRefinery refinery : refineries) {
            while (outputTable[i] == 0)
                i++;
            if (outputTable[i] != refinery.outputTank.fill(new FluidStack(refiningFluids[i], outputTable[i]), !simulate))
                return false;
            i++;
        }
        return true;
    }

    public TileEntityRefinery getMasterRefinery() {
        TileEntityRefinery master = this;
        while (master.getTileCache()[EnumFacing.DOWN.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            master = (TileEntityRefinery) master.getTileCache()[EnumFacing.DOWN.ordinal()].getTileEntity();
        }
        return master;
    }

    private boolean isMaster() {
        return getMasterRefinery() == this;
    }

    @Override
    public boolean redstoneAllows() {
        if (getWorld().isRemote) onNeighborBlockUpdate();
        boolean isPoweredByRedstone = poweredRedstone > 0;

        TileEntityRefinery refinery = this;
        while (poweredRedstone == 0 && refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getTileCache()[EnumFacing.UP.ordinal()].getTileEntity();
            refinery.onNeighborBlockUpdate();
            isPoweredByRedstone = refinery.poweredRedstone > 0;
        }

        switch (getRedstoneMode()) {
            case 0:
                return true;
            case 1:
                return isPoweredByRedstone;
            case 2:
                return !isPoweredByRedstone;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getOilTank() {
        return oilTank;
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getOutputTank() {
        return outputTank;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagCompound tankTag = new NBTTagCompound();
        oilTank.writeToNBT(tankTag);
        tag.setTag("oilTank", tankTag);

        tankTag = new NBTTagCompound();
        outputTank.writeToNBT(tankTag);
        tag.setTag("outputTank", tankTag);

        tag.setByte("redstoneMode", (byte) redstoneMode);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        oilTank.readFromNBT(tag.getCompoundTag("oilTank"));
        outputTank.readFromNBT(tag.getCompoundTag("outputTank"));
        redstoneMode = tag.getByte("redstoneMode");
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return heatExchanger;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    private void updateComparatorValue(List<TileEntityRefinery> refineries) {
        int value;
        if (oilTank.getFluidAmount() < 10 || refineries.size() < 2 || refineries.size() > refiningFluids.length) {
            value = 0;
        } else {
            value = refine(refineries, true) ? 15 : 0;
        }
        if (value != comparatorValue) {
            comparatorValue = value;
            updateNeighbours();
        }
    }

    @Override
    public int getComparatorValue() {
        return getMasterRefinery().comparatorValue;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(refineryFluidHandler);
        } else {
            return super.getCapability(capability, facing);
        }
    }


    private static class OilTank extends FluidTank {
        OilTank(int capacity) {
            super(capacity);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return Fluids.areFluidsEqual(fluid.getFluid(), Fluids.OIL);
        }
    }

    private class RefineryFluidHandler implements IFluidHandler {
        @Override
        public IFluidTankProperties[] getTankProperties() {
            return ArrayUtils.addAll(getMasterRefinery().oilTank.getTankProperties(), outputTank.getTankProperties());
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (isMaster()) {
                return oilTank.fill(resource, doFill);
            } else {
                return getMasterRefinery().oilTank.fill(resource, doFill);
            }
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return outputTank.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return outputTank.drain(maxDrain, doDrain);
        }
    }
}
