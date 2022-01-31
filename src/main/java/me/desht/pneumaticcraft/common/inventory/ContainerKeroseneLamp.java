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
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerKeroseneLamp extends ContainerPneumaticBase<TileEntityKeroseneLamp> {

    public ContainerKeroseneLamp(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerKeroseneLamp(int i, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.KEROSENE_LAMP.get(), i, playerInventory, pos);

        addSlot(new SlotFluidContainer(te.getPrimaryInventory(), 0, 132, 22));
        addSlot(new SlotOutput(te.getPrimaryInventory(), 1, 132, 55));

        addPlayerSlots(playerInventory, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return te.isGuiUseableByPlayer(player);
    }
}
