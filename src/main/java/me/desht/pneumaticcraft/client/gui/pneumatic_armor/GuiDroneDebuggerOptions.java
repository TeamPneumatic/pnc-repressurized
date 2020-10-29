package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.GuiUnitProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.living.DebugEntry;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
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

public class GuiDroneDebuggerOptions extends IOptionPage.SimpleToggleableOptions<DroneDebugUpgradeHandler> {
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

    public GuiDroneDebuggerOptions(IGuiScreen screen, DroneDebugUpgradeHandler upgradeHandler) {
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
                int entityId = NBTUtil.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                Entity entity = ClientUtils.getClientWorld().getEntityByID(entityId);
                if (entity instanceof EntityDrone) {
                    selectedDrone = (EntityDrone) entity;
                }
            }
        }

        showStart = new WidgetButtonExtended(30, 128, 150, 20,
                I18n.format("pneumaticcraft.gui.progWidget.debug.showStart"),
                b -> programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class)));
        gui.addWidget(showStart);

        showActive = new WidgetButtonExtended(30, 150, 150, 20,
                I18n.format("pneumaticcraft.gui.progWidget.debug.showActive"),
                b -> programmerUnit.gotoPiece(selectedDrone.getActiveWidget()));
        gui.addWidget(showActive);

        followCheckbox = new WidgetCheckBox(30, 176, 0xFFFFFFFF, " " + I18n.format("pneumaticcraft.gui.progWidget.debug.followActive"));
        followCheckbox.x = 180 - followCheckbox.getWidth();
        gui.addWidget(followCheckbox);

        Screen guiScreen = getGuiScreen().getScreen();
        programmingStartX = PROGRAMMING_MARGIN;
        programmingWidth = guiScreen.width - PROGRAMMING_MARGIN * 2;
        programmingHeight = guiScreen.height - PROGRAMMING_MARGIN - PROGRAMMING_START_Y;
        programmerUnit = new DebugInfoProgrammerUnit(selectedDrone != null ? selectedDrone.getProgWidgets() : new ArrayList<>(),
                gui.getFontRenderer(),
                0, 0, guiScreen.width, guiScreen.height,
                new Rectangle2d(programmingStartX, PROGRAMMING_START_Y, programmingWidth, programmingHeight),
                0, 0, 0);
        if (selectedDrone != null) {
            programmerUnit.gotoPiece(GuiProgrammer.findWidget(selectedDrone.getProgWidgets(), ProgWidgetStart.class));
        }
    }

    @Override
    public void renderPre(int x, int y, float partialTicks) {
        AbstractGui.fill(programmingStartX, PROGRAMMING_START_Y, programmingStartX + programmingWidth, PROGRAMMING_START_Y + programmingHeight, 0x55000000);
    }

    @Override
    public void renderPost(int x, int y, float partialTicks) {
        Screen guiScreen = getGuiScreen().getScreen();

        int screenWidth = guiScreen.width;
        int screenHeight = guiScreen.height;

        if (selectedDrone != null) {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow("Drone name: " + selectedDrone.getName().getFormattedText(), 20, screenHeight - 15, 0xFFFFFFFF);
            Minecraft.getInstance().fontRenderer.drawStringWithShadow("Routine: " + selectedDrone.getLabel(), screenWidth / 2f, screenHeight - 15, 0xFFFFFFFF);
        }

        RenderSystem.translated(0, 0, 300);
        programmerUnit.render(x, y, true, true);
        programmerUnit.renderForeground(x, y, null);
        RenderSystem.translated(0, 0, -300);

        followCheckbox.render(x, y, partialTicks);

        if (selectedDrone == null) {
            guiScreen.drawCenteredString(Minecraft.getInstance().fontRenderer, "Press '" + KeyHandler.getInstance().keybindDebuggingDrone.getLocalizedName() + "' on a Drone when tracked by an Entity Tracker to debug the Drone.", screenWidth / 2, screenHeight / 2, 0xFFFF0000);
        }

        IProgWidget widget = programmerUnit.getHoveredWidget(x, y);
        if (widget == null) widget = areaShowingWidget;
        getUpgradeHandler().getShowingPositions().clear();
        if (widget != null) {
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
            DebugEntry entry = selectedDrone.getDebugEntry(widgetId);
            if (entry != null && entry.hasCoords()) {
                getUpgradeHandler().getShowingPositions().add(entry.getPos());
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
                getUpgradeHandler().getShownArea().clear();
                int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
                if (areaShowWidgetId != widgetId) {
                    Set<BlockPos> area = Sets.newHashSet();
                    ((IAreaProvider) widget).getArea(area);
                    getUpgradeHandler().getShownArea().addAll(area);
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

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return programmerUnit.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private class DebugInfoProgrammerUnit extends GuiUnitProgrammer {

        DebugInfoProgrammerUnit(List<IProgWidget> progWidgets, FontRenderer fontRenderer, int guiLeft,
                                int guiTop, int width, int height, Rectangle2d bounds,
                                int translatedX, int translatedY, int lastZoom) {
            super(progWidgets, fontRenderer, guiLeft, guiTop, width, height, bounds, translatedX, translatedY, lastZoom);
            TileEntityProgrammer.updatePuzzleConnections(progWidgets);
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
                    tooltip.add(xlate("pneumaticcraft.gui.progWidget.debug.hasPositions").applyTextStyle(TextFormatting.GREEN));
                    if (widget != areaShowingWidget)
                        tooltip.add(xlate("pneumaticcraft.gui.progWidget.debug.clickToShow").applyTextStyle(TextFormatting.GREEN));
                }
            }
            if (widget instanceof IAreaProvider) {
                if (widgetId == areaShowWidgetId) {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .appendSibling(xlate("pneumaticcraft.gui.programmer.button.stopShowingArea"))
                            .applyTextStyle(TextFormatting.GREEN));
                } else {
                    tooltip.add(new StringTextComponent("Right-Click: ")
                            .appendSibling(xlate("pneumaticcraft.gui.programmer.button.showArea"))
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
    public boolean isToggleable() {
        return false;
    }

    @Override
    public boolean displaySettingsHeader() {
        return false;
    }

}
