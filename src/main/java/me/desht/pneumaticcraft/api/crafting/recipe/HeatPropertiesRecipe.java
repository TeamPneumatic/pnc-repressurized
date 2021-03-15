package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

public abstract class HeatPropertiesRecipe extends PneumaticCraftRecipe {
    protected HeatPropertiesRecipe(ResourceLocation id) {
        super(id);
    }

    public abstract int getHeatCapacity();

    public abstract int getTemperature();

    public abstract double getThermalResistance();

    public abstract Block getBlock();

    public abstract BlockState getTransformHot();

    public abstract BlockState getTransformCold();

    public abstract BlockState getTransformHotFlowing();

    public abstract BlockState getTransformColdFlowing();

    public abstract IHeatExchangerLogic getLogic();

    public abstract boolean matchState(BlockState state);
}
