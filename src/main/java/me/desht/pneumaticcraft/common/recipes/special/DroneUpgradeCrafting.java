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

package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.item.DroneItem;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class DroneUpgradeCrafting extends ShapelessRecipe {
    public static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            DroneItem::isBasicDrone,
            stack -> stack.getItem() == ModItems.PRINTED_CIRCUIT_BOARD.get()
    );

    public DroneUpgradeCrafting(CraftingBookCategory category) {
        super("", category, new ItemStack(ModItems.DRONE.get()), NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ModItems.PRINTED_CIRCUIT_BOARD.get()), Ingredient.of(PneumaticCraftTags.Items.BASIC_DRONES))
        );
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
        List<ItemStack> items = ModCraftingHelper.findItems(inv, ITEM_PREDICATES);
        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return Util.make(new ItemStack(ModItems.DRONE.get()),
                drone -> drone.applyComponents(items.getFirst().getComponents()));
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DRONE_UPGRADE_CRAFTING.get();
    }

}
