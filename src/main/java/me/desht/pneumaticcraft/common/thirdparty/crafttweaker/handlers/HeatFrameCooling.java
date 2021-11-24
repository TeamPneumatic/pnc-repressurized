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
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/HeatFrameCooling")
@ZenCodeType.Name("mods.pneumaticcraft.heatframecooling")
@ZenRegister
public class HeatFrameCooling implements IRecipeManager {
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredient input, IItemStack output, int temperature, @ZenCodeType.OptionalFloat float bonusMult, @ZenCodeType.OptionalFloat float bonusLimit) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new HeatFrameCoolingRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        input.asVanillaIngredient(),
                        temperature,
                        output.getInternal(),
                        bonusMult, bonusLimit)
        ));
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient inputFluid, IItemStack output, int temperature, @ZenCodeType.OptionalFloat float bonusMult, @ZenCodeType.OptionalFloat float bonusLimit) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                new HeatFrameCoolingRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        CTUtils.toFluidIngredient(inputFluid),
                        temperature,
                        output.getInternal(),
                        bonusMult, bonusLimit)
        ));
    }

    @Override
    public IRecipeType<HeatFrameCoolingRecipe> getRecipeType() {
        return PneumaticCraftRecipeType.HEAT_FRAME_COOLING;
    }
}
