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
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.PressureChamberInterfaceMenu;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.MAX_PROGRESS;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPressureChamberInterface extends GuiPneumaticContainerBase<PressureChamberInterfaceMenu,TileEntityPressureChamberInterface> {
    private WidgetAnimatedStat statusStat;
    private WidgetButtonExtended exportAnyButton;
    private WidgetLabel exportTypeLabel;

    private boolean hasEnoughPressure = true;

    public GuiPressureChamberInterface(PressureChamberInterfaceMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.pressureChamberInterface.status"), new ItemStack(ModBlocks.PRESSURE_CHAMBER_INTERFACE.get()), 0xFFFFAA00, false);

        exportAnyButton = addRenderableWidget(new WidgetButtonExtended(leftPos + 111, topPos + 32, 60, 20, TextComponent.EMPTY)
                .withTag("export_mode"));
        exportTypeLabel = addRenderableWidget(new WidgetLabel(leftPos + 111, topPos + 20, xlate("pneumaticcraft.gui.pressureChamberInterface.exportLabel")));
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
        super.renderLabels(matrixStack, x, y);

        int inputShift = (int) ((1F - (float) Math.cos(te.inputProgress / (float) MAX_PROGRESS * Math.PI)) * 11);
        int outputShift = (int) ((1F - (float) Math.cos(te.outputProgress / (float) MAX_PROGRESS * Math.PI)) * 11);
        fill(matrixStack, 63 + inputShift, 30, 87 + inputShift, 32, 0xFF5A62FF);
        fill(matrixStack, 63 + outputShift, 54, 87 + outputShift, 56, 0xFFFFA800);

    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -2);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PRESSURE_CHAMBER_INTERFACE;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        boolean exporting = te.interfaceMode == TileEntityPressureChamberInterface.InterfaceDirection.EXPORT;
        exportAnyButton.setVisible(exporting);
        exportAnyButton.visible = exporting;
        exportTypeLabel.visible = exporting;
        if (exportAnyButton.visible) {
            String textKey = "pneumaticcraft.gui.pressureChamberInterface.export." + (te.exportAny ? "any" : "valid");
            exportAnyButton.setMessage(xlate(textKey));
            exportAnyButton.setTooltipKey(textKey + ".tooltip");
        }

        statusStat.setText(ImmutableList.of(
                xlate("pneumaticcraft.gui.pressureChamberInterface.mode").withStyle(ChatFormatting.WHITE),
                xlate(te.interfaceMode.getTranslationKey()).withStyle(ChatFormatting.BLACK)
        ));

        if (hasEnoughPressure && !te.hasEnoughPressure()) {
            hasEnoughPressure = false;
            problemTab.openStat();
        } else if (te.hasEnoughPressure()) {
            hasEnoughPressure = true;
        }
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (te.interfaceMode == TileEntityPressureChamberInterface.InterfaceDirection.NONE) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber_interface.not_formed"));
        } else if (!te.hasEnoughPressure()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber_interface.not_enough_pressure"));
        }
    }
}
