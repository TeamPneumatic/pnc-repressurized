package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
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
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TileEntityRefinery extends TileEntityTickableBase
        implements IHeatExchanger, IRedstoneControlled, IComparatorSupport, ISerializableTanks, ISmartFluidSync {

    @GuiSynced
    @DescSynced
    @LazySynced
    private final RefineryInputTank inputTank = new RefineryInputTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    
    @GuiSynced
    @DescSynced
    @LazySynced
    private final SmartSyncTank outputTank = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY, 2);
    
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    
    @SuppressWarnings("unused")
    @DescSynced
    private int inputAmountScaled, outputAmountScaled;
    
    @GuiSynced
    private int redstoneMode;

    @GuiSynced
    private boolean blocked;
    
    private RefineryRecipe currentRecipe;
    private int workTimer = 0;
    private int comparatorValue;

    private int prevRefineryCount = -1;

    private final RefineryFluidHandler refineryFluidHandler = new RefineryFluidHandler();
    private boolean searchForRecipe = true;

    public TileEntityRefinery() {
    }

    public static boolean isInputFluidValid(Fluid fluid, int size) {
        return RefineryRecipe.getRecipe(fluid, size).isPresent();
    }

    @Override
    public String getName() {
        return Blockss.REFINERY.getTranslationKey();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {

            if (isMaster()) {
                List<TileEntityRefinery> refineries = getRefineries();
                if (searchForRecipe) {
                    Optional<RefineryRecipe> recipe = RefineryRecipe.getRecipe(inputTank.getFluid() != null ? inputTank.getFluid().getFluid() : null, refineries.size());
                    currentRecipe = recipe.orElse(null);
                    searchForRecipe = false;
                }
                boolean didWork = false;
                if (currentRecipe != null) {
                    if (prevRefineryCount != refineries.size() && refineries.size() > 1) {
                        redistributeFluids(refineries, currentRecipe);
                        prevRefineryCount = refineries.size();
                    }

                	if (redstoneAllows() && inputTank.getFluidAmount() >= currentRecipe.input.amount) {
	                    if (refineries.size() > 1 && refine(refineries, true)) {
	                        int progress = Math.max(0, ((int) heatExchanger.getTemperature() - 343) / 30);
	                        progress = Math.min(5, progress);
	                        heatExchanger.addHeat(-progress);
	                        workTimer += progress;
	                        while (workTimer >= 20 && inputTank.getFluidAmount() >= currentRecipe.input.amount) {
	                            workTimer -= 20;

	                            refine(refineries, false);
	                            inputTank.drain(currentRecipe.input.amount, true);
	                            for (int i = 0; i < progress; i++)
	                                NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + getWorld().rand.nextDouble(), getPos().getY() + refineries.size(), getPos().getZ() + getWorld().rand.nextDouble(), 0, 0, 0), getWorld());
	
	                        }
	                        didWork = true;
	                    } else {
	                        workTimer = 0;
	                    }
	                }
                }
                updateComparatorValue(refineries, didWork);
            }
        }
    }

    /**
     * Called when the number of refineries in the multiblock changes. Redistribute existing fluids (both input
     * and output) to match the current recipe so the refinery can continue to run.  Of course, it might not be
     * possible to move fluids if there's already something in the new tank, but we'll do our best.
     *
     * @param refineries list of all refineries (master - this one - is the first)
     * @param currentRecipe the current recipe, guaranteed to match the list of refineries
     */
    private void redistributeFluids(List<TileEntityRefinery> refineries, RefineryRecipe currentRecipe) {
        // only the master refinery should have fluid in its input tank
        // scan all non-master refineries, move any fluid from their input tank to the master (this TE), if possible
        for (int i = 1; i < refineries.size(); i++) {
            tryMoveFluid(refineries.get(i).getInputTank(), this.getInputTank());
        }

        FluidTank[] tempTanks = new FluidTank[refineries.size()];
        for (int i = 0; i < refineries.size(); i++) {
            tempTanks[i] = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
        }

        // now scan all refineries and ensure each one has the correct output, according to the current recipe
        for (int i = 0; i < refineries.size(); i++) {
            FluidTank sourceTank = refineries.get(i).getOutputTank();
            FluidStack fluid = sourceTank.getFluid();
            if (fluid != null && !fluid.isFluidEqual(currentRecipe.outputs[i])) {
                // this fluid shouldn't be here; find the appropriate output tank to move it to
                // using an intermediate temporary tank here to allow for possible swapping of fluids
                for (int j = 0; j < currentRecipe.outputs.length; j++) {
                    if (currentRecipe.outputs[j].isFluidEqual(fluid)) {
                        tryMoveFluid(sourceTank, tempTanks[j]);
                        break;
                    }
                }
            }
        }

        // and finally move fluids back to the actual output tanks
        for (int i = 0; i < refineries.size(); i++) {
            tryMoveFluid(tempTanks[i], refineries.get(i).getOutputTank());
        }
    }

    private void tryMoveFluid(FluidTank sourceTank, FluidTank destTank) {
        FluidStack fluid = sourceTank.drain(sourceTank.getCapacity(), false);
        if (fluid != null && fluid.amount > 0) {
            int moved = destTank.fill(fluid, true);
            if (moved > 0) {
                sourceTank.drain(moved, true);
            }
        }
    }

    private List<TileEntityRefinery> getRefineries() {
        List<TileEntityRefinery> refineries = new ArrayList<>();
        refineries.add(this);
        TileEntityRefinery refinery = this;
        while (refinery.getCachedNeighbor(EnumFacing.UP) instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getCachedNeighbor(EnumFacing.UP);
            refineries.add(refinery);
        }
        return refineries;
    }

    private boolean refine(List<TileEntityRefinery> refineries, boolean simulate) {
    	if(currentRecipe == null) {
    		blocked = true;
    		return false;
    	}
    	
        FluidStack[] outputs = currentRecipe.outputs;

        int i = 0;
        for (TileEntityRefinery refinery : refineries) {
        	if (i > outputs.length - 1) {
        		blocked = false;
        		return true;
        	}
        	
            if (outputs[i].amount != refinery.outputTank.fill(outputs[i], !simulate)) {
            	blocked = true;
            	return false;
            }
            
            i++;
        }

        blocked = false;
        return true;
    }

    public TileEntityRefinery getMasterRefinery() {
        TileEntityRefinery master = this;
        while (master.getCachedNeighbor(EnumFacing.DOWN) instanceof TileEntityRefinery) {
            master = (TileEntityRefinery) master.getCachedNeighbor(EnumFacing.DOWN);
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

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }
    
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagCompound tankTag = new NBTTagCompound();
        inputTank.writeToNBT(tankTag);
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
        inputTank.readFromNBT(tag.getCompoundTag("oilTank"));
        inputAmountScaled = inputTank.getScaledFluidAmount();
        outputTank.readFromNBT(tag.getCompoundTag("outputTank"));
        outputAmountScaled = outputTank.getScaledFluidAmount();
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

    private void updateComparatorValue(List<TileEntityRefinery> refineries, boolean didWork) {
        int value;
        if (inputTank.getFluidAmount() < 10 || refineries.size() < 2 || currentRecipe == null || refineries.size() > currentRecipe.outputs.length) {
            value = 0;
        } else {
            value = didWork ? 15 : 0;
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

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("OilTank", inputTank, "OutputTank", outputTank);
    }

    @Override
    public void updateScaledFluidAmount(int tankIndex, int amount) {
        if (tankIndex == 1) {
            inputAmountScaled = amount;
        } else if (tankIndex == 2) {
            outputAmountScaled = amount;
        }
    }

    private class RefineryInputTank extends SmartSyncTank {
        private Fluid prevFluid;

        RefineryInputTank(int capacity) {
            super(TileEntityRefinery.this, capacity, 1);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return getFluid() != null && getFluid().isFluidEqual(fluid)
                    || isInputFluidValid(fluid.getFluid(), 4);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            Fluid newFluid = getFluid() == null ? null : getFluid().getFluid();
            if (prevFluid != newFluid) {
                searchForRecipe = true;
                prevFluid = newFluid;
            }
        }
    }

    private class RefineryFluidHandler implements IFluidHandler {
        @Override
        public IFluidTankProperties[] getTankProperties() {
            return ArrayUtils.addAll(getMasterRefinery().inputTank.getTankProperties(), outputTank.getTankProperties());
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return getMasterRefinery().inputTank.fill(resource, doFill);
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
