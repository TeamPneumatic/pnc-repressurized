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

import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.network.SyncedField;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public abstract class AbstractForgeEnergyMenu<T extends AbstractPneumaticCraftBlockEntity> extends Abstract4SlotMenu<T> {

    private AbstractForgeEnergyMenu(MenuType type, int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(type, i, playerInventory, getTilePos(buffer));
    }

    public AbstractForgeEnergyMenu(MenuType type, int i, Inventory playerInventory, BlockPos tilePos) {
        super(type, i, playerInventory, tilePos);

        if (!te.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
            throw new IllegalStateException("block entity must support CapabilityEnergy.ENERGY on face null!");
        }
        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> {
            try {
                addSyncedField(new SyncedField.SyncedInt(h, EnergyStorage.class.getDeclaredField("energy")));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        });
    }
}
