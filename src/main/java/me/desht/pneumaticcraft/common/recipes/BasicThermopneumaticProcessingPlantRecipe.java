package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import me.desht.pneumaticcraft.api.recipe.TemperatureRange;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BasicThermopneumaticProcessingPlantRecipe implements IThermopneumaticProcessingPlantRecipe {
	private final ResourceLocation id;
    private final FluidStack inputFluid, outputFluid;
    private final Ingredient inputItem;
    private final float requiredPressure;
    private final boolean exothermic;
    private final TemperatureRange operatingTemperature;

    public BasicThermopneumaticProcessingPlantRecipe(ResourceLocation id, @Nonnull FluidStack inputFluid, @Nullable Ingredient inputItem,
                                                     FluidStack outputFluid, TemperatureRange operatingTemperature, float requiredPressure, boolean exothermic) {
        this.id = id;
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.exothermic = exothermic;
    }

    @Override
    public boolean matches(FluidStack fluidStack, @Nonnull ItemStack stack) {
        return (inputFluid.isEmpty() || (fluidStack.isFluidEqual(inputFluid) && fluidStack.getAmount() >= inputFluid.getAmount()))
                && (inputItem == null || inputItem.test(stack));
    }

    @Override
    public TemperatureRange getOperatingTemperature() {
        return operatingTemperature;
    }

    @Override
    public float getRequiredPressure() {
        return requiredPressure;
    }

    @Override
    public FluidStack getInputFluid() {
        return inputFluid;
    }

    @Nonnull
    @Override
    public Ingredient getInputItem() {
        return inputItem;
    }

    @Override
    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    @Override
    public boolean isExothermic() {
        return exothermic;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeResourceLocation(id);
        buf.writeVarInt(operatingTemperature.getMin());
        buf.writeVarInt(operatingTemperature.getMax());
        buf.writeFloat(requiredPressure);
        inputItem.write(buf);
        inputFluid.writeToPacket(buf);
        outputFluid.writeToPacket(buf);
        buf.writeBoolean(exothermic);
    }
}
