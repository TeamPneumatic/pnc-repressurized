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

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.block.entity.utility.AirCannonBlockEntity;
import me.desht.pneumaticcraft.common.inventory.slot.ItemFilteredSlot;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.List;

public class AirCannonMenu extends AbstractPneumaticCraftMenu<AirCannonBlockEntity> {

    public AirCannonMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public AirCannonMenu(int i, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.AIR_CANNON.get(), i, playerInventory, pos);

        addUpgradeSlots(8, 29);

        // add the gps slot
        addSlot(new ItemFilteredSlot(blockEntity.getItemHandler(), itemStack -> {
            if (!(itemStack.getItem() instanceof IPositionProvider pp)) return false;
            List<BlockPos> l = pp.getStoredPositions(playerInventory.player.getUUID(), itemStack);
            return !l.isEmpty() && l.get(0) != null;
        }, 1, 51, 29));

        // add the cannoned slot.
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 79, 40));

        addPlayerSlots(playerInventory, 84);
    }
}
