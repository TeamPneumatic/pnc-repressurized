package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.FuelQualityRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.other.FuelQualityRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/FuelQuality")
@ZenCodeType.Name("mods.pneumaticcraft.fuelquality")
@ZenRegister
public class FuelQuality implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient ingredient, int airPerBucket, @ZenCodeType.OptionalFloat(1f) float burnRate) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new FuelQualityRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toFluidIngredient(ingredient),
                        airPerBucket,
                        burnRate)
        ));
    }

    @Override
    public IRecipeType<FuelQualityRecipe> getRecipeType() {
        return PneumaticCraftRecipeType.FUEL_QUALITY;
    }
}
