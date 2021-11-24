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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.ProgrammerWidgetAreaRenderer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.debug.DroneDebugEntry;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class DroneDebuggerOptions extends IOptionPage.SimpleOptionPage<DroneDebugClientHandler> {
    private static final int PROGRAMMING_MARGIN = 20;
    private static final int PROGRAMMING_START_Y = 40;

    private final IDroneBase selectedDrone;
    private ProgrammerWidgetAreaRenderer programmerUnit;
    private int programmingStartX, programmingWidth, programmingHeight;
    private IProgWidget areaShowingWidget;
    private Button showActive;
    private Button showStart;
    private WidgetCheckBox followCheckbox;

    // Index of the widget whose area is being shown; static so it persists beyond this short-lived object,
    // which goes away when the GUI is closed.  Gets reset when a new drone is selected for debugging.
    private static int areaShowWidgetId = -1;

    public DroneDebuggerOptions(IGuiScreen screen, DroneDebugClientHandler upgradeHandler) {
        super(screen, upgradeHandler);

        selectedDrone = ItemPneumaticArmor.getDebuggedDrone();
    }

    public static void clearAreaShowWidgetId() {
        areaShowWidgetId = -1;
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        showStart = new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.gui.progWidget.debug.showStart"),
                b -> programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class)));
        gui.addWidget(showStart);

        showActive = new WidgetButtonExtended(30, 150, 150, 20,
                xlate("pneumaticcraft.gui.progWidget.debug.showActive"),
                b -> programmerUnit.gotoPiece(selectedDrone.getActiveWidget()));
        gui.addWidget(showActive);

        followCheckbox = new WidgetCheckBox(30, 176, 0xFFFFFFFF, new StringTextComponent(" ").append(xlate("pneumaticcraft.gui.progWidget.debug.followActive")));
        followCheckbox.x = 180 - followCheckbox.getWidth();
        gui.addWidget(followCheckbox);

        Screen guiScreen = getGuiScreen().getScreen();
        programmingStartX = PROGRAMMING_MARGIN;
        programmingWidth = guiScreen.width - PROGRAMMING_MARGIN * 2;
        programmingHeight = guiScreen.height - PROGRAMMING_MARGIN - PROGRAMMING_START_Y;
        programmerUnit = new DebugWidgetAreaRenderer(guiScreen, selectedDrone != null ? selectedDrone.getProgWidgets() : new ArrayList<>(),
                0, 0,
                new Rectangle2d(programmingStartX, PROGRAMMING_START_Y, programmingWidth, programmingHeight),
                0, 0, 0);
        if (isDroneValid()) {
            programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class));
        }
    }

    private boolean isDroneValid() {
        return selectedDrone != null && selectedDrone.isDroneStillValid();
    }

    @Override
    public void renderPre(MatrixStack matrixStack, int x, int y, float partialTicks) {
        AbstractGui.fill(matrixStack, programmingStartX, PROGRAMMING_START_Y, programmingStartX + programmingWidth, PROGRAMMING_START_Y + programmingHeight, 0x55000000);
    }

    @Override
    public void renderPost(MatrixStack matrixStack, int x, int y, float partialTicks) {
        Screen guiScreen = getGuiScreen().getScreen();

        int screenWidth = guiScreen.width;
        int screenHeight = guiScreen.height;

        if (isDroneValid()) {
            Minecraft.getInstance().font.drawShadow(matrixStack, xlate("pneumaticcraft.gui.progWidget.debug.droneName",
                    selectedDrone.getDroneName().getString()).getVisualOrderText(), 20, screenHeight - 15, 0xFFFFFFFF);
            Minecraft.getInstance().font.drawShadow(matrixStack, xlate("pneumaticcraft.gui.progWidget.debug.routine",
                    selectedDrone.getLabel()).getVisualOrderText(), screenWidth / 2f, screenHeight - 15, 0xFFFFFFFF);
        }

        matrixStack.pushPose();
        matrixStack.translate(0, 0, 300);
        programmerUnit.render(matrixStack, x, y, true, true);
        programmerUnit.renderForeground(matrixStack, x, y, null, getGuiScreen().getFontRenderer());
        matrixStack.popPose();

        followCheckbox.render(matrixStack, x, y, partialTicks);

        if (isDroneValid()) {

            getClientUpgradeHandler().getShowingPositions().clear();
            if (areaShowingWidget != null) {
                int widgetId = selectedDrone.getProgWidgets().indexOf(areaShowingWidget);
                DroneDebugEntry entry = selectedDrone.getDebugger().getDebugEntry(widgetId);
                if (entry != null && entry.hasCoords()) {
                    getClientUpgradeHandler().getShowingPositions().add(entry.getPos());
                }
            }
        } else {
            matrixStack.translate(0, 0, 200);
            AbstractGui.drawCenteredString(matrixStack, Minecraft.getInstance().font,
                    xlate("pneumaticcraft.gui.progWidget.debug.pressToDebug",
                            ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindDebuggingDrone)),
                    screenWidth / 2, screenHeight - 40, 0xFFFF0000
            );
            matrixStack.translate(0, 0, 200);
        }
    }

    @Override
    public void tick() {
        programmerUnit.tick();

        showStart.active = isDroneValid() && !selectedDrone.getProgWidgets().isEmpty();
        showActive.active = isDroneValid() && selectedDrone.getActiveWidget() != null;
        if (followCheckbox.checked && isDroneValid() && selectedDrone.getActiveWidget() != null) {
            programmerUnit.gotoPiece(selectedDrone.getActiveWidget());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!isDroneValid()) return super.mouseClicked(mouseX, mouseY, mouseButton);

        IProgWidget widget = programmerUnit.getHoveredWidget((int)mouseX, (int)mouseY);
        if (mouseButton == 0) {
            areaShowingWidget = areaShowingWidget == widget ? null : programmerUnit.getHoveredWidget((int)mouseX, (int)mouseY);
        } else if (mouseButton == 1) {
            if (widget instanceof IAreaProvider) {
                getClientUpgradeHandler().getShownArea().clear();
                int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
                if (areaShowWidgetId != widgetId) {
                    Set<BlockPos> area = Sets.newHashSet();
                    ((IAreaProvider) widget).getArea(area);
                    getClientUpgradeHandler().getShownArea().addAll(area);
                    areaShowWidgetId = widgetId;
                } else {
                    clearAreaShowWidgetId();
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseButton) {
        return isDroneValid() && programmerUnit.getScrollBar().mouseScrolled(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return isDroneValid() && programmerUnit.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private class DebugWidgetAreaRenderer extends ProgrammerWidgetAreaRenderer {

        DebugWidgetAreaRenderer(Screen parent, List<IProgWidget> progWidgets,
                                int guiLeft, int guiTop, Rectangle2d bounds,
                                int translatedX, int translatedY, int lastZoom) {
            super(parent, progWidgets, guiLeft, guiTop, bounds, translatedX, translatedY, lastZoom);
            TileEntityProgrammer.updatePuzzleConnections(progWidgets);
        }

        @Override
        protected void addAdditionalInfoToTooltip(IProgWidget widget, List<ITextComponent> tooltip) {
            if (!isDroneValid()) return;

            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);

            DroneDebugEntry entry = selectedDrone.getDebugger().getDebugEntry(widgetId);
            if (entry != null) {
                long elapsed = (System.currentTimeMillis() - entry.getReceivedTime()) / 50;
                tooltip.add((xlate("pneumaticcraft.gui.progWidget.debug.lastMessage",
                                PneumaticCraftUtils.convertTicksToMinutesAndSeconds(elapsed, true))).withStyle(TextFormatting.AQUA)
                );
                tooltip.add(new StringTextComponent("  \"")
                        .append(xlate(entry.getMessage()))
                        .append("\"  ")
                        .withStyle(TextFormatting.AQUA, TextFormatting.ITALIC));
                if (entry.hasCoords()) {
                    tooltip.add(xlate("pneumaticcraft.gui.progWidget.debug.hasPositions").withStyle(TextFormatting.YELLOW));
                    tooltip.add(xlate("pneumaticcraft.gui.progWidget.debug.clickToShow").withStyle(TextFormatting.GREEN));
                }
            }
            if (widget instanceof IAreaProvider) {
                if (widgetId == areaShowWidgetId) {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .append(xlate("pneumaticcraft.gui.programmer.button.stopShowingArea"))
                            .withStyle(TextFormatting.GREEN));
                } else {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .append(xlate("pneumaticcraft.gui.programmer.button.showArea"))
                            .withStyle(TextFormatting.GREEN));
                }
            }
        }

        @Override
        protected void renderAdditionally(MatrixStack matrixStack) {
            if (isDroneValid() && selectedDrone.getActiveWidget() != null) {
                drawBorder(matrixStack, selectedDrone.getActiveWidget(), 0xFF00FF00);
                if (areaShowWidgetId >= 0) {
                    drawBorder(matrixStack, selectedDrone.getProgWidgets().get(areaShowWidgetId), 0xA040FFA0, 2);
                }
            }
        }
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public boolean displaySettingsHeader() {
        return false;
    }

}
