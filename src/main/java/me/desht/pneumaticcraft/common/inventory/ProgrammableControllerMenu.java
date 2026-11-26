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

import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammableControllerBlockEntity;
import me.desht.pneumaticcraft.common.inventory.slot.ItemHandlerSlot;
import me.desht.pneumaticcraft.common.network.SyncedField;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

public class ProgrammableControllerMenu extends AbstractPneumaticCraftMenu<ProgrammableControllerBlockEntity> {

    public ProgrammableControllerMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ProgrammableControllerMenu(int i, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.PROGRAMMABLE_CONTROLLER.get(), i, playerInventory, pos);

        addSlot(new ItemHandlerSlot(blockEntity.getItemHandler(), 0, 89, 36));

        addUpgradeSlots(39, 28);

        addPlayerSlots(playerInventory, 84);

        EnergyHandler storage = blockEntity.getEnergyHandler(null);
        if (storage != null) {
            try {
                addSyncedField(new SyncedField.SyncedInt(storage, SimpleEnergyHandler.class.getDeclaredField("energy")));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }
}
