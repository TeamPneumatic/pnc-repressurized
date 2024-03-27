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

import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.DroneItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class DroneColorCrafting extends CustomPNCRecipe {
    // you'd think using Ingredient.fromTag(Tags.Items.Dyes) would work, but nope
    private static final Item[] DYES = new Item[DyeColor.values().length];

    private static final List<Predicate<ItemStack>> ITEM_PREDICATE = List.of(
            stack -> stack.getItem() instanceof DroneItem,
            stack -> DyeColor.getColor(stack) != null
    );

    static {
        Arrays.setAll(DYES, i -> DyeItem.byColor(DyeColor.values()[i]));
    }

    public DroneColorCrafting(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        List<ItemStack> stacks = findItems(container, ITEM_PREDICATE);
        return stacks.size() == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        List<ItemStack> stacks = findItems(container, ITEM_PREDICATE);
        ItemStack drone = stacks.get(0);
        DyeColor dyeColor = DyeColor.getColor(stacks.get(1));
        if (drone.isEmpty() || dyeColor == null) {
            return ItemStack.EMPTY;
        }

        CompoundTag droneTag = drone.getOrCreateTag();
        droneTag.putInt(DroneEntity.NBT_DRONE_COLOR, dyeColor.getId());
        return drone;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267025_) {
        return new ItemStack(ModItems.DRONE.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(DYES), Ingredient.of(ModItems.DRONE.get()));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DRONE_COLOR_CRAFTING.get();
    }

}
