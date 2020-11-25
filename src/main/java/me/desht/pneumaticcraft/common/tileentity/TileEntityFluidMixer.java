package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerFluidMixer;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.inventory.handler.OutputItemHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.FluidMixerRecipeImpl;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityFluidMixer extends TileEntityPneumaticBase implements
        IMinWorkingPressure, IRedstoneControl<TileEntityFluidMixer>, INamedContainerProvider,
        ISerializableTanks, IAutoFluidEjecting
{
    // Maps a fluid to all of the other fluids it can combine with
    private static final Map<Fluid, Set<Fluid>> FLUID_MATCHES = new HashMap<>();

    private final ItemStackHandler outputInv = new BaseItemStackHandler(this, 1);
    private final OutputItemHandler outputInvWrapper = new OutputItemHandler(outputInv);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> outputInvWrapper);

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
    private int redstoneMode;
    @GuiSynced
    public int craftingProgress = 0;
    @GuiSynced
    public int maxProgress; // 0 when no recipe, recipe's process time * 100 when there is a recipe
    @DescSynced
    public boolean didWork;
    @GuiSynced
    private final RedstoneController<TileEntityFluidMixer> rsController = new RedstoneController<>(this);

    private float airUsed;
    private FluidMixerRecipe currentRecipe = null;
    private boolean searchRecipes = true;
    private final MixerFluidHandler fluidHandler = new MixerFluidHandler();
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> fluidHandler);

    private final SmartSyncTank[] tanks = new SmartSyncTank[] { inputTank1, inputTank2, outputTank };

    public TileEntityFluidMixer() {
        super(ModTileEntities.FLUID_MIXER.get(), PneumaticValues.DANGER_PRESSURE_TIER_ONE, PneumaticValues.MAX_PRESSURE_TIER_ONE, PneumaticValues.VOLUME_FLUID_MIXER, 4);
    }

    @Override
    public void tick() {
        super.tick();

        inputTank1.tick();
        inputTank2.tick();
        outputTank.tick();

        if (!world.isRemote) {
            didWork = false;
            if (searchRecipes) {
                currentRecipe = findApplicableRecipe();
                requiredPressure = currentRecipe != null ? currentRecipe.getRequiredPressure() : 0f;
                maxProgress = currentRecipe != null ? currentRecipe.getProcessingTime() * 100 : 0;
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
        } else {
            if (didWork && world.rand.nextFloat() < 0.1f) {
                ClientUtils.emitParticles(world, pos, ParticleTypes.RAIN);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Items", outputInv.serializeNBT());

        return tag;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        outputInv.deserializeNBT(tag.getCompound("Items"));
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
        if (!currentRecipe.getOutputItem().isEmpty()
                && !outputInv.insertItem(0, currentRecipe.getOutputItem(), true).isEmpty()) {
            return false;
        }
        if (!currentRecipe.getOutputFluid().isEmpty()
                && outputTank.fill(currentRecipe.getOutputFluid(), IFluidHandler.FluidAction.SIMULATE) < currentRecipe.getOutputFluid().getAmount()) {
            return false;
        }
        return true;
    }

    private FluidMixerRecipe findApplicableRecipe() {
        for (FluidMixerRecipe recipe : PneumaticCraftRecipeType.FLUID_MIXER.getRecipes(world).values()) {
            if (recipe.matches(inputTank1.getFluid(), inputTank2.getFluid())) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return outputInvWrapper;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCap.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP && side != getRotation();
    }

    public static void clearCachedFluids() {
        FLUID_MATCHES.clear();
    }

    public static void cacheRecipeFluids(List<FluidMixerRecipeImpl> values) {
        for (FluidMixerRecipeImpl recipe : values) {
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

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ContainerFluidMixer(windowId, inv, pos);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
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

    private void moveOrDump(PlayerEntity player, int tank, boolean shiftHeld) {
        FluidStack moved;
        SmartSyncTank inputTank = tank == 1 ? inputTank1 : inputTank2;
        if (shiftHeld) {
            moved = inputTank.drain(inputTank.getCapacity(), IFluidHandler.FluidAction.EXECUTE);
        } else {
            moved = FluidUtil.tryFluidTransfer(outputTank, inputTank, inputTank.getFluidAmount(), true);
        }
        if (!moved.isEmpty() && player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new PacketPlaySound(SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, pos, 1f, 1f, false), (ServerPlayerEntity) player);
        }
    }

    @Override
    public RedstoneController<TileEntityFluidMixer> getRedstoneController() {
        return rsController;
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
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
            super(TileEntityFluidMixer.this, PneumaticValues.NORMAL_TANK_CAPACITY);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            searchRecipes = true;
        }
    }
}
