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

import me.desht.pneumaticcraft.common.item.minigun.StandardGunAmmoItem;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class GunAmmoPotionCrafting extends PNCSpecialCraftingRecipe {
    private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
            stack -> stack.getItem() instanceof PotionItem,
            stack -> stack.getItem() instanceof StandardGunAmmoItem
    );

    public GunAmmoPotionCrafting(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        List<ItemStack> stacks = ModCraftingHelper.findItems(inv, ITEM_PREDICATES);
        if (stacks.size() == 2) {
            ItemStack potion = stacks.get(0);
            ItemStack ammo = stacks.get(1).copy();
            if (!ammo.isEmpty() && !potion.isEmpty()) {
                StandardGunAmmoItem.setPotion(ammo, potion);
                return ammo;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected List<SlotDisplay> displayIngredients() {
        return List.of(
                new SlotDisplay.ItemSlotDisplay(ModItems.GUN_AMMO.get()),
                new SlotDisplay.Composite(List.of(
                        new SlotDisplay.ItemSlotDisplay(Items.POTION),
                        new SlotDisplay.ItemSlotDisplay(Items.SPLASH_POTION),
                        new SlotDisplay.ItemSlotDisplay(Items.LINGERING_POTION)
                ))
        );
    }

    @Override
    protected SlotDisplay displayResult() {
        return new SlotDisplay.ItemSlotDisplay(ModItems.GUN_AMMO.get());
    }

    @Override
    public RecipeSerializer<GunAmmoPotionCrafting> getSerializer() {
        return ModRecipeSerializers.GUN_AMMO_POTION_CRAFTING.get();
    }
}
