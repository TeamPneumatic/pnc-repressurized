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

package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerRefinery;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class TileEntityRefineryController extends TileEntityTickableBase
        implements IRedstoneControl<TileEntityRefineryController>, IComparatorSupport, ISerializableTanks,
        MenuProvider, IHeatExchangingTE
{
    @GuiSynced
    @DescSynced
    private final RefineryInputTank inputTank = new RefineryInputTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> inputTank);
    @GuiSynced
    public final SmartSyncTank[] outputsSynced = new SmartSyncTank[RefineryRecipe.MAX_OUTPUTS];  // purely for GUI syncing
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> heatCap = LazyOptional.of(() -> heatExchanger);
    @GuiSynced
    private final RedstoneController<TileEntityRefineryController> rsController = new RedstoneController<>(this);
    @GuiSynced
    private boolean blocked;
    @GuiSynced
    public int minTemp;
    @GuiSynced
    public int maxTemp;
    @GuiSynced
    private String currentRecipeIdSynced = "";
    @DescSynced
    private int outputCount;
    @DescSynced
    private int lastProgress; // indicates to client that refinery is running, for particle spawning

    private List<LazyOptional<IFluidHandler>> outputCache;
    private TemperatureRange operatingTemp = TemperatureRange.invalid();
    private RefineryRecipe currentRecipe;
    private int workTimer = 0;
    private int comparatorValue;
    private int prevOutputCount = -1;
    private boolean searchForRecipe = true;

    public TileEntityRefineryController(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFINERY.get(), pos, state);

        for (int i = 0; i < RefineryRecipe.MAX_OUTPUTS; i++) {
            outputsSynced[i] = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY);
        }
    }

    public static boolean isInputFluidValid(Level world, Fluid fluid, int size) {
        RefineryRecipe recipe =  PneumaticCraftRecipeType.REFINERY
                .findFirst(world, r -> r.getOutputs().size() <= size && FluidUtils.matchFluid(r.getInput(), fluid, true));
        return recipe != null;
    }

    private RefineryRecipe getRecipeFor(FluidStack fluid) {
        return PneumaticCraftRecipeType.REFINERY.stream(level)
                .filter(r -> r.getOutputs().size() <= outputCount)
                .filter(r -> FluidUtils.matchFluid(r.getInput(), fluid, true))
                .max(Comparator.comparingInt(r2 -> r2.getOutputs().size()))
                .orElse(null);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        inputTank.tick();
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (lastProgress > 0) {
            TileEntityRefineryOutput teRO = findAdjacentOutput();
            if (teRO != null) {
                for (int i = 0; i < lastProgress; i++) {
                    ClientUtils.emitParticles(getLevel(), teRO.getBlockPos().relative(Direction.UP, outputCount - 1), ParticleTypes.SMOKE);
                }
            }
        }
        for (SmartSyncTank smartSyncTank : outputsSynced) {
            smartSyncTank.tick();
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        lastProgress = 0;
        if (outputCache == null) cacheRefineryOutputs();
        outputCount = outputCache.size();
        if (prevOutputCount != outputCount) {
            searchForRecipe = true;
        }
        if (searchForRecipe) {
            currentRecipe = getRecipeFor(inputTank.getFluid());
            currentRecipeIdSynced = currentRecipe == null ? "" : currentRecipe.getId().toString();
            operatingTemp = currentRecipe == null ? TemperatureRange.invalid() : currentRecipe.getOperatingTemp();
            minTemp = operatingTemp.getMin();
            maxTemp = operatingTemp.getMax();
            searchForRecipe = false;
        }
        boolean hasWork = false;
        if (currentRecipe != null) {
            if (prevOutputCount != outputCount && outputCount > 1) {
                redistributeFluids();
            }

            if (outputCount > 1 && doesRedstoneAllow() && doRefiningStep(FluidAction.SIMULATE)) {
                hasWork = true;
                if (operatingTemp.inRange(heatExchanger.getTemperature())
                        && inputTank.getFluidAmount() >= currentRecipe.getInput().getAmount()) {
                    // TODO support for cryo-refining (faster as it gets colder, adds heat instead of removing)
                    int progress = Math.max(0, ((int) heatExchanger.getTemperature() - (operatingTemp.getMin() - 30)) / 30);
                    progress = Math.min(5, progress);
                    heatExchanger.addHeat(-progress);
                    workTimer += progress;
                    while (workTimer >= 20 && inputTank.getFluidAmount() >= currentRecipe.getInput().getAmount()) {
                        workTimer -= 20;
                        doRefiningStep(FluidAction.EXECUTE);
                        inputTank.drain(currentRecipe.getInput().getAmount(), FluidAction.EXECUTE);
                    }
                    lastProgress = progress;
                }
            } else {
                workTimer = 0;
            }
        }

        IntStream.range(0, outputCount).forEach(i -> outputCache.get(i).ifPresent(h -> {
            outputsSynced[i].setFluid(h.getFluidInTank(0).copy());
            outputsSynced[i].tick();
        }));

        prevOutputCount = outputCount;
        maybeUpdateComparatorValue(outputCount, hasWork);
    }

    /**
     * Called when the number of refinery outputs in the multiblock changes. Redistribute existing fluids to match the
     * current recipe so the refinery can continue to run.  Of course, it might not be possible to move fluids if
     * there's already something in the new tank, but we'll do our best.
     *
     * When this is called, there will be a valid recipe and at least two outputs present.
     */
    private void redistributeFluids() {
        FluidTank[] tempTanks = new FluidTank[outputCount];
        for (int i = 0; i < outputCount; i++) {
            tempTanks[i] = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
        }

        // now scan all refineries and ensure each one has the correct output, according to the current recipe
        for (int i = 0; i < outputCount; i++) {
            final FluidStack wantedFluid = currentRecipe.getOutputs().get(i);
            outputCache.get(i).ifPresent(outputHandler -> {
                FluidStack fluid = outputHandler.getFluidInTank(0);
                if (!fluid.isFluidEqual(wantedFluid)) {
                    // this fluid shouldn't be here; find the appropriate output tank to move it to,
                    // using an intermediate temporary tank to allow for possible swapping of fluids
                    for (int j = 0; j < currentRecipe.getOutputs().size(); j++) {
                        if (currentRecipe.getOutputs().get(j).isFluidEqual(fluid)) {
                            tryMoveFluid(outputHandler, tempTanks[j]);
                            break;
                        }
                    }
                }
            });
        }

        // and finally move fluids back to the actual output tanks
        for (int i = 0; i < outputCount; i++) {
            final IFluidHandler tempTank = tempTanks[i];
            outputCache.get(i).ifPresent(outputHandler -> tryMoveFluid(tempTank, outputHandler));
        }
    }

    private void tryMoveFluid(IFluidHandler sourceHandler, IFluidHandler destHandler) {
        FluidStack fluid = sourceHandler.drain(sourceHandler.getTankCapacity(0), FluidAction.SIMULATE);
        if (!fluid.isEmpty()) {
            int moved = destHandler.fill(fluid, FluidAction.EXECUTE);
            if (moved > 0) {
                sourceHandler.drain(moved, FluidAction.EXECUTE);
            }
        }
    }

    public void cacheRefineryOutputs() {
        if (isRemoved()) return;

        List<LazyOptional<IFluidHandler>> cache = new ArrayList<>();

        TileEntityRefineryOutput output = findAdjacentOutput();
        while (output != null) {
            // direction DOWN is important here to get the unwrapped cap
            LazyOptional<IFluidHandler> handler = output.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN);
            if (handler.isPresent()) handler.addListener(l -> cacheRefineryOutputs());
            cache.add(handler);
            BlockEntity te = output.getCachedNeighbor(Direction.UP);
            output = te instanceof TileEntityRefineryOutput ? (TileEntityRefineryOutput) te : null;
        }

        outputCache = cache;
    }

    public TileEntityRefineryOutput findAdjacentOutput() {
        for (Direction d : DirectionUtil.VALUES) {
            if (d != Direction.DOWN) {
                BlockEntity te = getCachedNeighbor(d);
                if (te instanceof TileEntityRefineryOutput) return (TileEntityRefineryOutput) te;
            }
        }
        return null;
    }

    private boolean doRefiningStep(FluidAction action) {
        List<FluidStack> recipeOutputs = currentRecipe.getOutputs();

        for (int i = 0; i < outputCache.size() && i < recipeOutputs.size(); i++) {
        	final FluidStack outFluid = recipeOutputs.get(i);
            int filled = outputCache.get(i).map(h -> h.fill(outFluid, action)).orElse(0);
            if (filled != outFluid.getAmount()) {
            	blocked = true;
            	return false;
            }
        }

        blocked = false;
        return true;
    }

    private boolean doesRedstoneAllow() {
        // TODO need a better implementation here (cache power level in controller when any output gets an update)

        int totalPower = getRedstoneController().getCurrentRedstonePower();

        // power to each refinery output block is also considered
        TileEntityRefineryOutput teRO = findAdjacentOutput();
        if (teRO != null) {
            while (teRO.getCachedNeighbor(Direction.UP) instanceof TileEntityRefineryOutput) {
                totalPower = Math.max(totalPower, teRO.getRedstoneController().getCurrentRedstonePower());
                teRO = (TileEntityRefineryOutput) teRO.getCachedNeighbor(Direction.UP);
            }
        }

        return switch (getRedstoneController().getCurrentMode()) {
            case 0 -> true;
            case 1 -> totalPower > 0;
            case 2 -> totalPower == 0;
            default -> false;
        };
    }

    @Override
    public String getCurrentRecipeIdSynced() {
        return currentRecipeIdSynced;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    public IFluidTank getInputTank() {
        return inputTank;
    }

    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public RedstoneController<TileEntityRefineryController> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    private void maybeUpdateComparatorValue(int outputCount, boolean hasWork) {
        int newValue;
        if (inputTank.getFluidAmount() < 10 || outputCount < 2 || currentRecipe == null || outputCount > currentRecipe.getOutputs().size()) {
            newValue = 0;
        } else {
            newValue = hasWork ? 15 : 0;
        }
        if (newValue != comparatorValue) {
            // update comparator output for the controller AND all known outputs
            comparatorValue = newValue;
            nonNullLevel().updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
            TileEntityRefineryOutput output = findAdjacentOutput();
            while (output != null && !output.getBlockState().isAir()) {
                nonNullLevel().updateNeighbourForOutputSignal(output.getBlockPos(), output.getBlockState().getBlock());
                BlockEntity te = output.getCachedNeighbor(Direction.UP);
                output = te instanceof TileEntityRefineryOutput ? (TileEntityRefineryOutput) te : null;
            }
        }
    }

    @Override
    public int getComparatorValue() {
        return comparatorValue;
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
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("OilTank", inputTank);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ContainerRefinery(i, playerInventory, getBlockPos());
    }

    @Override
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        return heatCap;
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }

    private class RefineryInputTank extends SmartSyncTank {
        RefineryInputTank(int capacity) {
            super(TileEntityRefineryController.this, capacity);
        }

        @Override
        public boolean isFluidValid(FluidStack fluid) {
            return getFluid().isFluidEqual(fluid) || isInputFluidValid(level, fluid.getFluid(), 4);
        }

        @Override
        protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
            super.onContentsChanged(prevFluid, prevAmount);

            Fluid newFluid = getFluid().getFluid();
            if (prevFluid != newFluid
                    || currentRecipe == null && getFluidAmount() > prevAmount
                    || currentRecipe != null && getFluidAmount() < prevAmount) {
                searchForRecipe = true;
            }
        }
    }
}
