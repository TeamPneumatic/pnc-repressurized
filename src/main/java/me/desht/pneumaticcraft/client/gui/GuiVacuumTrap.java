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

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRangeToggleButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumTrap;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class GuiVacuumTrap extends GuiPneumaticContainerBase<ContainerVacuumTrap, TileEntityVacuumTrap> {
    WidgetButtonExtended rangeButton;

    public GuiVacuumTrap(ContainerVacuumTrap container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new WidgetTank(leftPos + 98, topPos + 14, te.getFluidTank()));

        addRenderableWidget(rangeButton = new WidgetRangeToggleButton(leftPos + 152, topPos + 66, te));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_VACUUM_TRAP;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + (int)(imageWidth * 0.82), yStart + imageHeight / 4 - 2);
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);
        if (te.problem == TileEntityVacuumTrap.Problems.NO_CORE || te.problem == TileEntityVacuumTrap.Problems.CORE_FULL) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.problem.getTranslationKey()));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);
        if (te.problem == TileEntityVacuumTrap.Problems.TRAP_CLOSED) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.problem.getTranslationKey()));
        }
        if (te.getFluidTank().getFluidAmount() < TileEntityVacuumTrap.MEMORY_ESSENCE_AMOUNT) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.vacuum_trap.no_memory_essence"));
        }
    }
}
