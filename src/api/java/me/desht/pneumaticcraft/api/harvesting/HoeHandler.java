/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.harvesting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defines a hoe handler; use this to register items that are not vanilla-tyle hoes (i.e. do not extend
 * {@link net.minecraft.item.HoeItem}) as a valid tool for Harvesting Drones to use.
 * <p>
 * Hoe handlers are Forge registry objects and should be registered as such.
 */
public class HoeHandler extends ForgeRegistryEntry<HoeHandler> implements Predicate<ItemStack> {
    private final Predicate<ItemStack> matchItem;
    private final BiConsumer<ItemStack, PlayerEntity> useDurability;

    public HoeHandler(Predicate<ItemStack> matchItem, BiConsumer<ItemStack, PlayerEntity> useDurability) {
        this.matchItem = matchItem;
        this.useDurability = useDurability;
    }

    @Override
    public boolean test(ItemStack stack) {
        return matchItem.test(stack);
    }

    public Consumer<PlayerEntity> getConsumer(ItemStack stack) {
        return player -> useDurability.accept(stack, player);
    }

    /**
     * Default implementation for vanilla-compatible hoes.
     */
    public static class DefaultHoeHandler extends HoeHandler {
        public DefaultHoeHandler() {
            super(stack -> stack.getItem() instanceof HoeItem, (stack, player) -> stack.hurtAndBreak(1, player, p -> { }));
        }
    }
}
