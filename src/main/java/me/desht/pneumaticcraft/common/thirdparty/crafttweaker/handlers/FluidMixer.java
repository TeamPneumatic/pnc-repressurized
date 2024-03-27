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
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.common.recipes.machine.FluidMixerRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@Document("mods/PneumaticCraft/FluidMixer")
@ZenCodeType.Name("mods.pneumaticcraft.FluidMixer")
@ZenRegister
public class FluidMixer implements IRecipeManager<FluidMixerRecipe> {
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient input1, CTFluidIngredient input2, IFluidStack outputFluid, IItemStack outputItem, float pressure, int processingTime) {
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this,
                new RecipeHolder<>(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                new FluidMixerRecipeImpl(
                        CTUtils.toFluidIngredient(input1),
                        CTUtils.toFluidIngredient(input2),
                        outputFluid.getImmutableInternal(),
                        outputItem.getImmutableInternal(),
                        pressure,
                        processingTime)
                )
        ));
    }

    @Override
    public RecipeType<FluidMixerRecipe> getRecipeType() {
        return ModRecipeTypes.FLUID_MIXER.get();
    }
}
