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

import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.block.entity.AbstractFluidTankBlockEntity;
import me.desht.pneumaticcraft.common.inventory.FluidTankMenu;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidTankScreen extends AbstractPneumaticCraftContainerScreen<FluidTankMenu, AbstractFluidTankBlockEntity> {
    public FluidTankScreen(FluidTankMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new WidgetTank(leftPos + 152, topPos + 15, te.getTank()));
    }

    @Override
    protected String upgradeCategory() {
        return "fluid_tank";
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_FLUID_TANK;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }
}
