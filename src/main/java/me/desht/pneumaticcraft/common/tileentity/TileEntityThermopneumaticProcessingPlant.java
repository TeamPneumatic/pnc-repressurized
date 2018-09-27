package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.BasicThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityThermopneumaticProcessingPlant extends TileEntityPneumaticBase
        implements IHeatExchanger, IMinWorkingPressure, IRedstoneControlled, ISerializableTanks, ISmartFluidSync  {

    private static final int INVENTORY_SIZE = 1;
    private static final int CRAFTING_TIME = 60;

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
    public double requiredTemperature;
    @SuppressWarnings("unused")
    @DescSynced
    private int inputAmountScaled, outputAmountScaled;

    private final ItemStackHandler handler = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
        
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
            if (stack.isEmpty() || BasicThermopneumaticProcessingPlantRecipe.recipes.stream().noneMatch(r -> r.isValidInput(stack))) return stack;
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final ThermopneumaticFluidHandler fluidHandler = new ThermopneumaticFluidHandler();

    public TileEntityThermopneumaticProcessingPlant() {
        super(5, 7, 3000, 4);
        addApplicableUpgrade(EnumUpgrade.DISPENSER);
        heatExchanger.setThermalResistance(10);
    }

    @Override
    public boolean isConnectedTo(EnumFacing dir) {
        return getRotation().getOpposite() != dir && dir != EnumFacing.UP;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            IThermopneumaticProcessingPlantRecipe recipe = getValidRecipe();
            hasRecipe = recipe != null;
            if (hasRecipe) {
                ItemStack stackInSlot = handler.getStackInSlot(0);
                requiredPressure = recipe.getRequiredPressure(inputTank.getFluid(), stackInSlot);
                requiredTemperature = recipe.getRequiredTemperature(inputTank.getFluid(), stackInSlot);
                if (redstoneAllows() && heatExchanger.getTemperature() >= requiredTemperature && getPressure() >= getMinWorkingPressure()) {
                    craftingProgress++;
                    if (craftingProgress >= CRAFTING_TIME) {
                        outputTank.fill(recipe.getRecipeOutput(inputTank.getFluid(), stackInSlot).copy(), true);
                        recipe.useRecipeItems(inputTank.getFluid(), stackInSlot);
                        handler.setStackInSlot(0, stackInSlot);
                        addAir(-recipe.airUsed(inputTank.getFluid(), stackInSlot));
                        heatExchanger.addHeat(-recipe.heatUsed(inputTank.getFluid(), stackInSlot));
                        if (inputTank.getFluid() != null && inputTank.getFluid().amount <= 0) inputTank.setFluid(null);
                        craftingProgress = 0;
                    }
                }
            } else {
                craftingProgress = 0;
                requiredTemperature = 273;
                requiredPressure = 0;
            }
            if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
                autoExportLiquid();
            }
        }
    }

    private IThermopneumaticProcessingPlantRecipe getValidRecipe() {
        for (IThermopneumaticProcessingPlantRecipe recipe : BasicThermopneumaticProcessingPlantRecipe.recipes) {
            if (recipe.isValidRecipe(inputTank.getFluid(), handler.getStackInSlot(0))) {
                if (outputTank.getFluid() == null) {
                    return recipe;
                } else {
                    FluidStack output = recipe.getRecipeOutput(inputTank.getFluid(), handler.getStackInSlot(0));
                    if (output.getFluid() == outputTank.getFluid().getFluid() && output.amount <= outputTank.getCapacity() - outputTank.getFluidAmount()) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return handler;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        }
        return super.getCapability(capability, facing);
    }

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    @SideOnly(Side.CLIENT)
    public double getCraftingPercentage() {
        return (double) craftingProgress / CRAFTING_TIME;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setTag("Items", handler.serializeNBT());
        tag.setByte("redstoneMode", (byte) redstoneMode);
        tag.setInteger("craftingProgress", craftingProgress);

        NBTTagCompound tankTag = new NBTTagCompound();
        inputTank.writeToNBT(tankTag);
        tag.setTag("inputTank", tankTag);

        tankTag = new NBTTagCompound();
        outputTank.writeToNBT(tankTag);
        tag.setTag("outputTank", tankTag);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        handler.deserializeNBT(tag.getCompoundTag("Items"));
        redstoneMode = tag.getByte("redstoneMode");
        craftingProgress = tag.getInteger("craftingProgress");
        inputTank.readFromNBT(tag.getCompoundTag("inputTank"));
        inputAmountScaled = inputTank.getScaledFluidAmount();
        outputTank.readFromNBT(tag.getCompoundTag("outputTank"));
        outputAmountScaled = outputTank.getScaledFluidAmount();
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return heatExchanger;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        } else if (buttonID == 1) {
            inputTank.drain(inputTank.getCapacity(), true);
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure() {
        return requiredPressure;
    }

    @Override
    public String getName() {
        return Blockss.THERMOPNEUMATIC_PROCESSING_PLANT.getTranslationKey();
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

    private class ThermopneumaticFluidTankInput extends SmartSyncTank {

        ThermopneumaticFluidTankInput(int capacity){
            super(TileEntityThermopneumaticProcessingPlant.this, capacity, 1);
        }
        
        @Override
        public boolean canFillFluidType(FluidStack fluid){
            return fluid == null || BasicThermopneumaticProcessingPlantRecipe.recipes.stream().anyMatch(r -> r.isValidInput(fluid));
        }
    }

    private class ThermopneumaticFluidTankOutput extends SmartSyncTank {

        ThermopneumaticFluidTankOutput(int capacity){
            super(TileEntityThermopneumaticProcessingPlant.this, capacity, 2);
        }
    }

    private class ThermopneumaticFluidHandler implements IFluidHandler {
        @Override
        public IFluidTankProperties[] getTankProperties() {
            return ArrayUtils.addAll(inputTank.getTankProperties(), outputTank.getTankProperties());
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return inputTank.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return outputTank.getFluid() != null && outputTank.getFluid().isFluidEqual(resource) ? outputTank.drain(resource.amount, doDrain) : null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return outputTank.drain(maxDrain, doDrain);
        }
    }
}
