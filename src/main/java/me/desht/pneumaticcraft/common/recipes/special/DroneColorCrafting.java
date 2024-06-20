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

import me.desht.pneumaticcraft.common.item.DroneItem;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.function.Predicate;

public class DroneColorCrafting extends ShapelessRecipe {
    private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            stack -> stack.getItem() instanceof DroneItem,
            stack -> DyeColor.getColor(stack) != null
    );

    public DroneColorCrafting(CraftingBookCategory category) {
        super("", category, new ItemStack(ModItems.DRONE.get()),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Tags.Items.DYES), Ingredient.of(ModItems.DRONE.get()))
        );
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, HolderLookup.Provider registryAccess) {
        List<ItemStack> stacks = ModCraftingHelper.findItems(container, ITEM_PREDICATES);
        ItemStack drone = stacks.get(0).copy();
        DyeColor dyeColor = DyeColor.getColor(stacks.get(1));
        if (drone.isEmpty() || dyeColor == null) {
            return ItemStack.EMPTY;
        }

        drone.set(ModDataComponents.DRONE_COLOR, dyeColor.getId());

        return drone;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DRONE_COLOR_CRAFTING.get();
    }

}
