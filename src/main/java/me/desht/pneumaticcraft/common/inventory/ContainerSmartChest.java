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

import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSmartChest extends AbstractPneumaticCraftMenu<TileEntitySmartChest> {
    public static final int N_COLS = 12;

    public ContainerSmartChest(int windowId, Inventory inv, BlockPos pos) {
        super(ModMenuTypes.SMART_CHEST.get(), windowId, inv, pos);

        for (int i = 0; i < TileEntitySmartChest.CHEST_SIZE; i++) {
            addSlot(new SlotItemHandler(te.getPrimaryInventory(), i, 8 + (i % N_COLS) * 18, 18 + (i / N_COLS) * 18));
        }

        addUpgradeSlots(187, 148);

        addPlayerSlots(inv, 130);
    }

    public ContainerSmartChest(int windowId, Inventory inv, FriendlyByteBuf buffer) {
        this(windowId, inv, getTilePos(buffer));
    }
}
