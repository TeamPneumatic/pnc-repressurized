package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityThermopneumaticProcessingPlant extends TileEntityPneumaticBase
        implements IHeatExchanger, IMinWorkingPressure, IRedstoneControlled,
        ISerializableTanks, ISmartFluidSync, IAutoFluidEjecting, INamedContainerProvider {

    private static final int INVENTORY_SIZE = 1;
    private static final int CRAFTING_TIME = 60 * 100;
    private static final double MAX_SPEED_UP = 2.5;

    @GuiSynced
    @DescSynced
    @LazySynced
    private final ThermopneumaticFluidTankInput inputTank = new ThermopneumaticFluidTankInput(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    @DescSynced
    @LazySynced
    private final ThermopneumaticFluidTankOutput outputTank = new ThermopneumaticFluidTankOutput(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @GuiSynced
    public int redstoneMode;
    @GuiSynced
    private int craftingProgress;
    @GuiSynced
    public boolean hasRecipe;
    @GuiSynced
    private float requiredPressure;
    @GuiSynced
    public int minTemperature;
    @GuiSynced
    public int maxTemperature;
    @SuppressWarnings("unused")
    @DescSynced
    private int inputAmountScaled, outputAmountScaled;
    @DescSynced
    private boolean didWork;
    private IThermopneumaticProcessingPlantRecipe currentRecipe;
    private boolean searchForRecipe = true;

    private final ItemStackHandler itemHandler = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.isEmpty()
                    || PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes.values().stream().anyMatch(r -> r.getInputItem().test(stack));
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            searchForRecipe = true;
        }
    };
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> itemHandler);

    private final ThermopneumaticFluidHandler fluidHandler = new ThermopneumaticFluidHandler();
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> fluidHandler);

    public TileEntityThermopneumaticProcessingPlant() {
        super(ModTileEntityTypes.THERMOPNEUMATIC_PROCESSING_PLANT, 5, 7, 3000, 4);
        heatExchanger.setThermalResistance(10);
    }

    @Override
    public boolean canConnectPneumatic(Direction dir) {
        return getRotation().getOpposite() != dir && dir != Direction.UP;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isRemote) {
            // bit of a kludge since inv/fluid changes aren't always reliably detected
            if (searchForRecipe || (getWorld().getGameTime() & 0xf) == 0) {
                currentRecipe = getValidRecipe();
                searchForRecipe = false;
            }
            hasRecipe = currentRecipe != null;
            didWork = false;
            if (hasRecipe) {
                requiredPressure = currentRecipe.getRequiredPressure();
                minTemperature = currentRecipe.getOperatingTemperature().getMin();
                maxTemperature = currentRecipe.getOperatingTemperature().getMax();
                if (redstoneAllows()
                        && currentRecipe.getOperatingTemperature().inRange(heatExchanger.getTemperature())
                        && getPressure() >= getMinWorkingPressure()) {
                    double inc = minTemperature > 0 ? Math.min(MAX_SPEED_UP, heatExchanger.getTemperature() / minTemperature) : 1.0;
                    craftingProgress += inc * 100;
                    if (craftingProgress >= CRAFTING_TIME) {
                        outputTank.fill(currentRecipe.getOutputFluid().copy(), FluidAction.EXECUTE);
                        inputTank.drain(currentRecipe.getInputFluid().getAmount(), FluidAction.EXECUTE);
                        itemHandler.extractItem(0, 1, false);
                        addAir(-currentRecipe.airUsed());
                        heatExchanger.addHeat(-currentRecipe.heatUsed() * inc * 0.75);
                        craftingProgress -= CRAFTING_TIME;
                    }
                    didWork = true;
                }
            } else {
                craftingProgress = 0;
                minTemperature = 0;
                maxTemperature = 0;
                requiredPressure = 0;
            }
        } else {
            if (didWork && getWorld().rand.nextBoolean()) {
                ClientUtils.emitParticles(getWorld(), getPos(), ParticleTypes.SMOKE);
            }
        }
    }

    private IThermopneumaticProcessingPlantRecipe getValidRecipe() {
        for (IThermopneumaticProcessingPlantRecipe recipe : PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes.values()) {
            if (recipe.matches(inputTank.getFluid(), itemHandler.getStackInSlot(0))) {
                if (outputTank.getFluid().isEmpty()) {
                    return recipe;
                } else {
                    FluidStack output = recipe.getOutputFluid();
                    if (output.getFluid() == outputTank.getFluid().getFluid()
                            && output.getAmount() <= outputTank.getCapacity() - outputTank.getFluidAmount()) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
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

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    public double getCraftingPercentage() {
        return (double) craftingProgress / CRAFTING_TIME;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Items", itemHandler.serializeNBT());
        tag.putByte("redstoneMode", (byte) redstoneMode);
        tag.putInt("craftingProgress", craftingProgress);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        itemHandler.deserializeNBT(tag.getCompound("Items"));
        redstoneMode = tag.getByte("redstoneMode");
        craftingProgress = tag.getInt("craftingProgress");

        inputAmountScaled = inputTank.getScaledFluidAmount();
        outputAmountScaled = outputTank.getScaledFluidAmount();
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return heatExchanger;
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        } else if (tag.equals("dump")) {
            inputTank.drain(inputTank.getCapacity(), FluidAction.EXECUTE);
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure() {
        return requiredPressure;
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("InputTank", inputTank, "OutputTank", outputTank);
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
        return new ContainerThermopneumaticProcessingPlant(i, playerInventory, getPos());
    }

    private class ThermopneumaticFluidTankInput extends SmartSyncTank {
        private Fluid prevFluid;

        ThermopneumaticFluidTankInput(int capacity){
            super(TileEntityThermopneumaticProcessingPlant.this, capacity, 1);
        }
        
        @Override
        public boolean isFluidValid(FluidStack fluid){
            return fluid.isEmpty() || PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes.values().stream().anyMatch(r -> r.getInputFluid().isFluidEqual(fluid));
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            Fluid newFluid = getFluid().getFluid();
            if (prevFluid != newFluid) {
                searchForRecipe = true;
                prevFluid = newFluid;
            }
        }
    }

    private class ThermopneumaticFluidTankOutput extends SmartSyncTank {
        ThermopneumaticFluidTankOutput(int capacity){
            super(TileEntityThermopneumaticProcessingPlant.this, capacity, 2);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            FluidStack res = super.drain(resource, doDrain);
            if (doDrain.execute() && !res.isEmpty()) searchForRecipe = true;
            return res;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            FluidStack res = super.drain(maxDrain, doDrain);
            if (doDrain.execute() && !res.isEmpty()) searchForRecipe = true;
            return res;
        }
    }

    private class ThermopneumaticFluidHandler implements IFluidHandler {
        final FluidTank[] tanks;

        ThermopneumaticFluidHandler() {
            tanks = new FluidTank[]{ inputTank, outputTank };
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
            return outputTank.getFluid().isFluidEqual(resource) ? outputTank.drain(resource.getAmount(), doDrain) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            return outputTank.drain(maxDrain, doDrain);
        }
    }
}
