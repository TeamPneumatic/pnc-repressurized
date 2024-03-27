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

package me.desht.pneumaticcraft.common.inventory.slot;

import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UpgradeSlot extends SlotItemHandler {
    private final AbstractPneumaticCraftBlockEntity te;

    public UpgradeSlot(AbstractPneumaticCraftBlockEntity te, int index, int xPosition, int yPosition) {
        super(te.getUpgradeHandler(), index, xPosition, yPosition);
        this.te = te;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public void setChanged() {
        te.getUpgradeCache().invalidateCache();
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return ApplicableUpgradesDB.getInstance().getMaxUpgrades(te, PNCUpgrade.from(stack));
    }
}
