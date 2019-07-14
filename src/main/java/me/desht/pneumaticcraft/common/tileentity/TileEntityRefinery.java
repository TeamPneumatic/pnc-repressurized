package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerRefinery;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.RefineryRecipe;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TileEntityRefinery extends TileEntityTickableBase
        implements IHeatExchanger, IRedstoneControlled, IComparatorSupport, ISerializableTanks, ISmartFluidSync, INamedContainerProvider {

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

    @GuiSynced
    public int minTemp;

    @DescSynced
    private int refineryCount; // for particle spawning
    @DescSynced
    private int lastProgress; // for particle spawning

    private RefineryRecipe currentRecipe;
    private int workTimer = 0;
    private int comparatorValue;

    private int prevRefineryCount = -1;

    private final RefineryFluidHandler refineryFluidHandler = new RefineryFluidHandler();
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> refineryFluidHandler);

    private boolean searchForRecipe = true;

    public TileEntityRefinery() {
        super(ModTileEntityTypes.REFINERY);
    }

    public static boolean isInputFluidValid(Fluid fluid, int size) {
        return RefineryRecipe.getRecipe(fluid, size).isPresent();
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote && isMaster()) {
            lastProgress = 0;
            List<TileEntityRefinery> refineries = getRefineries();
            refineryCount = refineries.size();
            if (prevRefineryCount != refineryCount) searchForRecipe = true;
            if (searchForRecipe) {
                Optional<RefineryRecipe> recipe = RefineryRecipe.getRecipe(inputTank.getFluid() != null ?
                        inputTank.getFluid().getFluid() : null, refineries.size());
                currentRecipe = recipe.orElse(null);
                minTemp = currentRecipe == null ? 0 : currentRecipe.getMinimumTemp();
                searchForRecipe = false;
            }
            boolean hasWork = false;
            if (currentRecipe != null) {
                if (prevRefineryCount != refineryCount && refineries.size() > 1) {
                    redistributeFluids(refineries, currentRecipe);
                }

                if (refineries.size() > 1 && redstoneAllows() && refine(refineries, true)) {
                    hasWork = true;
                    if (heatExchanger.getTemperature() >= currentRecipe.getMinimumTemp()
                            && inputTank.getFluidAmount() >= currentRecipe.input.amount) {
                        int progress = Math.max(0, ((int) heatExchanger.getTemperature() - (currentRecipe.getMinimumTemp() - 30)) / 30);
                        progress = Math.min(5, progress);
                        heatExchanger.addHeat(-progress);
                        workTimer += progress;
                        while (workTimer >= 20 && inputTank.getFluidAmount() >= currentRecipe.input.amount) {
                            workTimer -= 20;
                            refine(refineries, false);
                            inputTank.drain(currentRecipe.input.amount, true);
                        }
                        lastProgress = progress;
                    }
                } else {
                    workTimer = 0;
                }
            }
            prevRefineryCount = refineryCount;
            updateComparatorValue(refineries, hasWork);
        } else if (getWorld().isRemote && lastProgress > 0) {
            for (int i = 0; i < lastProgress; i++) {
                ClientUtils.emitParticles(getWorld(), getPos().offset(Direction.UP, refineryCount - 1), ParticleTypes.SMOKE);
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
        while (refinery.getCachedNeighbor(Direction.UP) instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getCachedNeighbor(Direction.UP);
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
        while (master.getCachedNeighbor(Direction.DOWN) instanceof TileEntityRefinery) {
            master = (TileEntityRefinery) master.getCachedNeighbor(Direction.DOWN);
        }
        return master;
    }

    private boolean isMaster() {
        return getMasterRefinery() == this;
    }

    @Override
    public boolean redstoneAllows() {
        boolean isPoweredByRedstone = poweredRedstone > 0;

        TileEntityRefinery refinery = this;
        while (refinery.poweredRedstone == 0 && refinery.getCachedNeighbor(Direction.UP) instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery) refinery.getCachedNeighbor(Direction.UP);
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

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
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
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.putByte("redstoneMode", (byte) redstoneMode);

        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        inputAmountScaled = inputTank.getScaledFluidAmount();
        outputAmountScaled = outputTank.getScaledFluidAmount();

        redstoneMode = tag.getByte("redstoneMode");
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return heatExchanger;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
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
            getWorld().updateComparatorOutputLevel(getPos(), getBlockState().getBlock());
        }
    }

    @Override
    public int getComparatorValue() {
        return getMasterRefinery().comparatorValue;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCap.cast();
        } else {
            return super.getCapability(cap, side);
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

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerRefinery(i, playerInventory, getPos());
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
