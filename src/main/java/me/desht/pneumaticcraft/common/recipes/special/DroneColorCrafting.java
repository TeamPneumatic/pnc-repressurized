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

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.DroneItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

public class DroneColorCrafting extends ShapelessRecipe {
    // you'd think using Ingredient.fromTag(Tags.Items.Dyes) would work, but nope
    private static final Item[] DYES = new Item[DyeColor.values().length];
    static {
        Arrays.setAll(DYES, i -> DyeItem.byColor(DyeColor.values()[i]));
    }

    public DroneColorCrafting(ResourceLocation idIn, CraftingBookCategory category) {
        super(idIn, "", category, new ItemStack(ModItems.DRONE.get()), NonNullList.of(Ingredient.EMPTY,
                    Ingredient.of(DYES), Ingredient.of(ModItems.DRONE.get()))
        );
    }

    private Pair<ItemStack, DyeColor> findItems(CraftingContainer inv) {
        ItemStack drone = ItemStack.EMPTY;
        DyeColor dye = null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof DroneItem) {
                if (!drone.isEmpty()) return null;
                drone = stack.copy();
            } else if (dye == null) {
                DyeColor color = DyeColor.getColor(stack);
                if (color != null) {
                    dye = color;
                }
            } else if (!stack.isEmpty()) {
                return null;
            }
            if (!drone.isEmpty() && dye != null) break;
        }
        return drone.isEmpty() || dye == null ? null : Pair.of(drone, dye);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return findItems(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        Pair<ItemStack, DyeColor> data = findItems(inv);
        if (data == null) return ItemStack.EMPTY;
        ItemStack drone = data.getLeft();
        DyeColor dyeColor = data.getRight();
        if (drone.isEmpty() || dyeColor == null) return ItemStack.EMPTY;

        CompoundTag droneTag = drone.getOrCreateTag();
        droneTag.putInt(DroneEntity.NBT_DRONE_COLOR, dyeColor.getId());
        return drone;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DRONE_COLOR_CRAFTING.get();
    }
}
