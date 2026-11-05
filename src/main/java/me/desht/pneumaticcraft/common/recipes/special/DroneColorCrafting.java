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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.function.Predicate;

public class DroneColorCrafting extends PNCSpecialCraftingRecipe {
    private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            stack -> stack.getItem() instanceof DroneItem,
            stack -> DyeColor.getColor(stack) != null
    );

    public DroneColorCrafting(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        List<ItemStack> stacks = ModCraftingHelper.findItems(container, ITEM_PREDICATES);
        ItemStack drone = stacks.get(0).copyWithCount(1);
        DyeColor dyeColor = DyeColor.getColor(stacks.get(1));
        if (drone.isEmpty() || dyeColor == null) {
            return ItemStack.EMPTY;
        }

        drone.set(ModDataComponents.DRONE_COLOR, dyeColor.getId());

        return drone;
    }

    @Override
    protected List<SlotDisplay> displayIngredients() {
        return List.of(
                new SlotDisplay.TagSlotDisplay(Tags.Items.DYES),
                new SlotDisplay.ItemSlotDisplay(ModItems.DRONE.get())
        );
    }

    @Override
    protected SlotDisplay displayResult() {
        return new SlotDisplay.ItemSlotDisplay(ModItems.DRONE.get());
    }

    @Override
    public RecipeSerializer<DroneColorCrafting> getSerializer() {
        return ModRecipeSerializers.DRONE_COLOR_CRAFTING.get();
    }
}
