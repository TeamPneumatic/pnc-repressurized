package me.desht.pneumaticcraft.common.recipes.factories.conditions;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class ElectricCompressorCondition implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return () -> ConfigHandler.recipes.enableElectricCompressorRecipe;
    }
}
