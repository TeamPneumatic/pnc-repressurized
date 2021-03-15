package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;

public abstract class HeatPropertiesRecipe extends PneumaticCraftRecipe {
    protected HeatPropertiesRecipe(ResourceLocation id) {
        super(id);
    }

    public abstract int getHeatCapacity();

    public abstract int getTemperature();

    public abstract double getThermalResistance();

    public abstract Block getBlock();

    public abstract BlockState getBlockState();

    public abstract BlockState getTransformHot();

    public abstract BlockState getTransformCold();

    public abstract BlockState getTransformHotFlowing();

    public abstract BlockState getTransformColdFlowing();

    public abstract IHeatExchangerLogic getLogic();

    public abstract boolean matchState(BlockState state);

    public abstract Map<String,String> getBlockStatePredicates();

    public ITextComponent getInputDisplayName() {
        if (getBlock() instanceof FlowingFluidBlock) {
            return new FluidStack(((FlowingFluidBlock) getBlock()).getFluid(), 1000).getDisplayName();
        } else {
            return new ItemStack(getBlock()).getDisplayName();
        }
    }
}
