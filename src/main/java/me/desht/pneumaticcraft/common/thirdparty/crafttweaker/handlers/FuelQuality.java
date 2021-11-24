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
