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

import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class OneProbeCrafting extends CustomPNCRecipe {
    protected static final List<Predicate<ItemStack>> ITEM_PREDICATE = List.of(
            stack -> stack.getItem() == ModItems.PNEUMATIC_HELMET.get(),
            stack -> stack.getItem() == probe() && !stack.isEmpty()
    );
    private static Item theOneProbe = null;

    private static final String ONE_PROBE_TAG = "theoneprobe";

    public OneProbeCrafting(CraftingBookCategory category) {
        super(category);
    }

    private static Item probe() {
        if (theOneProbe == null) {
            theOneProbe = BuiltInRegistries.ITEM.get(new ResourceLocation("theoneprobe:probe"));
        }
        return theOneProbe;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return findItems(inv, ITEM_PREDICATE).size() == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        List<ItemStack> stacks = findItems(inv, ITEM_PREDICATE);
        return setOneProbeEnabled(stacks.get(0).copy());
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ONE_PROBE_HELMET_CRAFTING.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return setOneProbeEnabled(new ItemStack(ModItems.PNEUMATIC_HELMET.get()));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.PNEUMATIC_HELMET.get()), Ingredient.of(probe()));
    }

    public static boolean isOneProbeEnabled(ItemStack helmetStack) {
        return helmetStack.hasTag() && helmetStack.getTag().getInt(ONE_PROBE_TAG) > 0;
    }

    private static ItemStack setOneProbeEnabled(ItemStack helmetStack) {
        helmetStack.getOrCreateTag().putInt(ONE_PROBE_TAG, 1);
        return helmetStack;
    }
}
