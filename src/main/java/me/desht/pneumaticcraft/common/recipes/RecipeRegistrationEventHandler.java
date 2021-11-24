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

package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.NoNBTIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeRegistrationEventHandler {
    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        // register our custom recipe and ingredient types

        PneumaticCraftRecipeType.registerRecipeTypes(event.getRegistry());

        CraftingHelper.register(FluidTagPresentCondition.Serializer.INSTANCE);

        CraftingHelper.register(StackedIngredient.Serializer.ID, StackedIngredient.Serializer.INSTANCE);
        CraftingHelper.register(FluidIngredient.Serializer.ID, FluidIngredient.Serializer.INSTANCE);
        CraftingHelper.register(NoNBTIngredient.Serializer.ID, NoNBTIngredient.Serializer.INSTANCE);
    }
}
