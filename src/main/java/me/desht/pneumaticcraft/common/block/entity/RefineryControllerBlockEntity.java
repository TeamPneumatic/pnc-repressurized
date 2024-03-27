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

package me.desht.pneumaticcraft.common.block.entity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.RefineryMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.recipes.RecipeCaches;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.AcceptabilityCache;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class RefineryControllerBlockEntity extends AbstractTickingBlockEntity
        implements IRedstoneControl<RefineryControllerBlockEntity>, IComparatorSupport, ISerializableTanks,
        MenuProvider, IHeatExchangingTE {
    @GuiSynced
    @DescSynced
    private final RefineryInputTank inputTank = new RefineryInputTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    public final SyncOnlyTank[] outputsSynced = new SyncOnlyTank[RefineryRecipe.MAX_OUTPUTS];  // purely for GUI syncing
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    @GuiSynced
    private final RedstoneController<RefineryControllerBlockEntity> rsController = new RedstoneController<>(this);
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

    private List<BlockCapabilityCache<IFluidHandler,Direction>> outputCache;
    private TemperatureRange operatingTemp = TemperatureRange.invalid();
    private RefineryRecipe currentRecipe;
    private int workTimer = 0;
    private int comparatorValue;
    private int prevOutputCount = -1;
    private boolean searchForRecipe = true;
    private int nPlayersUsing = 0;

    private static final AcceptabilityCache<Fluid> acceptedFluidCache = new AcceptabilityCache<>();

    public RefineryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.REFINERY.get(), pos, state);

        for (int i = 0; i < RefineryRecipe.MAX_OUTPUTS; i++) {
            outputsSynced[i] = new SyncOnlyTank(this, PneumaticValues.NORMAL_TANK_CAPACITY);
        }
    }

    public static void clearCachedFluids() {
        acceptedFluidCache.clear();
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public IFluidHandler getFluidHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return inputTank;
    }

    public static boolean isInputFluidValid(Level world, FluidStack fluid, int size) {
        return fluid.isEmpty() || acceptedFluidCache.isAcceptable(fluid.getFluid(), () -> ModRecipeTypes.REFINERY.get()
                .findFirst(world, r -> r.getOutputs().size() <= size && FluidUtils.matchFluid(r.getInput(), fluid, true)).isPresent());
    }

    private Optional<RecipeHolder<RefineryRecipe>> findApplicableRecipe() {
        return ModRecipeTypes.REFINERY.get().stream(level)
                .filter(r -> r.value().getOutputs().size() <= outputCount)
                .filter(r -> FluidUtils.matchFluid(r.value().getInput(), inputTank.getFluid(), true))
                .max(Comparator.comparingInt(r2 -> r2.value().getOutputs().size()));
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
            RefineryOutputBlockEntity teRO = findAdjacentOutput();
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
        getOutputCache();
        if (prevOutputCount != outputCount) {
            searchForRecipe = true;
        }
        if (searchForRecipe) {
            RecipeCaches.REFINERY.getCachedRecipe(this::findApplicableRecipe, this::genIngredientHash).ifPresentOrElse(holder -> {
                currentRecipe = holder.value();
                currentRecipeIdSynced = holder.id().toString();
                operatingTemp = currentRecipe.getOperatingTemp();
            }, () -> {
                currentRecipe = null;
                currentRecipeIdSynced = "";
                operatingTemp = TemperatureRange.invalid();
            });
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

        if (nPlayersUsing > 0) {
            for (int i = 0; i < outputCount; i++) {
                final int j = i;
                getOutputHandler(i).ifPresent(h -> {
                    if (!outputsSynced[j].getFluid().isFluidStackIdentical(h.getFluidInTank(0))) {
                        outputsSynced[j].setFluid(h.getFluidInTank(0).copy());
                        outputsSynced[j].tick();
                    }
                });
            }
        }

        prevOutputCount = outputCount;
        maybeUpdateComparatorValue(hasWork);
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
        for (int i = 0; i < outputCount && i < currentRecipe.getOutputs().size(); i++) {
            final FluidStack wantedFluid = currentRecipe.getOutputs().get(i);
            getOutputHandler(i).ifPresent(outputHandler -> {
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
            getOutputHandler(i).ifPresent(outputHandler -> tryMoveFluid(tempTank, outputHandler));
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

    public void clearOutputCache() {
        outputCount = 0;
        outputCache = null;
    }

    private List<BlockCapabilityCache<IFluidHandler,Direction>> getOutputCache() {
        if (outputCache == null) {
            outputCache = new ArrayList<>();
            RefineryOutputBlockEntity output = findAdjacentOutput();
            while (output != null) {
                // direction DOWN is important here to get the unwrapped cap
                outputCache.add(BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, (ServerLevel) getLevel(),
                        output.getBlockPos(), Direction.DOWN,
                        () -> !isRemoved(), () -> outputCache = null));
                BlockEntity te = getLevel().getBlockEntity(output.getBlockPos().above());
                output = te instanceof RefineryOutputBlockEntity ? (RefineryOutputBlockEntity) te : null;
            }
        }
        outputCount = outputCache.size();
        return outputCache;
    }

    private Optional<IFluidHandler> getOutputHandler(int n) {
        return n >= 0 && n < outputCount ?
                Optional.ofNullable(getOutputCache().get(n).getCapability()) :
                Optional.empty();
    }

    public RefineryOutputBlockEntity findAdjacentOutput() {
        for (Direction d : DirectionUtil.VALUES) {
            if (d != Direction.DOWN && getCachedNeighbor(d) instanceof RefineryOutputBlockEntity output) {
                return output;
            }
        }
        return null;
    }

    private boolean doRefiningStep(FluidAction action) {
        List<FluidStack> recipeOutputs = currentRecipe.getOutputs();

        for (int i = 0; i < outputCount && i < recipeOutputs.size(); i++) {
            final FluidStack outFluid = recipeOutputs.get(i);
            int filled = getOutputHandler(i).map(h -> h.fill(outFluid, action)).orElse(0);
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
        RefineryOutputBlockEntity teRO = findAdjacentOutput();
        if (teRO != null) {
            while (teRO.getCachedNeighbor(Direction.UP) instanceof RefineryOutputBlockEntity) {
                totalPower = Math.max(totalPower, teRO.getRedstoneController().getCurrentRedstonePower());
                teRO = (RefineryOutputBlockEntity) teRO.getCachedNeighbor(Direction.UP);
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
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    public IFluidTank getInputTank() {
        return inputTank;
    }

    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public RedstoneController<RefineryControllerBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    private void maybeUpdateComparatorValue(boolean hasWork) {
        int newValue;
        if (hasWork && currentRecipe != null && inputTank.getFluidAmount() >= currentRecipe.getInput().getAmount() && outputCount >= currentRecipe.getOutputs().size()) {
            newValue = 15;
        } else {
            newValue = 0;
        }
        if (newValue != comparatorValue) {
            // update comparator output for the controller AND all known outputs
            comparatorValue = newValue;
            nonNullLevel().updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
            RefineryOutputBlockEntity output = findAdjacentOutput();
            while (output != null && !output.getBlockState().isAir()) {
                nonNullLevel().updateNeighbourForOutputSignal(output.getBlockPos(), output.getBlockState().getBlock());
                BlockEntity te = output.getCachedNeighbor(Direction.UP);
                output = te instanceof RefineryOutputBlockEntity ? (RefineryOutputBlockEntity) te : null;
            }
        }
    }

    @Override
    public int getComparatorValue() {
        return comparatorValue;
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("OilTank", inputTank);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new RefineryMenu(i, playerInventory, getBlockPos());
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }

    public void incPlayersUsing() {
        nPlayersUsing++;
    }

    public void decPlayersUsing() {
        if (nPlayersUsing == 0) {
            Log.warning("decPlayersUsing() called for " + this + " but already 0?");
        } else {
            nPlayersUsing = Math.max(0, nPlayersUsing - 1);
        }
    }

    public int genIngredientHash() {
        int n = getInputTank().getFluid().hasTag() ? getInputTank().getFluid().getTag().hashCode() : 0;
        return Objects.hash(BuiltInRegistries.FLUID.getId(getInputTank().getFluid().getFluid()), n);
    }

    private class RefineryInputTank extends SmartSyncTank {
        RefineryInputTank(int capacity) {
            super(RefineryControllerBlockEntity.this, capacity);
        }

        @Override
        public boolean isFluidValid(FluidStack fluid) {
            return getFluid().isFluidEqual(fluid) || isInputFluidValid(level, fluid, 4);
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

    public static class SyncOnlyTank extends SmartSyncTank {
        SyncOnlyTank(BlockEntity owner, int capacity) {
            super(owner, capacity);
        }

        @Override
        protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
            // do nothing, this tank is for client sync only
        }
    }
}
