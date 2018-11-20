package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.GuiUnitProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.common.entity.living.DebugEntry;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuiDroneDebuggerOptions extends Gui implements IOptionPage {
    private static final int PROGAMMING_MARGIN = 20;
    private static final int PROGRAMMING_START_Y = 40;

    private final DroneDebugUpgradeHandler upgradeHandler;
    private EntityDrone selectedDrone;
    private GuiUnitProgrammer programmerUnit;
    private int programmingStartX, programmingWidth, programmingHeight;
    private IProgWidget areaShowingWidget;
    private int screenWidth, screenHeight;
    private GuiButton showActive;
    private GuiButton showStart;
    private GuiCheckBox followCheckbox;

    // Index of the widget whose area is being shown; static so it persists beyond this short-lived object,
    // which goes away when the GUI is closed.  Gets reset when a new drone is selected for debugging.
    private static int areaShowWidgetId = -1;

    public GuiDroneDebuggerOptions(DroneDebugUpgradeHandler upgradeHandler) {
        this.upgradeHandler = upgradeHandler;
    }

    public static void clearAreaShowWidgetId() {
        areaShowWidgetId = -1;
    }

    @Override
    public String getPageName() {
        return "Drone Debugging";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        GuiScreen guiScreen = (GuiScreen) gui;
        screenWidth = guiScreen.width;
        screenHeight = guiScreen.height;

        if (PneumaticCraftRepressurized.proxy.getClientPlayer() != null) {
            ItemStack helmet = PneumaticCraftRepressurized.proxy.getClientPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (!helmet.isEmpty()) {
                int entityId = NBTUtil.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                Entity entity = PneumaticCraftRepressurized.proxy.getClientWorld().getEntityByID(entityId);
                if (entity instanceof EntityDrone) {
                    selectedDrone = (EntityDrone) entity;
                }
            }
        }

        showStart = new GuiButton(10, 30, 128, 150, 20, I18n.format("gui.progWidget.debug.showStart"));
        gui.getButtonList().add(showStart);

        showActive = new GuiButton(11, 30, 150, 150, 20, I18n.format("gui.progWidget.debug.showActive"));
        gui.getButtonList().add(showActive);

        followCheckbox = new GuiCheckBox(12, 30, 176, 0xFFFFFFFF, " " + I18n.format("gui.progWidget.debug.followActive"));
        followCheckbox.x = 180 - followCheckbox.getBounds().width;

        programmingStartX = PROGAMMING_MARGIN;
        programmingWidth = guiScreen.width - PROGAMMING_MARGIN * 2;
        programmingHeight = guiScreen.height - PROGAMMING_MARGIN - PROGRAMMING_START_Y;
        programmerUnit = new DebugInfoProgrammerUnit(selectedDrone != null ? selectedDrone.getProgWidgets() : new ArrayList<>(),
                gui.getFontRenderer(),
                0, 0, guiScreen.width, guiScreen.height,
                100,
                programmingStartX, PROGRAMMING_START_Y,
                programmingWidth, programmingHeight,
                0, 0, 0);
        if (selectedDrone != null) {
            programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class));
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (selectedDrone != null) {
            if (button.id == 10) {
                programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class));
            } else if (button.id == 11) {
                programmerUnit.gotoPiece(selectedDrone.getActiveWidget());
            }
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {
        drawRect(programmingStartX, PROGRAMMING_START_Y, programmingStartX + programmingWidth, PROGRAMMING_START_Y + programmingHeight, 0x55000000);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        if (selectedDrone != null) {
            Minecraft.getMinecraft().fontRenderer.drawString("Drone name: " + selectedDrone.getName(), 20, screenHeight - 15, 0xFFFFFFFF, true);
            Minecraft.getMinecraft().fontRenderer.drawString("Routine: " + selectedDrone.getLabel(), screenWidth / 2, screenHeight - 15, 0xFFFFFFFF, true);
        }

        programmerUnit.render(x, y, true, true, true);
        programmerUnit.renderForeground(x, y, null);

        followCheckbox.render(x, y, partialTicks);

        if (selectedDrone == null) {
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, "Press '" + Keyboard.getKeyName(KeyHandler.getInstance().keybindDebuggingDrone.getKeyCode()) + "' on a Drone when tracked by an Entity Tracker to debug the Drone.", screenWidth / 2, screenHeight / 2, 0xFFFF0000);
        }

        IProgWidget widget = programmerUnit.getHoveredWidget(x, y);
        if (widget == null) widget = areaShowingWidget;
        upgradeHandler.getShowingPositions().clear();
        if (widget != null) {
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
            DebugEntry entry = selectedDrone.getDebugEntry(widgetId);
            if (entry != null && entry.hasCoords()) {
                upgradeHandler.getShowingPositions().add(entry.getPos());
            }
        }
    }

    @Override
    public void updateScreen() {
        showStart.enabled = selectedDrone != null && !selectedDrone.getProgWidgets().isEmpty();
        showActive.enabled = selectedDrone != null && selectedDrone.getActiveWidget() != null;
        if (followCheckbox.checked && selectedDrone != null && selectedDrone.getActiveWidget() != null) {
            programmerUnit.gotoPiece(selectedDrone.getActiveWidget());
        }
    }

    @Override
    public void keyTyped(char ch, int key) {

    }

    @Override
    public void mouseClicked(int x, int y, int button) {
        if (followCheckbox.getBounds().contains(x, y)) {
            followCheckbox.onMouseClicked(x, y, button);
        } else {
            if (button == 0) {
                areaShowingWidget = programmerUnit.getHoveredWidget(x, y);
            } else if (button == 1) {
                IProgWidget widget = programmerUnit.getHoveredWidget(x, y);
                if (widget instanceof IAreaProvider) {
                    upgradeHandler.getShownArea().clear();
                    int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
                    if (areaShowWidgetId != widgetId) {
                        Set<BlockPos> area = Sets.newHashSet();
                        ((IAreaProvider) widget).getArea(area);
                        upgradeHandler.getShownArea().addAll(area);
                        areaShowWidgetId = widgetId;
                    } else {
                        clearAreaShowWidgetId();
                    }
                }
            }
        }
    }

    @Override
    public void handleMouseInput() {
        programmerUnit.getScrollBar().handleMouseInput();
    }

    private class DebugInfoProgrammerUnit extends GuiUnitProgrammer {

        DebugInfoProgrammerUnit(List<IProgWidget> progWidgets, FontRenderer fontRenderer, int guiLeft,
                                int guiTop, int width, int height, int xSize, int startX, int startY, int areaWidth, int areaHeight,
                                int translatedX, int translatedY, int lastZoom) {
            super(progWidgets, fontRenderer, guiLeft, guiTop, width, height, xSize, startX, startY, areaWidth, areaHeight, translatedX, translatedY, lastZoom);
        }

        @Override
        protected void addAdditionalInfoToTooltip(IProgWidget widget, List<String> tooltip) {
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);

            DebugEntry entry = selectedDrone.getDebugEntry(widgetId);
            if (entry != null) {
                long elapsed = (System.currentTimeMillis() - entry.getReceivedTime()) / 50;
                tooltip.add(TextFormatting.AQUA + "Last message:  " + TextFormatting.YELLOW + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(elapsed, true) + " ago");
                tooltip.add(TextFormatting.AQUA + TextFormatting.ITALIC.toString() + "  \"" + I18n.format(entry.getMessage()) + "\"  ");
                if (entry.hasCoords()) {
                    tooltip.add(TextFormatting.GREEN + I18n.format("gui.progWidget.debug.hasPositions"));
                    if (widget != areaShowingWidget)
                        tooltip.add(TextFormatting.GREEN + I18n.format("gui.progWidget.debug.clickToShow"));
                }
            }
            if (widget instanceof IAreaProvider) {
                if (widgetId == areaShowWidgetId) {
                    tooltip.add(TextFormatting.GREEN + "Right-Click: " + I18n.format("gui.programmer.button.stopShowingArea"));
                } else {
                    tooltip.add(TextFormatting.GREEN + "Right-Click: " + I18n.format("gui.programmer.button.showArea"));
                }
            }
        }

        @Override
        protected void renderAdditionally() {
            if (selectedDrone != null && selectedDrone.getActiveWidget() != null) {
                drawBorder(selectedDrone.getActiveWidget(), 0xFF00FF00);
                if (areaShowWidgetId >= 0) {
                    drawBorder(selectedDrone.getProgWidgets().get(areaShowWidgetId), 0xA040FFA0, 2);
                }
            }
        }
    }

    @Override
    public boolean canBeTurnedOff() {
        return false;
    }

    @Override
    public boolean displaySettingsText() {
        return false;
    }

}
