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

package me.desht.pneumaticcraft.datagen.recipe;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class Criteria {
    private static InventoryChangeTrigger.TriggerInstance hasItem(net.minecraft.world.level.ItemLike itemIn) {
        return hasItem(ItemPredicate.Builder.item().of(itemIn).build());
    }

//    private static InventoryChangeTrigger.TriggerInstance hasItem(SetTag<Item> tagIn) {
//        return hasItem(ItemPredicate.Builder.item().of(tagIn).build());
//    }

    private static InventoryChangeTrigger.TriggerInstance hasItem(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }

    public static RecipeCriterion has(ItemLike provider) {
        return RecipeCriterion.of(PneumaticCraftUtils.getRegistryName(provider.asItem()).orElseThrow().getPath(), hasItem(provider.asItem()));
    }

    public static RecipeCriterion has(Ingredient ingredient) {
        Item item = ingredient.getItems()[0].getItem();
        return RecipeCriterion.of(PneumaticCraftUtils.getRegistryName(item).orElseThrow().getPath(), hasItem(item));
    }

    public static class RecipeCriterion {

        public final String name;
        public final CriterionTriggerInstance criterion;

        private RecipeCriterion(String name, CriterionTriggerInstance criterion) {
            this.name = name;
            this.criterion = criterion;
        }

        public static RecipeCriterion of(String name, CriterionTriggerInstance criterion) {
            return new RecipeCriterion(name, criterion);
        }
    }
}
