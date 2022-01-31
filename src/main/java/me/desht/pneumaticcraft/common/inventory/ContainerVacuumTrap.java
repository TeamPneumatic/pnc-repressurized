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
import me.desht.pneumaticcraft.common.item.ItemSpawnerCore;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerVacuumTrap extends ContainerPneumaticBase<TileEntityVacuumTrap> {
    public ContainerVacuumTrap(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public ContainerVacuumTrap(int windowId, Inventory invPlayer, BlockPos pos) {
        super(ModMenuTypes.VACUUM_TRAP.get(), windowId, invPlayer, pos);

        addSlot(new SlotSpawnerCore(te.getPrimaryInventory(), 0, 62, 38));

        addUpgradeSlots(8, 29);

        addPlayerSlots(invPlayer, 84);
    }

    public static class SlotSpawnerCore extends SlotItemHandler {
        public SlotSpawnerCore(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return stack.getItem() instanceof ItemSpawnerCore;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
