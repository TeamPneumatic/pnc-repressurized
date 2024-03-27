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

import me.desht.pneumaticcraft.common.block.entity.PneumaticEnergyStorage;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Predicate;

public class CuriosUtils {
    private static final Pair<String,Integer> NONE = Pair.of("", -1);

    /**
     * Supply Forge Energy from the given energy storage to all Curio items the player has.
     *
     * @param player the player
     * @param energyStorage source energy storage
     * @param maxTransfer max amount to transfer per item
     */
    public static void chargeItems(Player player, PneumaticEnergyStorage energyStorage, int maxTransfer) {

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots() && energyStorage.getEnergyStored() > 0; i++) {
                ItemStack stack = stackHandler.getStacks().getStackInSlot(i);
                IOHelper.getCap(stack, Capabilities.EnergyStorage.ITEM).ifPresent(receivingStorage -> {
                    int energyLeft = energyStorage.getEnergyStored();
                    energyStorage.extractEnergy(
                            receivingStorage.receiveEnergy(Math.min(energyLeft, maxTransfer), false), false
                    );
                });
            }
        }));
    }

    /**
     * Get the curio item in the given curios inventory at the given slot
     * @param player the player
     * @param invId id of the curios inventory in question
     * @param slot slot in the given curios inventory
     * @return stack in that slot
     */
    public static ItemStack getStack(Player player, String invId, int slot) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            ICurioStacksHandler h = handler.getCurios().get(invId);
            return h == null ? ItemStack.EMPTY : h.getStacks().getStackInSlot(slot);
        }).orElse(ItemStack.EMPTY);
    }

    /**
     * Try to find a curio item on the player matching the given predicate
     * @param player the player
     * @param predicate an itemstack matching predicate
     * @return a pair of (inventory id and slot), or null if no match
     */
    public static Pair<String,Integer> findStack(Player player, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            for (Map.Entry<String,ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                for (int i = 0; i < entry.getValue().getSlots(); i++) {
                    if (predicate.test(entry.getValue().getStacks().getStackInSlot(i))) {
                        return Pair.of(entry.getKey(), i);
                    }
                }
            }
            return NONE;
        }).orElse(NONE);
    }

    public static IItemHandler makeCombinedInvWrapper(@Nonnull Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> new CombinedInvWrapper(handler.getCurios().values().stream()
                        .map(ICurioStacksHandler::getStacks)
                        .toArray(IItemHandlerModifiable[]::new))
                ).orElse(new CombinedInvWrapper());

    }
}
