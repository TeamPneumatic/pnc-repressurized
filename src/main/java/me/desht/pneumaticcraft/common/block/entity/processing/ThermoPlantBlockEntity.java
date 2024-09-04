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

package me.desht.pneumaticcraft.common.block.entity.processing;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.inventory.ThermoPlantMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.recipes.RecipeCaches;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.AcceptabilityCache;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ThermoPlantBlockEntity extends AbstractAirHandlingBlockEntity implements
        IMinWorkingPressure, IRedstoneControl<ThermoPlantBlockEntity>, ISerializableTanks,
        IAutoFluidEjecting, MenuProvider, IComparatorSupport, IHeatExchangingTE {

    private static final int INVENTORY_SIZE = 1;
    private static final int CRAFTING_TIME = 60 * 100;  // 60 ticks base crafting time
    private static final double MAX_SPEED_UP = 2.5;  // speed-up due to temperature

    private static final AcceptabilityCache<Item> acceptedItemCache = new AcceptabilityCache<>();
    private static final AcceptabilityCache<Fluid> acceptedFluidCache = new AcceptabilityCache<>();

    @GuiSynced
    @DescSynced
    private final ThermopneumaticFluidTankInput inputTank = new ThermopneumaticFluidTankInput(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    @DescSynced
    private final SmartSyncTank outputTank = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    @GuiSynced
    public final RedstoneController<ThermoPlantBlockEntity> rsController = new RedstoneController<>(this);
    @GuiSynced
    private float craftingProgress;
    @GuiSynced
    private float requiredPressure;
    @GuiSynced
    public int minTemperature;
    @GuiSynced
    public int maxTemperature;
    @GuiSynced
    public TPProblem problem = TPProblem.OK;
    @DescSynced
    private boolean didWork;
    @GuiSynced
    private String currentRecipeIdSynced = "";
    private ThermoPlantRecipe currentRecipe;
    private boolean searchForRecipe = true;

    private final ItemStackHandler inputItemHandler = new InputItemHandler(this);
    private final ItemStackHandler outputItemHandler = new BaseItemStackHandler(this, INVENTORY_SIZE);
    private final ThermopneumaticInvWrapper invWrapper = new ThermopneumaticInvWrapper(inputItemHandler, outputItemHandler);

    private final ThermopneumaticFluidHandler fluidHandler = new ThermopneumaticFluidHandler();
    private double airUsage;

    public ThermoPlantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), pos, state, PressureTier.TIER_ONE_HALF, 3000, 4);
        heatExchanger.setThermalResistance(10);
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public boolean canConnectPneumatic(Direction dir) {
        return getRotation().getOpposite() != dir && dir != Direction.UP;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        inputTank.tick();
        outputTank.tick();
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (didWork && nonNullLevel().random.nextBoolean()) {
            ClientUtils.emitParticles(nonNullLevel(), getBlockPos(), ParticleTypes.SMOKE, 0.9);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        problem = TPProblem.OK;

        ThermoPlantRecipe prevRecipe = currentRecipe;
        if (searchForRecipe) {
            RecipeCaches.THERMO_PLANT.getCachedRecipe(this::findApplicableRecipe, this::genIngredientHash).ifPresentOrElse(holder -> {
                currentRecipe = holder.value();
                currentRecipeIdSynced = holder.id().toString();
            }, () -> {
                currentRecipe = null;
                currentRecipeIdSynced = "";
            });
            searchForRecipe = false;
        }
        if (prevRecipe != currentRecipe) {
            nonNullLevel().updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
        }

        didWork = false;
        if (currentRecipe != null) {
            if (getInputTank().getFluidAmount() < currentRecipe.getInputFluidAmount()) {
                problem = TPProblem.NOT_ENOUGH_FLUID;
            } else if (heatExchanger.getTemperature() > currentRecipe.getOperatingTemperature().getMax()) {
                problem = TPProblem.TOO_HOT;
            } else if (heatExchanger.getTemperature() < currentRecipe.getOperatingTemperature().getMin()) {
                problem = TPProblem.TOO_COLD;
            } else if (rsController.shouldRun() && hasEnoughPressure()) {
                runOneCycle();
            }
        } else {
            problem = TPProblem.NO_RECIPE;
            craftingProgress = 0;
            minTemperature = 0;
            maxTemperature = 0;
            requiredPressure = 0;
        }
    }

    private void runOneCycle() {
        // do one work cycle of the TPP
        float speedBoost = (float) (minTemperature > 0 ? Math.min(MAX_SPEED_UP, heatExchanger.getTemperature() / minTemperature) : 1.0);
        if (craftingProgress < CRAFTING_TIME) {
            float progressInc = speedBoost * currentRecipe.getRecipeSpeed() * 100;
            craftingProgress += progressInc;
            float progressDivider = progressInc / CRAFTING_TIME;
            airUsage += currentRecipe.airUsed() * progressDivider * speedBoost * currentRecipe.getAirUseMultiplier();
            if (airUsage > 1) {
                int i = (int) airUsage;
                addAir(-i);
                airUsage -= i;
            }
            heatExchanger.addHeat(-currentRecipe.heatUsed(heatExchanger.getAmbientTemperature()) * speedBoost * 0.75 * progressDivider);
        }
        if (craftingProgress >= CRAFTING_TIME) {
            int filled = outputTank.fill(currentRecipe.getOutputFluid().copy(), FluidAction.SIMULATE);
            ItemStack excess = outputItemHandler.insertItem(0, currentRecipe.getOutputItem().copy(), true);
            if (filled == currentRecipe.getOutputFluid().getAmount() && excess.isEmpty()) {
                outputTank.fill(currentRecipe.getOutputFluid().copy(), FluidAction.EXECUTE);
                outputItemHandler.insertItem(0, currentRecipe.getOutputItem().copy(), false);
                inputTank.drain(currentRecipe.getInputFluidAmount(), FluidAction.EXECUTE);
                inputItemHandler.extractItem(0, getInputItemCount(), false);
                craftingProgress -= CRAFTING_TIME;
            } else {
                problem = TPProblem.OUTPUT_BLOCKED;
            }
        }
        didWork = problem == TPProblem.OK;
    }

    private int getInputItemCount() {
        if (currentRecipe != null) {
            ItemStack[] stacks = currentRecipe.getInputItem().map(Ingredient::getItems).orElse(new ItemStack[0]);
            return stacks.length > 0 ? stacks[0].getCount() : 0;
        }
        return 0;
    }

    private boolean hasEnoughPressure() {
        if (getMinWorkingPressure() == 0) {
            return true;
        } else if (getMinWorkingPressure() > 0) {
            return getPressure() >= getMinWorkingPressure();
        } else {
            return getPressure() <= getMinWorkingPressure();
        }
    }

    /**
     * Find a recipe which matches the input fluid and/or item
     *
     * @return a recipe, or null for no matching recipe
     */
    private Optional<RecipeHolder<ThermoPlantRecipe>> findApplicableRecipe() {
        for (RecipeHolder<ThermoPlantRecipe> holder : ModRecipeTypes.getRecipes(level, ModRecipeTypes.THERMO_PLANT)) {
            ThermoPlantRecipe recipe = holder.value();
            if (recipe.matches(inputTank.getFluid(), inputItemHandler.getStackInSlot(0))) {
                requiredPressure = recipe.getRequiredPressure();
                minTemperature = recipe.getOperatingTemperature().getMin();
                maxTemperature = recipe.getOperatingTemperature().getMax();
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }

    public IFluidTank getInputTank() {
        return inputTank;
    }

    public IFluidTank getOutputTank() {
        return outputTank;
    }

    @Override
    public IFluidHandler getFluidHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return fluidHandler;
    }

    public double getCraftingPercentage() {
        return (double) craftingProgress / CRAFTING_TIME;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("Items", inputItemHandler.serializeNBT(provider));
        tag.put("Output", outputItemHandler.serializeNBT(provider));
        tag.putFloat("craftingProgress", craftingProgress);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        inputItemHandler.deserializeNBT(provider, tag.getCompound("Items"));
        outputItemHandler.deserializeNBT(provider, tag.getCompound("Output"));
        craftingProgress = tag.getFloat("craftingProgress");
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;
        if (tag.equals("dump")) {
            FluidStack moved;
            if (shiftHeld) {
                moved = inputTank.drain(inputTank.getCapacity(), FluidAction.EXECUTE);
            } else {
                moved = FluidUtil.tryFluidTransfer(outputTank, inputTank, inputTank.getFluidAmount(), true);
            }
            if (!moved.isEmpty()) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, worldPosition, 1f, 1f, false), player);
            }
        }
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return invWrapper;
    }

    @Override
    public RedstoneController<ThermoPlantBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public float getMinWorkingPressure() {
        return requiredPressure;
    }

    @Nonnull
    @Override
    public Map<DataComponentType<SimpleFluidContent>, PNCFluidTank> getSerializableTanks() {
        return Map.of(
                ModDataComponents.INPUT_TANK_1.get(), inputTank,
                ModDataComponents.OUTPUT_TANK.get(), outputTank
        );
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ThermoPlantMenu(i, playerInventory, getBlockPos());
    }

    public IItemHandler getOutputInventory() {
        return outputItemHandler;
    }

    @Override
    public int getComparatorValue() {
        if (currentRecipe == null
                || outputTank.fill(currentRecipe.getOutputFluid().copy(), FluidAction.SIMULATE) < currentRecipe.getOutputFluid().getAmount()
                || !outputItemHandler.insertItem(0, currentRecipe.getOutputItem().copy(), true).isEmpty()) {
            return 0;
        } else {
            return 15;
        }
    }

    public static void clearCachedItemsAndFluids() {
        acceptedItemCache.clear();
        acceptedFluidCache.clear();
    }

    @Override
    public IHeatExchangerLogic getHeatExchanger(Direction dir) {
        return heatExchanger;
    }

    @Override
    public String getCurrentRecipeIdSynced() {
        return currentRecipeIdSynced;
    }

    public int genIngredientHash() {
        return Objects.hash(
                FluidStack.hashFluidAndComponents(inputTank.getFluid()),
                ItemStack.hashItemAndComponents(inputItemHandler.getStackInSlot(0))
        );
    }

    public IItemHandler getInputItemHandler() {
        return inputItemHandler;
    }

    private class ThermopneumaticFluidTankInput extends SmartSyncTank {
        ThermopneumaticFluidTankInput(int capacity){
            super(ThermoPlantBlockEntity.this, capacity);
        }

        @Override
        public boolean isFluidValid(FluidStack fluid) {
            return fluid.isEmpty() || acceptedFluidCache.isAcceptable(fluid.getFluid(), () ->
                    ModRecipeTypes.THERMO_PLANT.get().stream(level)
                            .anyMatch(r -> r.value().testFluid(fluid))
            );
        }

        @Override
        protected void onContentsChanged(FluidStack prevStack) {
            super.onContentsChanged(prevStack);

            if (!FluidStack.isSameFluidSameComponents(prevStack, getFluid())
                    || currentRecipe == null && getFluidAmount() > prevStack.getAmount()
                    || currentRecipe != null && getFluidAmount() < prevStack.getAmount()) {
                searchForRecipe = true;
            }
        }
    }

    private class InputItemHandler extends BaseItemStackHandler {
        private Item prev = null;

        public InputItemHandler(BlockEntity te) {
            super(te, INVENTORY_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.isEmpty() || acceptedItemCache.isAcceptable(stack.getItem(), () ->
                    ModRecipeTypes.THERMO_PLANT.get().stream(level)
                            .anyMatch(r -> r.value().testItem(stack))
            );
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (getStackInSlot(0).getItem() != prev) {
                searchForRecipe = true;
            }
            prev = getStackInSlot(0).getItem();
        }
    }

    private class ThermopneumaticFluidHandler implements IFluidHandler {
        final IFluidTank[] tanks;

        ThermopneumaticFluidHandler() {
            tanks = new IFluidTank[]{ inputTank, outputTank };
        }

        @Override
        public int getTanks() {
            return tanks.length;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return tanks[tank].getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tanks[tank].getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return tanks[tank].isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            return inputTank.fill(resource, doFill);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            return FluidStack.isSameFluidSameComponents(outputTank.getFluid(), resource) ?
                    outputTank.drain(resource.getAmount(), doDrain) :
                    FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            return outputTank.drain(maxDrain, doDrain);
        }
    }

    private class ThermopneumaticInvWrapper implements IItemHandler {
        private final IItemHandler input;
        private final IItemHandler output;

        ThermopneumaticInvWrapper(IItemHandler input, IItemHandler output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? input.getStackInSlot(0) : output.getStackInSlot(0);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return slot == 0 ? inputItemHandler.insertItem(0, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? outputItemHandler.extractItem(0, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? input.getSlotLimit(0) : output.getSlotLimit(0);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 ? input.isItemValid(0, stack) : output.isItemValid(0, stack);
        }
    }

    public enum TPProblem implements ITranslatableEnum {
        OK(""),
        NO_RECIPE("noRecipe"),
        NOT_ENOUGH_FLUID("notEnoughFluid"),
        TOO_HOT("tooMuchHeat"),
        TOO_COLD("notEnoughHeat"),
        OUTPUT_BLOCKED("outputBlocked");

        private final String key;

        TPProblem(String key) {
            this.key = key;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.problems." + key;
        }
    }
}
