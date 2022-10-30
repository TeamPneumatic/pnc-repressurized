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
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipeTypes;
import me.desht.pneumaticcraft.common.recipes.other.HeatPropertiesRecipeImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Collections;
import java.util.Map;

@Document("mods/PneumaticCraft/BlockHeatProperties")
@ZenRegister
@ZenCodeType.Name("mods.pneumaticcraft.BlockHeatProperties")
public class BlockHeatProperties implements IRecipeManager<HeatPropertiesRecipe> {
    @ZenCodeType.Method
    public void addRecipe(String name, Block block, int temperature, double thermalResistance) {
        addRecipe(name, block, Collections.emptyMap(), temperature, thermalResistance);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, Block block, Map<String,String> matchProps, int temperature, double thermalResistance) {
        if (matchProps.isEmpty()) {
            CraftTweakerAPI.apply(new ActionAddRecipe<>(this,
                    new HeatPropertiesRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                            block, temperature, thermalResistance)
            ));
        } else {
            CraftTweakerAPI.apply(new ActionAddRecipe<>(this,
                    new HeatPropertiesRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                            block,
                            null, null, null, null,
                            0, temperature, thermalResistance, matchProps, "")
            ));
        }
    }

    @ZenCodeType.Method
    public void addRecipe(String name, Block block, Map<String,String> matchProps,
                          int temperature, double thermalResistance, int heatCapacity,
                          @ZenCodeType.Nullable BlockState transformHot, @ZenCodeType.Nullable BlockState transformHotFlowing,
                          @ZenCodeType.Nullable BlockState transformCold, @ZenCodeType.Nullable BlockState transformColdFlowing,
                          @ZenCodeType.OptionalString String descriptionKey) {
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this,
                new HeatPropertiesRecipeImpl(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                        block,
                        transformHot, transformHotFlowing,
                        transformCold, transformColdFlowing,
                        heatCapacity, temperature, thermalResistance,
                        matchProps,
                        descriptionKey)
        ));
    }

    @Override
    public RecipeType<HeatPropertiesRecipe> getRecipeType() {
        return ModRecipeTypes.BLOCK_HEAT_PROPERTIES.get();
    }
}
