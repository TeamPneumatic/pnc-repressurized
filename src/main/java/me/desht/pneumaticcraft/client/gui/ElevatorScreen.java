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

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.ElevatorBaseBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ElevatorMenu;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.misc.Symbols.ARROW_LEFT;
import static me.desht.pneumaticcraft.api.misc.Symbols.ARROW_RIGHT;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ElevatorScreen extends AbstractPneumaticCraftContainerScreen<ElevatorMenu, ElevatorBaseBlockEntity> {
    private WidgetAnimatedStat statusStat;
    private WidgetTextField floorNameField;
    private WidgetLabel noFloorsLabel, floorNumberLabel;
    private WidgetButtonExtended cycleDown, cycleUp;
    private int currentEditedFloor;

    public ElevatorScreen(ElevatorMenu container, Inventory inventoryPlayer, Component displayName) {
        super(container, inventoryPlayer, displayName);
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.ELEVATOR_BASE.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat floorNameStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.elevator.floorNames"),
                new ItemStack(ModBlocks.ELEVATOR_CALLER.get()), 0xFF005500, false);
        floorNameStat.setMinimumExpandedDimensions(120, 85);

        floorNameField = new WidgetTextField(font,6, 60, 120, 20);
        floorNameField.setValue(te.getFloorName(currentEditedFloor));
        floorNameField.setResponder(this::updateFloor);  // gui responder

        floorNameStat.addSubWidget(floorNameField);
        floorNameStat.addSubWidget(noFloorsLabel = new WidgetLabel(5, 20, xlate("pneumaticcraft.gui.tab.info.elevator.noCallers")).setColor(0xFFFFFFFF));
        floorNameStat.addSubWidget(floorNumberLabel = new WidgetLabel(65, 40, Component.empty())
                .setAlignment(WidgetLabel.Alignment.CENTRE).setColor(0xFFFFFFFF));
        floorNameStat.addSubWidget(cycleDown = new WidgetButtonExtended(5, 35, 20, 20, ARROW_LEFT, button -> cycleFloor(-1)));
        floorNameStat.addSubWidget(cycleUp = new WidgetButtonExtended(105, 35, 20, 20, ARROW_RIGHT, button -> cycleFloor(1)));
    }

    private void cycleFloor(int dir) {
        if (te.floorHeights.length > 0) {
            currentEditedFloor += dir;
            if (currentEditedFloor >= te.floorHeights.length) currentEditedFloor = 0;
            else if (currentEditedFloor < 0) currentEditedFloor = te.floorHeights.length - 1;

            floorNameField.setValue(te.getFloorName(currentEditedFloor));
            setFocused(floorNameField);
        }
    }

    private void updateFloor(String floorName) {
        te.setFloorName(currentEditedFloor, floorName);
        sendDelayed(5);
    }

    @Override
    protected void doDelayedAction() {
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, currentEditedFloor));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ELEVATOR;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float opacity, int x, int y) {
        super.renderBg(graphics, opacity, x, y);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        statusStat.setText(getStatusText());
        cycleDown.active = cycleUp.active = te.floorHeights.length > 0;
        noFloorsLabel.visible = te.floorHeights.length == 0;
        floorNumberLabel.visible = te.floorHeights.length > 0;
        floorNameField.active = te.floorHeights.length > 0;
        floorNumberLabel.setMessage(xlate("pneumaticcraft.gui.tab.info.elevator.floorNumber", currentEditedFloor + 1, te.floorHeights.length));
    }

    private List<Component> getStatusText() {
        List<Component> text = new ArrayList<>();

        text.add(xlate("pneumaticcraft.gui.tab.info.elevator.extension",
                PneumaticCraftUtils.roundNumberTo(te.extension, 1)).withStyle(ChatFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.info.elevator.maxExtension",
                PneumaticCraftUtils.roundNumberTo(te.getMaxElevatorHeight(), 1)).withStyle(ChatFormatting.BLACK));

        return text;
    }

    @Override
    protected void addWarnings(List<Component> textList) {
        super.addWarnings(textList);
        if (te.getMaxElevatorHeight() == te.extension) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.elevator.fully_extended"));
        }
    }
}
