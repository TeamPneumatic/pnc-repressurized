package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.ThermoPlantRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/ThermopneumaticProcessingPlant")
@ZenCodeType.Name("mods.pneumaticcraft.thermopneumaticprocessingplant")
@ZenRegister
public class ThermopneumaticProcessingPlant implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient inputFluid, IIngredient inputItem, IFluidStack outputFluid, IItemStack outputItem, float pressure, int minTemp, @ZenCodeType.OptionalInt(Integer.MAX_VALUE) int maxTemp, @ZenCodeType.OptionalFloat(1f) float recipeSpeed, @ZenCodeType.OptionalBoolean() boolean exothermic) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new ThermoPlantRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toFluidIngredient(inputFluid),
                        inputItem.asVanillaIngredient(),
                        outputFluid.getImmutableInternal(),
                        outputItem.getImmutableInternal(),
                        TemperatureRange.of(minTemp, maxTemp),
                        pressure,
                        recipeSpeed,
                        exothermic)
        ));
    }

    @Override
    public IRecipeType<ThermoPlantRecipe> getRecipeType() {
        return PneumaticCraftRecipeType.THERMO_PLANT;
    }
}
