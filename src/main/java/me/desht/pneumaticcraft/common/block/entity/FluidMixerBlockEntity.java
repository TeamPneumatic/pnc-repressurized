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
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.FluidMixerMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.inventory.handler.OutputItemHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.recipes.RecipeCaches;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FluidMixerBlockEntity extends AbstractAirHandlingBlockEntity implements
        IMinWorkingPressure, IRedstoneControl<FluidMixerBlockEntity>, MenuProvider,
        ISerializableTanks, IAutoFluidEjecting
{
    // Maps a fluid to all the other fluids it can combine with
    private static final Map<Fluid, Set<Fluid>> FLUID_MATCHES = new HashMap<>();

    private final ItemStackHandler outputInv = new BaseItemStackHandler(this, 1);
    private final OutputItemHandler outputInvWrapper = new OutputItemHandler(outputInv);
//    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> outputInvWrapper);

    @GuiSynced
    @DescSynced
    private final SmartSyncTank inputTank1 = new InputTank();
    @GuiSynced
    @DescSynced
    private final SmartSyncTank inputTank2 = new InputTank();
    @GuiSynced
    @DescSynced
    private final SmartSyncTank outputTank = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    private float requiredPressure;
    @GuiSynced
    public float craftingProgress = 0;
    @GuiSynced
    public int maxProgress; // 0 when no recipe, recipe's process time * 100 when there is a recipe
    @DescSynced
    public boolean didWork;
    @GuiSynced
    private final RedstoneController<FluidMixerBlockEntity> rsController = new RedstoneController<>(this);
    @GuiSynced
    private String currentRecipeIdSynced = "";

    private float airUsed;
    private FluidMixerRecipe currentRecipe = null;
    private boolean searchRecipes = true;
    private final MixerFluidHandler fluidHandler = new MixerFluidHandler();
//    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> fluidHandler);

    private final SmartSyncTank[] tanks = new SmartSyncTank[] { inputTank1, inputTank2, outputTank };

    public FluidMixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FLUID_MIXER.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_FLUID_MIXER, 4);
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        inputTank1.tick();
        inputTank2.tick();
        outputTank.tick();
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (didWork && nonNullLevel().random.nextFloat() < 0.1f) {
            ClientUtils.emitParticles(level, worldPosition, ParticleTypes.RAIN);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        didWork = false;
        if (searchRecipes) {
            RecipeCaches.FLUID_MIXER.getCachedRecipe(this::findApplicableRecipe, this::genIngredientHash).ifPresentOrElse(holder -> {
                currentRecipe = holder.value();
                currentRecipeIdSynced = holder.id().toString();
                requiredPressure = currentRecipe.getRequiredPressure();
                maxProgress = currentRecipe.getProcessingTime() * 100;
            }, () -> {
                currentRecipe = null;
                currentRecipeIdSynced = "";
                requiredPressure = 0f;
                maxProgress = 0;
            });
            searchRecipes = false;
        }
        if (rsController.shouldRun() && currentRecipe != null && getPressure() >= requiredPressure && hasOutputSpace()) {
            craftingProgress += 100 * (1 + Math.min(getPressure() - requiredPressure, 1.5f));
            didWork = true;
            airUsed += 2.5f * getPressure();
            if (airUsed > 1f) {
                int a = (int) airUsed;
                airHandler.addAir(-a);
                airUsed -= a;
            }
            if (craftingProgress >= maxProgress && takeInputIngredients()) {
                if (!currentRecipe.getOutputFluid().isEmpty()) {
                    outputTank.fill(currentRecipe.getOutputFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
                }
                if (!currentRecipe.getOutputItem().isEmpty()) {
                    outputInv.insertItem(0, currentRecipe.getOutputItem().copy(), false);
                }
                craftingProgress -= maxProgress;
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("Items", outputInv.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        outputInv.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public String getCurrentRecipeIdSynced() {
        return currentRecipeIdSynced;
    }

    private boolean takeInputIngredients() {
        if (currentRecipe.getInput1().testFluid(inputTank1.getFluid()) && currentRecipe.getInput2().testFluid(inputTank2.getFluid())) {
            inputTank1.drain(currentRecipe.getInput1().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            inputTank2.drain(currentRecipe.getInput2().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            return true;
        } else if (currentRecipe.getInput2().testFluid(inputTank1.getFluid()) && currentRecipe.getInput1().testFluid(inputTank2.getFluid())) {
            inputTank1.drain(currentRecipe.getInput2().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            inputTank2.drain(currentRecipe.getInput1().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    public boolean hasOutputSpace() {
        if (!currentRecipe.getOutputItem().isEmpty() && !outputInv.insertItem(0, currentRecipe.getOutputItem(), true).isEmpty()) {
            return false;
        }
        return currentRecipe.getOutputFluid().isEmpty()
                || outputTank.fill(currentRecipe.getOutputFluid(), IFluidHandler.FluidAction.SIMULATE) >= currentRecipe.getOutputFluid().getAmount();
    }

    private Optional<RecipeHolder<FluidMixerRecipe>> findApplicableRecipe() {
        for (RecipeHolder<FluidMixerRecipe> holder : ModRecipeTypes.getRecipes(level, ModRecipeTypes.FLUID_MIXER)) {
            if (holder.value().matches(inputTank1.getFluid(), inputTank2.getFluid())) {
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return outputInvWrapper;
    }

    @Override
    public IFluidHandler getFluidHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return fluidHandler;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP && side != getRotation();
    }

    public static void clearCachedFluids() {
        FLUID_MATCHES.clear();
    }

    public static void cacheRecipeFluids(List<FluidMixerRecipe> values) {
        for (FluidMixerRecipe recipe : values) {
            for (FluidStack input1 : recipe.getInput1().getFluidStacks()) {
                for (FluidStack input2 : recipe.getInput2().getFluidStacks()) {
                    Set<Fluid> fluidSet1 = FLUID_MATCHES.computeIfAbsent(input1.getFluid(), k -> new HashSet<>());
                    Set<Fluid> fluidSet2 = FLUID_MATCHES.computeIfAbsent(input2.getFluid(), k -> new HashSet<>());
                    fluidSet1.add(input2.getFluid());
                    fluidSet2.add(input1.getFluid());
                }
            }
        }
    }

    /**
     * Is this fluid in any of the fluid mixer's recipe ingredients?
     * @param fluid the fluid to test
     * @return true if the fluid is accepted by the mixer at all, false otherwise
     */
    public static boolean isFluidAccepted(Fluid fluid) {
        return FLUID_MATCHES.containsKey(fluid);
    }

    /**
     * Are the two fluids compatible in the fluid mixer?
     * @param fluid1 the first fluid
     * @param fluid2 the second fluid
     * @return true if the fluids can be combine in the mixer, false otherwise
     */
    public static boolean isFluidMatch(Fluid fluid1, Fluid fluid2) {
        Set<Fluid> fluids = FLUID_MATCHES.get(fluid1);
        return fluids != null && fluids.contains(fluid2);
    }

    @Override
    public float getMinWorkingPressure() {
        return requiredPressure;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new FluidMixerMenu(windowId, inv, worldPosition);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;

        if (tag.startsWith("dump")) {
            try {
                moveOrDump(player, Integer.parseInt(tag.substring(4)), shiftHeld);
            } catch (NumberFormatException ignored) {
            }
        } else {
            super.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }

    private void moveOrDump(Player player, int tank, boolean shiftHeld) {
        FluidStack moved;
        SmartSyncTank inputTank = tank == 1 ? inputTank1 : inputTank2;
        if (shiftHeld) {
            moved = inputTank.drain(inputTank.getCapacity(), IFluidHandler.FluidAction.EXECUTE);
        } else {
            moved = FluidUtil.tryFluidTransfer(outputTank, inputTank, inputTank.getFluidAmount(), true);
        }
        if (!moved.isEmpty() && player instanceof ServerPlayer) {
            NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, worldPosition, 1f, 1f, false), (ServerPlayer) player);
        }
    }

    @Override
    public RedstoneController<FluidMixerBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("Input1", inputTank1, "Input2", inputTank2, "Output", outputTank);
    }

    public IFluidTank getInputTank1() {
        return inputTank1;
    }

    public IFluidTank getInputTank2() {
        return inputTank2;
    }

    public IFluidTank getOutputTank() {
        return outputTank;
    }

    public float getCraftingPercentage() {
        return maxProgress > 0 ? (float)craftingProgress / maxProgress : 0;
    }

    public int genIngredientHash() {
        FluidStack f1 = inputTank1.getFluid();
        FluidStack f2 = inputTank2.getFluid();
        int n1 = f1.hasTag() ? f1.getTag().hashCode() : 0;
        int n2 = f2.hasTag() ? f2.getTag().hashCode() : 0;

        return Objects.hash(
                BuiltInRegistries.FLUID.getId(f1.getFluid()),
                n1,
                BuiltInRegistries.FLUID.getId(f2.getFluid()),
                n2
        );
    }

    private class MixerFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 3;
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
        public int fill(FluidStack resource, FluidAction action) {
            if (isFluidAccepted(resource.getFluid())) {
                if (inputTank1.isEmpty() && inputTank2.isEmpty()) {
                    // both tanks empty - fill first tank by default
                    return inputTank1.fill(resource, action);
                } else if (inputTank2.isEmpty()) {
                    if (resource.getFluid() == inputTank1.getFluid().getFluid()) {
                        return inputTank1.fill(resource, action);
                    } else if (isFluidMatch(resource.getFluid(), inputTank1.getFluid().getFluid())) {
                        // fluid in first tank only - fill second tank iff fluid is compatible
                        return inputTank2.fill(resource, action);
                    }
                } else if (inputTank1.isEmpty()) {
                    if (resource.getFluid() == inputTank2.getFluid().getFluid()) {
                        return inputTank2.fill(resource, action);
                    } else if (isFluidMatch(resource.getFluid(), inputTank2.getFluid().getFluid())) {
                        // fluid in second tank only - fill first tank iff fluid is compatible
                        return inputTank1.fill(resource, action);
                    }
                } else {
                    // fluid in both tanks - fill whichever one matches, if either
                    int filled = inputTank1.fill(resource, action);
                    return filled == 0 ? inputTank2.fill(resource, action) : filled;
                }
            }
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return outputTank.drain(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return outputTank.drain(maxDrain, action);
        }
    }

    private class InputTank extends SmartSyncTank {
        InputTank() {
            super(FluidMixerBlockEntity.this, PneumaticValues.NORMAL_TANK_CAPACITY);
        }

        @Override
        protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
            super.onContentsChanged(prevFluid, prevAmount);
            searchRecipes = true;
        }
    }
}
