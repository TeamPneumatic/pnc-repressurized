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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defines a hoe handler; use this to register items that are not vanilla-style hoes (i.e. do not extend
 * {@link HoeItem}) as a valid tool for Harvesting Drones to use.
 * <p>
 * Hoe handlers are registry objects and should be registered as such.
 */
public class HoeHandler implements Predicate<ItemStack> {
    private final Predicate<ItemStack> matchItem;
    private final BiConsumer<ItemStack, Player> useDurability;

    public HoeHandler(Predicate<ItemStack> matchItem, BiConsumer<ItemStack, Player> useDurability) {
        this.matchItem = matchItem;
        this.useDurability = useDurability;
    }

    @Override
    public boolean test(ItemStack stack) {
        return matchItem.test(stack);
    }

    public Consumer<Player> getConsumer(ItemStack stack) {
        return player -> useDurability.accept(stack, player);
    }

    /**
     * Default implementation for vanilla-compatible hoes.
     */
    public static class DefaultHoeHandler extends HoeHandler {
        public DefaultHoeHandler() {
            super(stack -> stack.getItem() instanceof HoeItem, (stack, player) -> stack.hurtAndBreak(1, (ServerLevel) player.level(), player, item -> { }));
        }
    }
}
