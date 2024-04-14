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

package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.block.entity.hopper.OmnidirectionalHopperBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;

public class OmnidirectionalHopperMenu extends AbstractPneumaticCraftMenu<OmnidirectionalHopperBlockEntity> {

    public OmnidirectionalHopperMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public OmnidirectionalHopperMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.OMNIDIRECTIONAL_HOPPER.get(), windowId, playerInventory, pos);

        addUpgradeSlots(23, 29);

        for (int i = 0; i < OmnidirectionalHopperBlockEntity.INVENTORY_SIZE; i++) {
            addSlot(new SlotItemHandler(blockEntity.getItemHandler(), i, 68 + i * 18, 36));
        }

        addPlayerSlots(playerInventory, 84);
    }
}
