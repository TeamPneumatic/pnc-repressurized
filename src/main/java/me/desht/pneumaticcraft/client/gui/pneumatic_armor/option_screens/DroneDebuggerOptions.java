package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.GuiUnitProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.debug.DroneDebugEntry;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class DroneDebuggerOptions extends IOptionPage.SimpleToggleableOptions<DroneDebugClientHandler> {
    private static final int PROGRAMMING_MARGIN = 20;
    private static final int PROGRAMMING_START_Y = 40;

    private EntityDrone selectedDrone;
    private GuiUnitProgrammer programmerUnit;
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
    }

    public static void clearAreaShowWidgetId() {
        areaShowWidgetId = -1;
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        if (Minecraft.getInstance().player != null) {
            ItemStack helmet = ClientUtils.getClientPlayer().getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!helmet.isEmpty()) {
                int entityId = NBTUtils.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                Entity entity = ClientUtils.getClientWorld().getEntityByID(entityId);
                if (entity instanceof EntityDrone) {
                    selectedDrone = (EntityDrone) entity;
                }
            }
        }

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
        programmerUnit = new DebugInfoProgrammerUnit(selectedDrone != null ? selectedDrone.getProgWidgets() : new ArrayList<>(),
                0, 0, guiScreen.width, guiScreen.height,
                new Rectangle2d(programmingStartX, PROGRAMMING_START_Y, programmingWidth, programmingHeight),
                0, 0, 0);
        if (selectedDrone != null) {
            programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class));
        }
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

        if (selectedDrone != null) {
            Minecraft.getInstance().fontRenderer.func_238407_a_(matrixStack, xlate("pneumaticcraft.gui.progWidget.debug.droneName",  selectedDrone.getName().getString()).func_241878_f(), 20, screenHeight - 15, 0xFFFFFFFF);
            Minecraft.getInstance().fontRenderer.func_238407_a_(matrixStack, xlate("pneumaticcraft.gui.progWidget.debug.routine",  selectedDrone.getLabel()).func_241878_f(), screenWidth / 2f, screenHeight - 15, 0xFFFFFFFF);
        }

        matrixStack.push();
        matrixStack.translate(0, 0, 300);
        programmerUnit.render(matrixStack, x, y, true, true);
        programmerUnit.renderForeground(matrixStack, x, y, null);
        matrixStack.pop();

        followCheckbox.render(matrixStack, x, y, partialTicks);

        if (selectedDrone == null) {
            guiScreen.drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer,
                    xlate("pneumaticcraft.gui.progWidget.debug.pressToDebug",
                            ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindDebuggingDrone)),
                    screenWidth / 2, screenHeight / 2, 0xFFFF0000
            );
        }

        IProgWidget widget = programmerUnit.getHoveredWidget(x, y);
        if (widget == null) widget = areaShowingWidget;
        getClientUpgradeHandler().getShowingPositions().clear();
        if (widget != null) {
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
            DroneDebugEntry entry = selectedDrone.getDebugEntry(widgetId);
            if (entry != null && entry.hasCoords()) {
                getClientUpgradeHandler().getShowingPositions().add(entry.getPos());
            }
        }
    }

    @Override
    public void tick() {
        showStart.active = selectedDrone != null && !selectedDrone.getProgWidgets().isEmpty();
        showActive.active = selectedDrone != null && selectedDrone.getActiveWidget() != null;
        if (followCheckbox.checked && selectedDrone != null && selectedDrone.getActiveWidget() != null) {
            programmerUnit.gotoPiece(selectedDrone.getActiveWidget());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            areaShowingWidget = programmerUnit.getHoveredWidget((int)mouseX, (int)mouseY);
        } else if (mouseButton == 1) {
            IProgWidget widget = programmerUnit.getHoveredWidget((int)mouseX, (int)mouseY);
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
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseButton) {
        return programmerUnit.getScrollBar().mouseScrolled(mouseX, mouseY, mouseButton);
    }

    private class DebugInfoProgrammerUnit extends GuiUnitProgrammer {

        DebugInfoProgrammerUnit(List<IProgWidget> progWidgets, int guiLeft,
                                int guiTop, int width, int height, Rectangle2d bounds,
                                int translatedX, int translatedY, int lastZoom) {
            super(progWidgets, guiLeft, guiTop, width, height, bounds, translatedX, translatedY, lastZoom);
            TileEntityProgrammer.updatePuzzleConnections(progWidgets);
        }

        @Override
        protected void addAdditionalInfoToTooltip(IProgWidget widget, List<ITextComponent> tooltip) {
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);

            DroneDebugEntry entry = selectedDrone.getDebugEntry(widgetId);
            if (entry != null) {
                long elapsed = (System.currentTimeMillis() - entry.getReceivedTime()) / 50;
                tooltip.add(new StringTextComponent("Last message:" ).mergeStyle(TextFormatting.AQUA)
                        .appendString(PneumaticCraftUtils.convertTicksToMinutesAndSeconds(elapsed, true))
                        .mergeStyle(TextFormatting.YELLOW)
                        .appendString(" ago"));
                tooltip.add(new StringTextComponent("  \"")
                        .append(xlate(entry.getMessage()))
                        .appendString("\"  ")
                        .mergeStyle(TextFormatting.AQUA, TextFormatting.ITALIC));
                if (entry.hasCoords()) {
                    tooltip.add(xlate("pneumaticcraft.gui.progWidget.debug.hasPositions").mergeStyle(TextFormatting.GREEN));
                    if (widget != areaShowingWidget)
                        tooltip.add(xlate("pneumaticcraft.gui.progWidget.debug.clickToShow").mergeStyle(TextFormatting.GREEN));
                }
            }
            if (widget instanceof IAreaProvider) {
                if (widgetId == areaShowWidgetId) {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .append(xlate("pneumaticcraft.gui.programmer.button.stopShowingArea"))
                            .mergeStyle(TextFormatting.GREEN));
                } else {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .append(xlate("pneumaticcraft.gui.programmer.button.showArea"))
                            .mergeStyle(TextFormatting.GREEN));
                }
            }
        }

        @Override
        protected void renderAdditionally(MatrixStack matrixStack) {
            if (selectedDrone != null && selectedDrone.getActiveWidget() != null) {
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
