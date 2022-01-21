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
import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;

public class OneProbeCrafting extends ShapelessRecipe {
    @ObjectHolder("theoneprobe:probe")
    public static final Item ONE_PROBE = null;

    private static final String ONE_PROBE_TAG = "theoneprobe";

    public OneProbeCrafting(ResourceLocation idIn) {
        super(idIn, "", makeOutputStack(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.PNEUMATIC_HELMET.get()), Ingredient.of(ONE_PROBE)));
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        if (ONE_PROBE == null) return false;

        boolean probeFound = false, helmetFound = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            Item item = inv.getItem(i).getItem();
            if (item == ModItems.PNEUMATIC_HELMET.get()) {
                if (helmetFound || isOneProbeEnabled(inv.getItem(i))) return false;
                helmetFound = true;
            } else if (item == ONE_PROBE) {
                if (probeFound) return false;
                probeFound = true;
            } else if (item != Items.AIR) {
                return false;
            }
        }
        return probeFound && helmetFound;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack helmet = findHelmet(inv);
        if (helmet.isEmpty()) return ItemStack.EMPTY;
        ItemStack output = helmet.copy();
        setOneProbeEnabled(output);
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ONE_PROBE_HELMET_CRAFTING.get();
    }

    private ItemStack findHelmet(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).getItem() == ModItems.PNEUMATIC_HELMET.get()) {
                return inv.getItem(i).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack makeOutputStack() {
        ItemStack stack = new ItemStack(ModItems.PNEUMATIC_HELMET.get());
        setOneProbeEnabled(stack);
        return stack;
    }

    public static boolean isOneProbeEnabled(ItemStack helmetStack) {
        return helmetStack.hasTag() && helmetStack.getTag().getInt(ONE_PROBE_TAG) > 0;
    }

    private static void setOneProbeEnabled(ItemStack helmetStack) {
        helmetStack.getOrCreateTag().putInt(ONE_PROBE_TAG, 1);
    }
}
