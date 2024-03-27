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

package me.desht.pneumaticcraft.common.thirdparty.curios;

import me.desht.pneumaticcraft.common.item.MemoryStickItem;
import me.desht.pneumaticcraft.common.item.MemoryStickItem.MemoryStickLocator;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Allows us to intercept ticks for memory stick(s) in a Curios inventory handler
 * Need to do this to cache what memory sticks players have on them for efficient
 * XP orb absorption.
 */
public class CuriosTickerCapability {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new MemoryCurio(stack), ModItems.MEMORY_STICK.get());
    }

    private record MemoryCurio(ItemStack stack) implements ICurio {
        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public void curioTick(SlotContext slotContext) {
            if (MemoryStickItem.shouldAbsorbXPOrbs(stack) && slotContext.entity() instanceof Player player) {
                MemoryStickItem.cacheMemoryStickLocation(player, MemoryStickLocator.namedInv(slotContext.identifier(), slotContext.index()));
            }
            ICurio.super.curioTick(slotContext);
        }
    }
}
