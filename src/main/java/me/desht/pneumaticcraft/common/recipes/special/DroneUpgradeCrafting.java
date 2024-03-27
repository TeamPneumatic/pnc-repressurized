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
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public class DroneUpgradeCrafting extends CustomPNCRecipe {
    // you'd think using Ingredient.fromTag(PneumaticCraftTags.Items.BASIC_DRONES) would work, but nope
    private static final Item[] DRONES = {
            ModItems.LOGISTICS_DRONE.get(),
            ModItems.HARVESTING_DRONE.get(),
            ModItems.GUARD_DRONE.get(),
            ModItems.COLLECTOR_DRONE.get(),
    };

    public DroneUpgradeCrafting(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        List<ItemStack> stacks = findItems(container, List.of(
                this::isBasicDrone,
                stack -> stack.getItem() == ModItems.PRINTED_CIRCUIT_BOARD.get()
        ));
        return stacks.size() == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        List<ItemStack> basicDrone = findItems(inv, List.of(this::isBasicDrone));
        if (basicDrone.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack drone = new ItemStack(ModItems.DRONE.get());
        CompoundTag droneTag = basicDrone.get(0).getOrCreateTag();
        drone.setTag(droneTag);
        return drone;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.PRINTED_CIRCUIT_BOARD.get()), Ingredient.of(DRONES));
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267025_) {
        return new ItemStack(ModItems.DRONE.get());
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    private boolean isBasicDrone(ItemStack stack) {
        return stack.getItem() instanceof DroneItem && !((DroneItem) stack.getItem()).canProgram(stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DRONE_UPGRADE_CRAFTING.get();
    }
}
