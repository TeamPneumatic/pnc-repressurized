package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.GuiUnitProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.common.entity.living.DebugEntry;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
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

public class GuiDroneDebuggerOptions extends Screen implements IOptionPage {
    private static final int PROGAMMING_MARGIN = 20;
    private static final int PROGRAMMING_START_Y = 40;

    private final DroneDebugUpgradeHandler upgradeHandler;
    private EntityDrone selectedDrone;
    private GuiUnitProgrammer programmerUnit;
    private int programmingStartX, programmingWidth, programmingHeight;
    private IProgWidget areaShowingWidget;
    private int screenWidth, screenHeight;
    private Button showActive;
    private Button showStart;
    private GuiCheckBox followCheckbox;

    // Index of the widget whose area is being shown; static so it persists beyond this short-lived object,
    // which goes away when the GUI is closed.  Gets reset when a new drone is selected for debugging.
    private static int areaShowWidgetId = -1;

    public GuiDroneDebuggerOptions(DroneDebugUpgradeHandler upgradeHandler) {
        super(new StringTextComponent("Done Debugging"));
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
        Screen guiScreen = (Screen) gui;
        screenWidth = guiScreen.width;
        screenHeight = guiScreen.height;

        if (PneumaticCraftRepressurized.proxy.getClientPlayer() != null) {
            ItemStack helmet = PneumaticCraftRepressurized.proxy.getClientPlayer().getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!helmet.isEmpty()) {
                int entityId = NBTUtil.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                Entity entity = PneumaticCraftRepressurized.proxy.getClientWorld().getEntityByID(entityId);
                if (entity instanceof EntityDrone) {
                    selectedDrone = (EntityDrone) entity;
                }
            }
        }

        showStart = new GuiButtonSpecial(30, 128, 150, 20,
                I18n.format("gui.progWidget.debug.showStart"),
                b -> programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class)));
        gui.getWidgetList().add(showStart);

        showActive = new GuiButtonSpecial(30, 150, 150, 20,
                I18n.format("gui.progWidget.debug.showActive"),
                b -> programmerUnit.gotoPiece(selectedDrone.getActiveWidget()));
        gui.getWidgetList().add(showActive);

        followCheckbox = new GuiCheckBox(30, 176, 0xFFFFFFFF, " " + I18n.format("gui.progWidget.debug.followActive"));
        followCheckbox.x = 180 - followCheckbox.getBounds().width;

        programmingStartX = PROGAMMING_MARGIN;
        programmingWidth = guiScreen.width - PROGAMMING_MARGIN * 2;
        programmingHeight = guiScreen.height - PROGAMMING_MARGIN - PROGRAMMING_START_Y;
        programmerUnit = new DebugInfoProgrammerUnit(selectedDrone != null ? selectedDrone.getProgWidgets() : new ArrayList<>(),
                gui.getFontRenderer(),
                0, 0, guiScreen.width, guiScreen.height,
                programmingStartX, PROGRAMMING_START_Y,
                programmingWidth, programmingHeight,
                0, 0, 0);
        if (selectedDrone != null) {
            programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class));
        }
    }

    @Override
    public void renderPre(int x, int y, float partialTicks) {
        fill(programmingStartX, PROGRAMMING_START_Y, programmingStartX + programmingWidth, PROGRAMMING_START_Y + programmingHeight, 0x55000000);
    }

    @Override
    public void renderPost(int x, int y, float partialTicks) {
        if (selectedDrone != null) {
            font.drawStringWithShadow("Drone name: " + selectedDrone.getName(), 20, screenHeight - 15, 0xFFFFFFFF);
            font.drawStringWithShadow("Routine: " + selectedDrone.getLabel(), screenWidth / 2f, screenHeight - 15, 0xFFFFFFFF);
        }

        GlStateManager.translated(0, 0, 300);
        programmerUnit.render(x, y, true, true, true);
        programmerUnit.renderForeground(x, y, null);
        GlStateManager.translated(0, 0, -300);

        followCheckbox.render(x, y, partialTicks);

        if (selectedDrone == null) {
            drawCenteredString(font, "Press '" + KeyHandler.getInstance().keybindDebuggingDrone.getKeyDescription() + "' on a Drone when tracked by an Entity Tracker to debug the Drone.", screenWidth / 2, screenHeight / 2, 0xFFFF0000);
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
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseButton) {
        return programmerUnit.getScrollBar().mouseScrolled(mouseX, mouseY, mouseButton);
    }

    private class DebugInfoProgrammerUnit extends GuiUnitProgrammer {

        DebugInfoProgrammerUnit(List<IProgWidget> progWidgets, FontRenderer fontRenderer, int guiLeft,
                                int guiTop, int width, int height, int startX, int startY, int areaWidth, int areaHeight,
                                int translatedX, int translatedY, int lastZoom) {
            super(progWidgets, fontRenderer, guiLeft, guiTop, width, height, startX, startY, areaWidth, areaHeight, translatedX, translatedY, lastZoom);
        }

        @Override
        protected void addAdditionalInfoToTooltip(IProgWidget widget, List<ITextComponent> tooltip) {
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);

            DebugEntry entry = selectedDrone.getDebugEntry(widgetId);
            if (entry != null) {
                long elapsed = (System.currentTimeMillis() - entry.getReceivedTime()) / 50;
                tooltip.add(new StringTextComponent("Last message:" ).applyTextStyle(TextFormatting.AQUA)
                        .appendText(PneumaticCraftUtils.convertTicksToMinutesAndSeconds(elapsed, true))
                        .applyTextStyle(TextFormatting.YELLOW)
                        .appendText(" ago"));
                tooltip.add(new StringTextComponent("  \"")
                        .appendSibling(xlate(entry.getMessage()))
                        .appendText("\"  ")
                        .applyTextStyles(TextFormatting.AQUA, TextFormatting.ITALIC));
                if (entry.hasCoords()) {
                    tooltip.add(xlate("gui.progWidget.debug.hasPositions").applyTextStyle(TextFormatting.GREEN));
                    if (widget != areaShowingWidget)
                        tooltip.add(xlate("gui.progWidget.debug.clickToShow").applyTextStyle(TextFormatting.GREEN));
                }
            }
            if (widget instanceof IAreaProvider) {
                if (widgetId == areaShowWidgetId) {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .appendSibling(xlate("gui.programmer.button.stopShowingArea"))
                            .applyTextStyle(TextFormatting.GREEN));
                } else {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .appendSibling(xlate("gui.programmer.button.showArea"))
                            .applyTextStyle(TextFormatting.GREEN));
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
    public boolean displaySettingsHeader() {
        return false;
    }

}
