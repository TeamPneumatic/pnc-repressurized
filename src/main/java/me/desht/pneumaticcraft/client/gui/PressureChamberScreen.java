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
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.PressureChamberValveBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.PressureChamberValveMenu;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PressureChamberScreen extends AbstractPneumaticCraftContainerScreen<PressureChamberValveMenu,PressureChamberValveBlockEntity> {
    public PressureChamberScreen(PressureChamberValveMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        int sOut = te.multiBlockSize;
        int sIn = te.multiBlockSize - 2;
        addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()), 0xFFFFAA00, false)
                .setText(ImmutableList.of(
                        xlate("pneumaticcraft.gui.tab.pressureChamber.chamberSize").withStyle(ChatFormatting.WHITE),
                        Component.literal( sOut + "x" + sOut + "x" + sOut + " (outside)").withStyle(ChatFormatting.BLACK),
                        Component.literal( sIn + "x" + sIn + "x" + sIn + " (inside)").withStyle(ChatFormatting.BLACK)
                ));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);

        Component title = xlate("pneumaticcraft.gui.pressureChamberTitle", te.multiBlockSize + "x" + te.multiBlockSize + "x" + te.multiBlockSize);
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    protected PointXY getInvNameOffset() {
        return null;
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);
        if (!te.isValidRecipeInChamber) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.no_recipe"));
        }
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        if (te.isValidRecipeInChamber && !te.isSufficientPressureInChamber) {
            if (te.recipePressure > 0F) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.not_enough_pressure"));
            } else {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.too_much_pressure"));
            }
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.required_pressure", te.recipePressure));
        }
    }
}
