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
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerThermopneumaticProcessingPlant extends
        ContainerPneumaticBase<TileEntityThermopneumaticProcessingPlant> {

    public ContainerThermopneumaticProcessingPlant(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerThermopneumaticProcessingPlant(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), windowId, playerInventory, pos);

        // add upgrade slots
        for (int i = 0; i < 4; i++) {
            addSlot(new SlotUpgrade(te, i, 98 + i * 18, 106));
        }
        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 38, 19));
        addSlot(new SlotOutput(te.getOutputInventory(), 0, 53, 67));

        addPlayerSlots(playerInventory, 130);
    }
}
