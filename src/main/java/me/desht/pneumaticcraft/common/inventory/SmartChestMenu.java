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

import me.desht.pneumaticcraft.common.block.entity.utility.SmartChestBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SmartChestMenu extends AbstractPneumaticCraftMenu<SmartChestBlockEntity> {
    public static final int N_COLS = 12;

    public SmartChestMenu(int windowId, Inventory inv, BlockPos pos) {
        super(ModMenuTypes.SMART_CHEST.get(), windowId, inv, pos);

        for (int i = 0; i < SmartChestBlockEntity.CHEST_SIZE; i++) {
            addSlot(new SlotItemHandler(blockEntity.getItemHandler(), i, 8 + (i % N_COLS) * 18, 18 + (i / N_COLS) * 18));
        }

        addUpgradeSlots(187, 148);

        addPlayerSlots(inv, 130);
    }

    public SmartChestMenu(int windowId, Inventory inv, FriendlyByteBuf buffer) {
        this(windowId, inv, getTilePos(buffer));
    }
}
