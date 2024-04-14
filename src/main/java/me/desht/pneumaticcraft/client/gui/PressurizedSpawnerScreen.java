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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRangeToggleButton;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.spawning.PressurizedSpawnerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.spawning.VacuumTrapBlockEntity;
import me.desht.pneumaticcraft.common.inventory.PressurizedSpawnerMenu;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PressurizedSpawnerScreen extends AbstractPneumaticCraftContainerScreen<PressurizedSpawnerMenu, PressurizedSpawnerBlockEntity> {
    WidgetAnimatedStat infoStat;
    WidgetButtonExtended rangeButton;

    public PressurizedSpawnerScreen(PressurizedSpawnerMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(rangeButton = new WidgetRangeToggleButton(leftPos + 152, topPos + 66, te));

        infoStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.PRESSURIZED_SPAWNER.get()), 0xFF4E4066, false);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        infoStat.setText(ImmutableList.of(
            xlate("pneumaticcraft.gui.tab.status.pressurizedSpawner.spawnRate", te.getSpawnInterval()),
            xlate("pneumaticcraft.gui.tab.status.pressurizedSpawner.airUsage", te.getAirUsage())
        ));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PRESSURIZED_SPAWNER;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + (int)(imageWidth * 0.82), yStart + imageHeight / 4);
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);
        if (te.problem == VacuumTrapBlockEntity.Problems.NO_CORE) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressurized_spawner.no_core"));
        }
    }
}
