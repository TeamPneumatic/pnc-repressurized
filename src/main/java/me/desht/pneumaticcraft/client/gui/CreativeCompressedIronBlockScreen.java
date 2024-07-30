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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.block.entity.heat.CreativeCompressedIronBlockEntity;
import me.desht.pneumaticcraft.common.inventory.CreativeCompressedIronBlockMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CreativeCompressedIronBlockScreen extends AbstractCreativeAdjusterScreen<CreativeCompressedIronBlockMenu, CreativeCompressedIronBlockEntity> {
    private static final Adjustments ADJUSTMENTS = new Adjustments(1f, 10f);

    public CreativeCompressedIronBlockScreen(CreativeCompressedIronBlockMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected float getShiftMultiplier() {
        return 10f;
    }

    @Override
    protected Component formatStringDesc() {
        return Component.literal((te.targetTemperature - 273) + "°C");
    }

    @Override
    protected Adjustments getAdjustments() {
        return ADJUSTMENTS;
    }

    protected String formatAdjustment(float adj) {
        return String.format("%+.0f", adj);
    }
}
