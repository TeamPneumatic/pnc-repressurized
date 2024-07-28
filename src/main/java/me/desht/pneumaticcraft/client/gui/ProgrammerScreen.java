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

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.client.gui.programmer.AbstractProgWidgetScreen;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetGuiManager;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.render.ProgWidgetRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.ProgWidgetUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import me.desht.pneumaticcraft.common.inventory.ProgrammerMenu;
import me.desht.pneumaticcraft.common.item.GPSAreaToolItem;
import me.desht.pneumaticcraft.common.item.GPSToolItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.network.PacketProgrammerSync;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.legacyconv.ConversionType;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgrammerScreen extends AbstractPneumaticCraftContainerScreen<ProgrammerMenu,ProgrammerBlockEntity> {
    private PastebinScreen pastebinGui;

    private WidgetButtonExtended importButton;
    private WidgetButtonExtended exportButton;
    private WidgetButtonExtended allWidgetsButton;
    private WidgetCheckBox showInfo, showFlow;
    private WidgetTextField nameField;
    private WidgetTextField filterField;
    private WidgetButtonExtended undoButton, redoButton;
    private WidgetButtonExtended convertToRelativeButton;
    private WidgetButtonExtended rotateCoordsButton;

    // those widgets currently visible in the tray
    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<>();
    // widgets being deleted (visual only)
    private final List<RemovingWidget> removingWidgets = new ArrayList<>();
    private BitSet filteredSpawnWidgets;

    private ProgrammerWidgetAreaRenderer programmerUnit;
    private IProgWidget draggingWidget;
    private int lastMouseX, lastMouseY;
    private double dragMouseStartX, dragMouseStartY;
    private double dragWidgetStartX, dragWidgetStartY;
    private boolean draggingBG;
    private int widgetPage;
    private int maxPage;

    private boolean showingAllWidgets;
    private int showingWidgetProgress;
    private int oldShowingWidgetProgress;

    private final Rect2i programmerBounds;

    private static final int WIDGET_X_SPACING = 22; // x size of widgets in the widget tray

    private WidgetDifficulty programmerDifficulty;

    public ProgrammerScreen(ProgrammerMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        IntIntPair size = calculateScreenSize();
        imageWidth = size.firstInt();
        imageHeight = size.secondInt();

        ProgrammerMenu.AreaGeometry geometry = calculateAreaBounds();
        programmerBounds = new Rect2i(geometry.x(), geometry.y(), geometry.width(), geometry.height());

        programmerDifficulty = ConfigHelper.client().general.programmerDifficulty.get();
    }

    public static void updateProgramNameIfOpen() {
        if (Minecraft.getInstance().screen instanceof ProgrammerScreen programmerScreen) {
            ItemStack stack = programmerScreen.te.getItemInProgrammingSlot();
            programmerScreen.nameField.setValue(stack.getHoverName().getString());
        }
    }

    @Override
    public void init() {
        super.init();

        if (pastebinGui != null && pastebinGui.getOutput() != null) {
            List<IProgWidget> progWidgets = ProgWidgetUtils.getWidgetsFromJson(registryAccess(), pastebinGui.getOutput());
            if (pastebinGui.shouldMerge) {
                List<IProgWidget> newWidgets = te.mergeWidgetsFromNBT(progWidgets);
                te.setProgWidgets(newWidgets, ClientUtils.getClientPlayer());
            } else {
                te.setProgWidgets(progWidgets, ClientUtils.getClientPlayer());
            }
            pastebinGui = null;
            NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
            te.recentreStartPiece = true;
        }

        if (programmerUnit != null) {
            te.translatedX = programmerUnit.getTranslatedX();
            te.translatedY = programmerUnit.getTranslatedY();
            te.zoomState = programmerUnit.getLastZoom();
        }

        Rect2i bounds = getProgrammerBounds();
        programmerUnit = new ProgrammerWidgetAreaRenderer(te.progWidgets, leftPos, topPos,
                bounds, te.translatedX, te.translatedY, te.zoomState);
        addRenderableWidget(programmerUnit.getScrollBar());

        // right and bottom edges of the programming area
        int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth();
        int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight() + 3;

        importButton = new WidgetButtonExtended(leftPos + xRight + 4, topPos + 3, 15, 15, Symbols.ARROW_LEFT)
                .withTag("import")
                .setTooltipKey("pneumaticcraft.gui.programmer.button.import");
        addRenderableWidget(importButton);

        exportButton = new WidgetButtonExtended(leftPos + xRight + 4, topPos + 20, 15, 15, Symbols.ARROW_RIGHT)
                .withTag("export")
                .withCustomTooltip(this::updateExportButtonTooltip);
        addRenderableWidget(exportButton);

        var lb = addRenderableWidget(new WidgetButtonExtended(leftPos + xRight - 13, topPos + yBottom, 13, 10,
                Symbols.TRIANGLE_LEFT, b -> adjustPage(-1)));
        addRenderableWidget(new WidgetButtonExtended(leftPos + xRight + 30, topPos + yBottom, 13, 10,
                Symbols.TRIANGLE_RIGHT, b -> adjustPage(1)));

        allWidgetsButton = new WidgetButtonExtended(leftPos + xRight + 20, topPos + yBottom - 15, 12, 12,
                Symbols.TRIANGLE_UP_LEFT, b -> toggleShowWidgets());
        allWidgetsButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.openPanel.tooltip"));
        addRenderableWidget(allWidgetsButton);

        addDifficultyButtons(lb.getY() + lb.getHeight() + 3);

        addRenderableWidget(new WidgetButtonExtended(leftPos + 5, topPos + yBottom + 4, 55, 20,
                xlate("pneumaticcraft.gui.programmer.button.showStart"), b -> gotoStart())
                .setTooltipText(xlate("pneumaticcraft.gui.programmer.button.showStart.tooltip")));
        addRenderableWidget(new WidgetButtonExtended(leftPos + 5, topPos + yBottom + 26, 55, 20,
                xlate("pneumaticcraft.gui.programmer.button.showLatest"), b -> gotoLatest())
                .setTooltipText(xlate("pneumaticcraft.gui.programmer.button.showLatest.tooltip")));
        addRenderableWidget(showInfo = new WidgetCheckBox(leftPos + 5, topPos + yBottom + 49, 0xFF404040,
                xlate("pneumaticcraft.gui.programmer.checkbox.showInfo")).setChecked(te.showInfo));
        addRenderableWidget(showFlow = new WidgetCheckBox(leftPos + 5, topPos + yBottom + 61, 0xFF404040,
                xlate("pneumaticcraft.gui.programmer.checkbox.showFlow")).setChecked(te.showFlow));

        WidgetButtonExtended pastebinButton = new WidgetButtonExtended(leftPos - 22, topPos + 44, 20, 20, "",
                b -> pastebin());
        pastebinButton.setTooltipText(xlate("pneumaticcraft.gui.remote.button.pastebinButton"));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        addRenderableWidget(pastebinButton);

        undoButton = new WidgetButtonExtended(leftPos - 22, topPos + 2, 20, 20, "").withTag("undo");
        redoButton = new WidgetButtonExtended(leftPos - 22, topPos + 23, 20, 20, "").withTag("redo");
        WidgetButtonExtended clearAllButton = new WidgetButtonExtended(leftPos - 22, topPos + 65, 20, 20, Component.empty(), b -> clear());
        convertToRelativeButton = new WidgetButtonExtended(leftPos - 22, topPos + 86, 20, 20, "R", b -> convertToRelative())
                .withCustomTooltip(this::buildConvertButtonTooltip);
        rotateCoordsButton = new WidgetButtonExtended(leftPos - 22, topPos + 107, 20, 20, "90", b -> rotateCoords90())
                .setTooltipText(xlate("pneumaticcraft.gui.programmer.button.rotate90button.tooltip"));

        undoButton.setRenderedIcon(Textures.GUI_UNDO_ICON_LOCATION);
        redoButton.setRenderedIcon(Textures.GUI_REDO_ICON_LOCATION);
        clearAllButton.setRenderedIcon(Textures.GUI_DELETE_ICON_LOCATION);

        undoButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.undoButton.tooltip"));
        redoButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.redoButton.tooltip"));
        clearAllButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.clearAllButton.tooltip"));

        addRenderableWidget(undoButton);
        addRenderableWidget(redoButton);
        addRenderableWidget(clearAllButton);
        addRenderableWidget(convertToRelativeButton);
        addRenderableWidget(rotateCoordsButton);

        addLabel(title, leftPos + 7, topPos + 6, 0xFF404040);

        nameField = new WidgetTextField(font, leftPos + xRight - 99, topPos + 4, 98, font.lineHeight + 3);
        nameField.setValue(te.displayedStack.getHoverName().getString());
        nameField.setResponder(s -> updateDroneName());
        addRenderableWidget(nameField);

        filterField = new FilterTextField(font, leftPos + 78, topPos + 24, 100, font.lineHeight + 3);
        filterField.setResponder(s -> filterSpawnWidgets());

        addRenderableWidget(filterField);

        Component name = xlate("pneumaticcraft.gui.programmer.name");
        addLabel(name, leftPos + xRight - 102 - font.width(name), topPos + 6, 0xFF404040);

        updateWidgetTrayLayout();

        for (IProgWidget widget : te.progWidgets) {
            if (!programmerUnit.isOutsideProgrammingArea(widget)) {
                return;
            }
        }
        programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
    }

    private void addDifficultyButtons(int y0) {
        int widestRB = Math.max(font.width(xlate("pneumaticcraft.gui.programmer.difficulty")),
                Arrays.stream(WidgetDifficulty.values())
                        .map(wd -> font.width(xlate(wd.getTranslationKey())))
                        .max(Comparator.comparingInt(o -> o)).orElse(0)
        );
        int fh = font.lineHeight + 2;
        WidgetLabel label = addRenderableWidget(new WidgetLabel(getGuiLeft() + imageWidth - widestRB - 20, y0, xlate("pneumaticcraft.gui.programmer.difficulty")));
        WidgetRadioButton.Builder<DifficultyButton> rbb = WidgetRadioButton.Builder.create();
        for (WidgetDifficulty wd : WidgetDifficulty.values()) {
            y0 += fh;
            DifficultyButton dButton = new DifficultyButton(label.getX(), y0,
                    0xFF404040, wd, b -> updateDifficulty(wd));
            dButton.setTooltip(Tooltip.create(xlate(wd.getTooltipTranslationKey())));
            rbb.addRadioButton(dButton, wd == programmerDifficulty);
        }
        rbb.build(this::addRenderableWidget);
    }

    @Override
    public boolean isPauseScreen() {
        return ConfigHelper.client().general.programmerGuiPauses.get();
    }

    public static void onCloseFromContainer() {
        if (Minecraft.getInstance().screen instanceof ProgrammerScreen p) {
            p.removed();
        }
    }

    public Rect2i getProgrammerBounds() {
        return programmerBounds;
    }

    private int getWidgetTrayRight() {
        return programmerBounds.getX() + programmerBounds.getWidth() + 20;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_BLANK_256; // dummy, not actually used
    }

    private void updateWidgetTrayLayout() {
        int y = 40, page = 0;
        int x = getWidgetTrayRight() - maxPage * WIDGET_X_SPACING + 2;
        boolean showAllWidgets = showingWidgetProgress == WIDGET_X_SPACING * maxPage && showingAllWidgets;
        filterField.setVisible(showAllWidgets);

        maxPage = 0;
        visibleSpawnWidgets.clear();
        int idx = 0;
        int nWidgets = PNCRegistries.PROG_WIDGETS_REGISTRY.size();
        for (ProgWidgetType<?> type : PNCRegistries.PROG_WIDGETS_REGISTRY) {
            IProgWidget widget = IProgWidget.create(type);
            if (widget.isAvailable() && widget.isDifficultyOK(programmerDifficulty)) {
                widget.setPosition(showAllWidgets ? x : getWidgetTrayRight(), y);
                int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
                y += widgetHeight;

                if (showAllWidgets || page == widgetPage) {
                    visibleSpawnWidgets.add(widget);
                }
                if (y > getProgrammerBounds().getHeight() - 20) {
                    y = 40;
                    x += WIDGET_X_SPACING;
                    page++;
                    if (idx < nWidgets - 1) maxPage++;
                }
            }
            idx++;
        }
        maxPage++;

        filterField.setX(Math.min(leftPos + getWidgetTrayRight() - 25 - filterField.getWidth(), leftPos + getWidgetTrayRight() - (maxPage * WIDGET_X_SPACING)));
        filterSpawnWidgets();

        if (widgetPage >= maxPage) {
            widgetPage = maxPage - 1;
            updateWidgetTrayLayout();
        }
    }

    private void filterSpawnWidgets() {
        String filterText = filterField.getValue().trim();
        if (!visibleSpawnWidgets.isEmpty() && !filterText.isEmpty()) {
            filteredSpawnWidgets = new BitSet(visibleSpawnWidgets.size());
            for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
                IProgWidget widget = visibleSpawnWidgets.get(i);
                String widgetName = I18n.get(widget.getTranslationKey());
                filteredSpawnWidgets.set(i, widgetName.toLowerCase().contains(filterText.toLowerCase()));
            }
        } else {
            filteredSpawnWidgets = null;
        }
    }

    @Override
    protected boolean shouldAddInfoTab() {
        return false;
    }

    private void updateDroneName() {
        ItemStack stack = te.getItemInProgrammingSlot();
        if (stack != ItemStack.EMPTY && !stack.getHoverName().getString().equals(nameField.getValue())) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(nameField.getValue()));
            sendDelayed(5);
        }
    }

    @Override
    protected void doDelayedAction() {
        NetworkHandler.sendToServer(PacketUpdateTextfield.create(te, 0));
    }

    private void adjustPage(int dir) {
        widgetPage += dir;
        if (widgetPage < 0) widgetPage = maxPage -1;
        else if (widgetPage >= maxPage) widgetPage = 0;
        updateWidgetTrayLayout();
    }

    private static final Component TDR = Component.literal(Symbols.TRIANGLE_DOWN_RIGHT);
    private static final Component TUL = Component.literal(Symbols.TRIANGLE_UP_LEFT);

    private void toggleShowWidgets() {
        showingAllWidgets = !showingAllWidgets;
        allWidgetsButton.setMessage(showingAllWidgets ? TDR : TUL);
        updateWidgetTrayLayout();
        if (showingAllWidgets) setFocused(filterField);
    }

    private void updateDifficulty(WidgetDifficulty difficulty) {
        this.programmerDifficulty = difficulty;
        ConfigHelper.setProgrammerDifficulty(difficulty);
        if (showingAllWidgets) {
            toggleShowWidgets();
        }
        updateWidgetTrayLayout();
    }

    private void gotoLatest() {
        if (!te.progWidgets.isEmpty()) {
            programmerUnit.gotoPiece(te.progWidgets.getLast());
        }
    }

    private void gotoStart() {
        programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
    }

    private void pastebin() {
        JsonElement json = ProgWidgetUtils.putWidgetsToJson(registryAccess(), te.progWidgets);
        minecraft.setScreen(pastebinGui = new PastebinScreen(this, json, ConversionType.PROG_WIDGET));
    }

    private void clear() {
        te.progWidgets.forEach(w -> removingWidgets.add(new RemovingWidget(w)));
        te.progWidgets.clear();
        NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
    }

    private void convertToRelative() {
        for (IProgWidget widget : te.progWidgets) {
            if (widget instanceof ProgWidgetStart) {
                generateRelativeOperators((ProgWidgetCoordinateOperator) widget.getOutputWidget(), null, false);
                break;
            }
        }
    }

    @Override
    protected PointXY getInvNameOffset() {
        return null;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth(); // 299 or 649
        int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight(); // 171 or 427

        String str = widgetPage + 1 + "/" + maxPage;
        graphics.drawString(font, str, xRight + 15 - font.width(str) / 2f, yBottom + 4, 0x404040, false);

        if (showingWidgetProgress == 0) {
            programmerUnit.renderForeground(graphics, mouseX, mouseY, draggingWidget, font);
        }

        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            if (widget != draggingWidget && mouseX - leftPos >= widget.getX()
                    && mouseY - topPos >= widget.getY() && mouseX - leftPos <= widget.getX() + widget.getWidth() / 2
                    && mouseY - topPos <= widget.getY() + widget.getHeight() / 2
                    && (!showingAllWidgets || filteredSpawnWidgets == null || filteredSpawnWidgets.get(i))) {
                List<Component> tooltip = new ArrayList<>();
                widget.getTooltip(tooltip);
                ThirdPartyManager.instance().getDocsProvider().addTooltip(tooltip, showingAllWidgets);
                if (Minecraft.getInstance().options.advancedItemTooltips) {
                    ResourceLocation id = PNCRegistries.PROG_WIDGETS_REGISTRY.getKey(widget.getType());
                    if (id != null) tooltip.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
                }
                if (!tooltip.isEmpty()) {
                    int tw = tooltip.stream().map(l -> font.width(l)).max(Comparator.naturalOrder()).orElse(0) + 16;
                    graphics.renderComponentTooltip(font, tooltip, Math.min(mouseX, minecraft.getWindow().getGuiScaledWidth() - tw) - leftPos, mouseY - topPos);
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        GuiUtils.drawNineSliced(graphics, Textures.GUI_BLANK_256, getGuiLeft(), getGuiTop(), 0, imageWidth, imageHeight, 256, 256, 4);
        GuiUtils.drawPlayerSlots(graphics, getGuiLeft() + menu.slots.get(1).x - 1, getGuiTop() + menu.slots.get(1).y - 1);
        GuiUtils.drawInsetPanel(graphics,
                getGuiLeft() + getProgrammerBounds().getX() - 1, getGuiTop() + getProgrammerBounds().getY() - 1,
                getProgrammerBounds().getWidth() + 2, getProgrammerBounds().getHeight() + 2);
        GuiUtils.drawInsetPanel(graphics, getGuiLeft() + menu.slots.get(0).x - 1, getGuiTop() + menu.slots.get(0).y - 1, 18, 18);

        programmerUnit.render(graphics, mouseX, mouseY, showFlow.checked, showInfo.checked && showingWidgetProgress == 0);

        // draw expanding widget tray
        int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth() + 20;
        int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight();
        if (showingWidgetProgress > 0) {
            int width = (int)Mth.lerp(partialTicks, (float)oldShowingWidgetProgress, (float)showingWidgetProgress);
            GuiUtils.drawNineSliced(graphics, Textures.GUI_WIDGET_TRAY, getGuiLeft() + xRight - width, getGuiTop() + 36, 0,
                    width, yBottom - 35,
                    256, 128, 2);
            if (showingAllWidgets && draggingWidget != null) {
                toggleShowWidgets();
            }
        } else {
            GuiUtils.drawNineSliced(graphics, Textures.GUI_WIDGET_TRAY, getGuiLeft() + xRight - 3, getGuiTop() + 36, 0,
                    22, yBottom - 35,
                    256, 128, 2);
        }

        // draw widgets in the widget tray
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        PoseStack poseStack = graphics.pose();
        int maxProgress = maxPage * WIDGET_X_SPACING;
        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            poseStack.pushPose();
            poseStack.translate(widget.getX() + leftPos, widget.getY() + topPos, 0);
            poseStack.scale(0.5f, 0.5f, 1f);
            int alpha = 255;
            if (showingAllWidgets && filteredSpawnWidgets != null && !filteredSpawnWidgets.get(i)) {
                alpha = 48;
            } else if (showingWidgetProgress > 0 && showingWidgetProgress < maxProgress) {
                float p = (float) showingWidgetProgress / maxProgress;
                alpha = 32 + (int) (223 * (1 - p));
            }
            ProgWidgetRenderer.renderProgWidget2d(graphics, widget, alpha);
            poseStack.popPose();
        }

        // draw the widget currently being dragged, if any
        if (draggingWidget != null) {
            float scale = programmerUnit.getScale();
            poseStack.pushPose();
            poseStack.translate(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
            poseStack.scale(scale, scale, 1f);
            poseStack.pushPose();
            poseStack.translate(draggingWidget.getX() + leftPos, draggingWidget.getY() + topPos, 0);
            poseStack.scale(0.5f, 0.5f, 1f);
            ProgWidgetRenderer.renderProgWidget2d(graphics, draggingWidget);
            poseStack.popPose();
            poseStack.popPose();
        }

        RenderSystem.disableBlend();

        if (!removingWidgets.isEmpty()) {
            drawRemovingWidgets(graphics);
        }
    }

    /**
     * This doesn't achieve much other than look really cool.
     */
    private void drawRemovingWidgets(GuiGraphics graphics) {
        Iterator<RemovingWidget> iter = removingWidgets.iterator();
        int h = minecraft.getWindow().getGuiScaledHeight();
        float scale = programmerUnit.getScale();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        poseStack.scale(scale, scale, 1f);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        while (iter.hasNext()) {
            RemovingWidget rw = iter.next();
            IProgWidget w = rw.widget;
            if (w.getY() + rw.ty > h) {
                iter.remove();
            } else {
                poseStack.pushPose();
                poseStack.translate(w.getX() + rw.tx + leftPos, w.getY() + rw.ty + topPos, 0);
                poseStack.scale(0.5f, 0.5f, 1f);
                ProgWidgetRenderer.renderProgWidget2d(graphics, w);
                poseStack.popPose();
                rw.tick();

            }
        }
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return super.keyPressed(keyCode, scanCode, modifiers);

        if (nameField.isFocused()) {
            return nameField.keyPressed(keyCode, scanCode, modifiers);
        } else if (filterField.isFocused() && keyCode != GLFW.GLFW_KEY_TAB) {
            return filterField.keyPressed(keyCode, scanCode, modifiers);
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_I -> showWidgetDocs();
            case GLFW.GLFW_KEY_R -> {
                if (exportButton.isHoveredOrFocused()) {
                    NetworkHandler.sendToServer(new PacketGuiButton("program_when"));
                }
                yield true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                toggleShowWidgets();
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (ClientUtils.hasShiftDown()) {
                    clear();
                } else {
                    IProgWidget widget = programmerUnit.getHoveredWidget(lastMouseX, lastMouseY);
                    if (widget != null) {
                        removingWidgets.add(new RemovingWidget(widget));
                        te.progWidgets.remove(widget);
                        NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
                    }
                }
                yield true;
            }
            case GLFW.GLFW_KEY_Z -> {
                NetworkHandler.sendToServer(new PacketGuiButton("undo"));
                yield true;
            }
            case GLFW.GLFW_KEY_Y -> {
                NetworkHandler.sendToServer(new PacketGuiButton("redo"));
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                gotoStart();
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                gotoLatest();
                yield true;
            }
            case GLFW.GLFW_KEY_P -> {
                pastebin();
                yield true;
            }
            default -> super.keyPressed(keyCode, scanCode, modifiers);
        };
    }

    private boolean showWidgetDocs() {
        int x = lastMouseX;
        int y = lastMouseY;

        IProgWidget hoveredWidget = programmerUnit.getHoveredWidget(x, y);
        if (hoveredWidget != null) {
            ThirdPartyManager.instance().getDocsProvider().showWidgetDocs(getWidgetId(hoveredWidget));
            return true;
        } else {
            for (IProgWidget widget : visibleSpawnWidgets) {
                if (widget != draggingWidget && x - leftPos >= widget.getX() && y - topPos >= widget.getY() && x - leftPos <= widget.getX() + widget.getWidth() / 2 && y - topPos <= widget.getY() + widget.getHeight() / 2) {
                    ThirdPartyManager.instance().getDocsProvider().showWidgetDocs(getWidgetId(widget));
                    return true;
                }
            }
        }
        return false;
    }

    private String getWidgetId(IProgWidget w) {
        return w == null ? null : w.getTypeID().getPath();
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidPlaced(IProgWidget widget1) {
        Rect2i draggingRect = new Rect2i(widget1.getX(), widget1.getY(), widget1.getWidth() / 2, widget1.getHeight() / 2);
        for (IProgWidget w : te.progWidgets) {
            if (w != widget1 && ClientUtils.intersects(draggingRect, w.getX(), w.getY(), w.getWidth() / 2.0, w.getHeight() / 2.0)) {
                return false;
            }
        }
        IProgWidget[] parameters = widget1.getConnectedParameters();
        if (parameters != null) {
            for (IProgWidget widget : parameters) {
                if (widget != null && !isValidPlaced(widget)) return false;
            }
        }
        IProgWidget outputWidget = widget1.getOutputWidget();
        return !(outputWidget != null && !isValidPlaced(outputWidget));
    }

    /**
     * Called when shift + middle-clicking: copy this widget and all connecting widgets to the side and/or below
     * (but not above).
     * @param original original widget being copied
     * @param copy new copy of the widget
     */
    private void copyWidgetRecursively(IProgWidget original, IProgWidget copy) {
        IProgWidget[] connectedWidgets = original.getConnectedParameters();
        if (connectedWidgets != null) {
            for (int i = 0; i < connectedWidgets.length; i++) {
                if (connectedWidgets[i] != null) {
                    IProgWidget connectedCopy = connectedWidgets[i].copyWidget();
                    te.progWidgets.add(connectedCopy);
                    copy.setParameter(i, connectedCopy);
                    copyWidgetRecursively(connectedWidgets[i], connectedCopy);
                }
            }
        }
        IProgWidget outputWidget = original.getOutputWidget();
        if (outputWidget != null) {
            IProgWidget outputCopy = outputWidget.copyWidget();
            te.progWidgets.add(outputCopy);
            copy.setOutputWidget(outputCopy);
            copyWidgetRecursively(outputWidget, outputCopy);
        }
    }

    /**
     * Called when a widget is placed outside the programming area: delete it and any connected widgets.
     *
     * @param widget the widget
     */
    private void deleteConnectingWidgets(IProgWidget widget) {
        te.progWidgets.remove(widget);
        removingWidgets.add(new RemovingWidget(widget));
        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if (connectingWidgets != null) {
            for (IProgWidget widg : connectingWidgets) {
                if (widg != null) deleteConnectingWidgets(widg);
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if (outputWidget != null) deleteConnectingWidgets(outputWidget);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        programmerUnit.tick();

        if (te.recentreStartPiece) {
            programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
            te.recentreStartPiece = false;
        }

        programmerUnit.getScrollBar().visible = showingWidgetProgress == 0;
        if (showingWidgetProgress > 0) {
            programmerUnit.getScrollBar().setCurrentState(programmerUnit.getLastZoom());
        }

        undoButton.active = te.canUndo;
        redoButton.active = te.canRedo;

        updateConvertRelativeState();

        oldShowingWidgetProgress = showingWidgetProgress;
        int maxProgress = maxPage * WIDGET_X_SPACING;
        if (showingAllWidgets) {
            if (showingWidgetProgress < maxProgress) {
                showingWidgetProgress += maxProgress / 5;
                if (showingWidgetProgress >= maxProgress) {
                    showingWidgetProgress = maxProgress;
                    updateWidgetTrayLayout();
                }
            } else {
                setFocused(filterField);
            }
        } else {
            showingWidgetProgress -= maxProgress / 5;
            if (showingWidgetProgress < 0) showingWidgetProgress = 0;
        }

        ItemStack programmedItem = te.getItemInProgrammingSlot();
        boolean isDeviceInserted = !programmedItem.isEmpty();
        importButton.active = isDeviceInserted;
        exportButton.active = isDeviceInserted && programmerUnit.getTotalErrors() == 0;

        updateExportButtonTooltip();

        if (!programmedItem.isEmpty()) {
            nameField.setEditable(true);
        } else {
            nameField.setEditable(false);
            nameField.setValue("");
        }
    }

    private List<Component> updateExportButtonTooltip() {
        List<Component> exportButtonTooltip = new ArrayList<>();
        exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export"));
        exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export.programmingWhen",
                xlate("pneumaticcraft.gui.programmer.button.export." + (te.programOnInsert ? "onItemInsert" : "pressingButton")))
                .withStyle(ChatFormatting.AQUA));
        exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export.pressRToChange")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        if (!te.getItemInProgrammingSlot().isEmpty()) {
            int required = te.getRequiredPuzzleCount();
            if (required != 0) exportButtonTooltip.add(Component.empty());
            int effectiveRequired = ClientUtils.getClientPlayer().isCreative() ? 0 : required;
            int available = te.availablePuzzlePieces + countPlayerPuzzlePieces();
            exportButton.active = exportButton.active && effectiveRequired <= available;
            if (required > 0) {
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.requiredPieces", effectiveRequired)
                        .withStyle(ChatFormatting.YELLOW));
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.availablePieces", available)
                        .withStyle(ChatFormatting.YELLOW));
            } else if (required < 0) {
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.returnedPieces", -effectiveRequired)
                        .withStyle(ChatFormatting.GREEN));
            }
            if (required != 0 && ClientUtils.getClientPlayer().isCreative()) {
                exportButtonTooltip.add(Component.literal("(Creative mode)").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            if (effectiveRequired > available) {
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.notEnoughPieces")
                        .withStyle(ChatFormatting.RED));
            }
        } else {
            exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export.noProgrammableItem")
                    .withStyle(ChatFormatting.GOLD));
        }

        if (programmerUnit.getTotalErrors() > 0) {
            exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.errorCount", programmerUnit.getTotalErrors()).withStyle(ChatFormatting.RED));
        }
        if (programmerUnit.getTotalWarnings() > 0)
            exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.warningCount", programmerUnit.getTotalWarnings()).withStyle(ChatFormatting.YELLOW));

        return exportButtonTooltip;
    }

    private int countPlayerPuzzlePieces() {
        int count = 0;
        for (ItemStack stack : ClientUtils.getClientPlayer().getInventory().items) {
            if (stack.getItem() == ModItems.PROGRAMMING_PUZZLE.get()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void updateConvertRelativeState() {
        convertToRelativeButton.visible = programmerDifficulty == WidgetDifficulty.ADVANCED;
        rotateCoordsButton.visible = programmerDifficulty == WidgetDifficulty.ADVANCED;
        if (programmerDifficulty != WidgetDifficulty.ADVANCED) {
            return;
        }

        convertToRelativeButton.active = false;
        rotateCoordsButton.active = false;
    }

    private List<Component> buildConvertButtonTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.desc"));

        IProgWidget startWidget = findWidget(te.progWidgets, ProgWidgetStart.class);
        if (startWidget == null) {
            tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.noStartPiece").withStyle(ChatFormatting.RED));
        } else {
            IProgWidget widget = startWidget.getOutputWidget();
            if (widget instanceof ProgWidgetCoordinateOperator operatorWidget) {
                if (operatorWidget.getVariable().isEmpty()) {
                    tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.noVariableName").withStyle(ChatFormatting.RED));
                } else {
                    try {
                        rotateCoordsButton.active = true;
                        if (generateRelativeOperators(operatorWidget, tooltip, true)) {
                            convertToRelativeButton.active = true;
                        } else {
                            tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.notEnoughRoom").withStyle(ChatFormatting.RED));
                        }
                    } catch (NullPointerException e) {
                        tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.cantHaveVariables").withStyle(ChatFormatting.RED));
                    }
                }

            } else {
                tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.noBaseCoordinate").withStyle(ChatFormatting.RED));
            }
        }

        return tooltip;
//        convertToRelativeButton.setTooltipText(PneumaticCraftUtils.combineComponents(tooltip));
    }

    private boolean generateRelativeOperators(ProgWidgetCoordinateOperator baseWidget, List<Component> tooltip, boolean simulate) {
        BlockPos baseCoord = ProgWidgetCoordinateOperator.calculateCoordinate(baseWidget, 0, baseWidget.getOperator());
        Map<BlockPos, String> offsetToVariableNames = new HashMap<>();

        for (IProgWidget widget : te.progWidgets) {
            if (widget instanceof ProgWidgetArea area) {
                if (area.getVarName(0).isEmpty()) {
                    area.getPos(0).ifPresent(pos -> {
                        BlockPos offset = pos.subtract(baseCoord);
                        String var = makeOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                        if (!simulate) area.setVarName(0, var);
                    });
                }
                if (area.getVarName(1).isEmpty()) {
                    area.getPos(1).ifPresent(pos -> {
                        BlockPos offset = pos.subtract(baseCoord);
                        String var = makeOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                        if (!simulate) area.setVarName(1, var);
                    });
                }
            } else if (widget instanceof ProgWidgetCoordinate coordinate && baseWidget.getConnectedParameters()[0] != widget) {
                if (!coordinate.isUsingVariable()) {
                    BlockPos coord = coordinate.getCoordinate().orElse(BlockPos.ZERO);
                    String coordStr = PneumaticCraftUtils.posToString(coord);
                    if (PneumaticCraftUtils.distBetweenSq(coord, 0, 0, 0) < 4096) {
                        // When the coordinate value is close to 0, there's a low chance it means a position, and rather an offset.
                        if (tooltip != null)
                            tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.coordIsNotChangedWarning", coordStr)
                                    .withStyle(ChatFormatting.YELLOW));
                    } else {
                        if (tooltip != null)
                            tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.coordIsChangedWarning", coordStr)
                                    .withStyle(ChatFormatting.YELLOW));
                        if (!simulate) {
                            BlockPos offset = coord.subtract(baseCoord);
                            String var = makeOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                            coordinate.setVariable(var);
                            coordinate.setUsingVariable(true);
                        }
                    }
                }
            }
        }

        if (!offsetToVariableNames.isEmpty()) {
            ProgWidgetCoordinateOperator firstOperator = null;
            ProgWidgetCoordinateOperator prevOperator = baseWidget;
            int x = baseWidget.getX();
            for (Map.Entry<BlockPos, String> entry : offsetToVariableNames.entrySet()) {
                ProgWidgetCoordinateOperator operator = new ProgWidgetCoordinateOperator();
                operator.setVariable(entry.getValue());

                int y = prevOperator.getY() + prevOperator.getHeight() / 2;
                operator.setPosition(x, y);
                if (!isValidPlaced(operator)) return false;

                ProgWidgetCoordinate coord1 = new ProgWidgetCoordinate();
                coord1.setPosition(x + prevOperator.getWidth() / 2, y);
                coord1.setVariable(baseWidget.getVariable());
                coord1.setUsingVariable(true);
                if (!isValidPlaced(coord1)) return false;

                ProgWidgetCoordinate coord2 = new ProgWidgetCoordinate();
                coord2.setPosition(x + prevOperator.getWidth() / 2 + coord1.getWidth() / 2, y);
                coord2.setCoordinate(entry.getKey());
                if (!isValidPlaced(coord2)) return false;

                if (!simulate) {
                    te.progWidgets.add(operator);
                    te.progWidgets.add(coord1);
                    te.progWidgets.add(coord2);
                }
                if (firstOperator == null) firstOperator = operator;
                prevOperator = operator;
            }
            if (!simulate) {
                NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
                ProgWidgetUtils.updatePuzzleConnections(te.progWidgets);
            }
        }
        return true;
    }

    private void rotateCoords90() {
        IProgWidget startWidget = findWidget(te.progWidgets, ProgWidgetStart.class);
        if (startWidget == null) {
            return;
        }
        boolean changed = false;
        if (startWidget.getOutputWidget() instanceof ProgWidgetCoordinateOperator baseWidget && !baseWidget.getVariable().isEmpty()) {
            String varName = baseWidget.getVariable();
            for (IProgWidget widget : te.progWidgets) {
                // iterate through all add/sub coordinate operators; try to find a coordinate on the right
                // whose variable matches the base widget variable,
                // and then at least one more coordinate to the right of that
                // if that matches, assume it's an offset, and rotate it: (x,z) -> (z,-x)
                if (widget instanceof ProgWidgetCoordinateOperator oper && oper.getOperator() == EnumOperator.PLUS_MINUS) {
                    if (oper.getConnectedParameters()[0] instanceof ProgWidgetCoordinate c1
                            && c1.isUsingVariable() && varName.equals(c1.getVariable())
                            && c1.getConnectedParameters()[0] instanceof ProgWidgetCoordinate) {
                        while (c1.getConnectedParameters()[0] instanceof ProgWidgetCoordinate c2) {
                            BlockPos pos = c2.getCoordinate().orElse(BlockPos.ZERO);
                            c2.setCoordinate(new BlockPos(pos.getZ(), pos.getY(), -pos.getX()));
                            c1 = c2;
                        }
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
        }
    }

    private String makeOffsetVariable(Map<BlockPos, String> offsetToVariableNames, String baseVariable, BlockPos offset) {
        if (offset.equals(BlockPos.ZERO))
            return baseVariable;
        return offsetToVariableNames.computeIfAbsent(offset, k -> "var" + (offsetToVariableNames.size() + 1));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final float scale = programmerUnit.getScale();
        final double trMouseX = (mouseX - programmerUnit.getTranslatedX()) / scale;
        final double trMouseY = (mouseY - programmerUnit.getTranslatedY()) / scale;

        if (button == 0) {
            // left-click
            IProgWidget hovered = programmerUnit.getHoveredWidget((int) mouseX, (int) mouseY);
            ItemStack heldItem = ClientUtils.getClientPlayer().containerMenu.getCarried();
            if (heldItem.getItem() instanceof IPositionProvider) {
                return mouseClickedWithPosProvider(trMouseX, trMouseY, hovered, heldItem);
            } if (!heldItem.isEmpty()) {
                return mouseClickedWithItem(trMouseX, trMouseY, hovered, heldItem);
            } else {
                if (mouseClickedEmpty(trMouseX, trMouseY, mouseX, mouseY, scale, hovered))
                    return true;
            }
        } else if (button == 2) {
            // middle-click: copy widget, or show docs if clicked on widget tray
            if (showingWidgetProgress == 0) {
                IProgWidget widget = programmerUnit.getHoveredWidget((int) mouseX, (int) mouseY);
                if (widget != null) {
                    draggingWidget = widget.copyWidget();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = trMouseX - leftPos;
                    dragMouseStartY = trMouseY - topPos;
                    dragWidgetStartX = widget.getX();
                    dragWidgetStartY = widget.getY();
                    if (Screen.hasShiftDown()) copyWidgetRecursively(widget, draggingWidget);
                    return true;
                }
            } else {
                return showWidgetDocs();
            }
        } else if (button == 1 && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget((int)mouseX, (int)mouseY);
            if (widget != null) {
                // right-click a prog widget: show its options screen, if any
                AbstractProgWidgetScreen<?> gui = ProgWidgetGuiManager.getGui(widget, this);
                if (gui != null) {
                    minecraft.setScreen(gui);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean mouseClickedWithPosProvider(double mouseX, double mouseY, IProgWidget hovered, ItemStack heldItem) {
        ProgWidgetArea areaToolWidget = heldItem.getItem() instanceof GPSAreaToolItem ? GPSAreaToolItem.getArea(minecraft.player, heldItem) : null;
        if (hovered != null) {
            // clicked an existing widget: update any area or coordinate widgets from the held item
            if (areaToolWidget != null && hovered instanceof ProgWidgetArea hoveredArea) {
                hoveredArea.updateFrom(areaToolWidget);
                hoveredArea.setPos(0, areaToolWidget.getPos(0).orElse(null));
                hoveredArea.setPos(1, areaToolWidget.getPos(1).orElse(null));
                hoveredArea.setVarName(0, areaToolWidget.getVarName(0));
                hoveredArea.setVarName(1, areaToolWidget.getVarName(1));
                NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
            } else if (heldItem.getItem() == ModItems.GPS_TOOL.get()) {
                if (hovered instanceof ProgWidgetCoordinate) {
                    ((ProgWidgetCoordinate) hovered).loadFromGPSTool(heldItem);
                } else if (hovered instanceof ProgWidgetArea areaHovered) {
                    GPSToolItem.getGPSLocation(ClientUtils.getClientPlayer().getUUID(), heldItem).ifPresent(gpsPos -> {
                        areaHovered.setPos(0, gpsPos);
                        areaHovered.setPos(1, gpsPos);
                    });
                    areaHovered.setVarName(0, GPSToolItem.getVariable(heldItem));
                    areaHovered.setVarName(1, GPSToolItem.getVariable(heldItem));
                }
                NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
            }
        } else {
            // clicked on an empty area: create a new area or coordinate widget(s)
            List<IProgWidget> toCreate = new ArrayList<>();
            if (areaToolWidget != null) {
                if (Screen.hasShiftDown()) {
                    toCreate.add(ProgWidgetCoordinate.fromPos(areaToolWidget.getPos(0).orElse(BlockPos.ZERO)));
                    areaToolWidget.getPos(1).ifPresent(pos -> toCreate.add(ProgWidgetCoordinate.fromPos(pos)));
                } else {
                    toCreate.add(areaToolWidget);
                }
            } else if (heldItem.getItem() == ModItems.GPS_TOOL.get()) {
                if (Screen.hasShiftDown()) {
                    ProgWidgetArea areaWidget = ProgWidgetArea.fromPosition(GPSToolItem.getGPSLocation(heldItem).orElse(BlockPos.ZERO));
                    String var = GPSToolItem.getVariable(heldItem);
                    if (!var.isEmpty()) areaWidget.setVarName(0, var);
                    toCreate.add(areaWidget);
                } else {
                    toCreate.add(ProgWidgetCoordinate.fromGPSTool(heldItem));
                }
            }
            int n = te.progWidgets.size();
            for (int i = 0; i < toCreate.size(); i++) {
                IProgWidget p = toCreate.get(i);
                p.setPosition((int) (mouseX - leftPos - p.getWidth() / 3d), (int) (mouseY - topPos - p.getHeight() / 4d) + i * p.getHeight());
                if (!programmerUnit.isOutsideProgrammingArea(p) && isValidPlaced(p)) {
                    te.progWidgets.add(p);
                }
            }
            if (te.progWidgets.size() > n) {
                NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
            }
        }
        return true;
    }

    private boolean mouseClickedWithItem(double mouseX, double mouseY, IProgWidget hovered, ItemStack heldItem) {
        // mouse clicked with an item on the cursor, maybe create or update an item filter widget
        if (hovered == null) {
            ProgWidgetItemFilter p = new ProgWidgetItemFilter();
            p.setFilter(heldItem.copy());
            p.setPosition((int) (mouseX - leftPos - p.getWidth() / 3d), (int) (mouseY - topPos - p.getHeight() / 4d));
            if (!programmerUnit.isOutsideProgrammingArea(p)) {
                te.progWidgets.add(p);
            }
            NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
            return true;
        } else if (hovered instanceof ProgWidgetItemFilter p) {
            p.setFilter(heldItem.copy());
            NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
            return true;
        }
        return false;
    }

    private boolean mouseClickedEmpty(double mouseX, double mouseY, double origX, double origY, float scale, IProgWidget hovered) {
        // just a regular click, nothing of interest held
        if (showingAllWidgets || origX > leftPos + getProgrammerBounds().getX() + getProgrammerBounds().getWidth()) {
            // clicking on a widget in the tray?
            for (IProgWidget widget : visibleSpawnWidgets) {
                if (origX >= widget.getX() + leftPos
                        && origY >= widget.getY() + topPos
                        && origX <= widget.getX() + leftPos + widget.getWidth() / 2f
                        && origY <= widget.getY() + topPos + widget.getHeight() / 2f)
                {
                    draggingWidget = widget.copyWidget();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = mouseX - (int) (leftPos / scale);
                    dragMouseStartY = mouseY - (int) (topPos / scale);
                    dragWidgetStartX = (int) ((widget.getX() - programmerUnit.getTranslatedX()) / scale);
                    dragWidgetStartY = (int) ((widget.getY() - programmerUnit.getTranslatedY()) / scale);
                    return true;
                }
            }
        }
        if (hovered != null) {
            // clicking on a widget in the main area
            draggingWidget = hovered;
            dragMouseStartX = mouseX - leftPos;
            dragMouseStartY = mouseY - topPos;
            dragWidgetStartX = hovered.getX();
            dragWidgetStartY = hovered.getY();
            return true;
        } else if (getProgrammerBounds().contains((int) origX - leftPos, (int) origY - topPos)) {
            draggingBG = true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingBG = false;
        if (draggingWidget != null) {
            if (programmerUnit.isOutsideProgrammingArea(draggingWidget)) {
                deleteConnectingWidgets(draggingWidget);
            } else {
                ProgWidgetUtils.snapWidgetIntoProgram(draggingWidget, te.progWidgets);
                if (!isValidPlaced(draggingWidget)) {
                    ProgWidgetUtils.positionConnectedWidgets(draggingWidget, (int)dragWidgetStartX, (int)dragWidgetStartY);
                    if (programmerUnit.isOutsideProgrammingArea(draggingWidget) || !isValidPlaced(draggingWidget))
                        deleteConnectingWidgets(draggingWidget);
                }
            }
            NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(te));
            ProgWidgetUtils.updatePuzzleConnections(te.progWidgets);
            draggingWidget = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        return programmerUnit.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (draggingWidget != null) {
            mouseX = (mouseX - programmerUnit.getTranslatedX()) / programmerUnit.getScale();
            mouseY = (mouseY - programmerUnit.getTranslatedY()) / programmerUnit.getScale();
            ProgWidgetUtils.positionConnectedWidgets(draggingWidget,
                    (int)(mouseX - dragMouseStartX + dragWidgetStartX - leftPos),
                    (int)(mouseY - dragMouseStartY + dragWidgetStartY - topPos));
            return true;
        } else if (draggingBG) {
            return programmerUnit.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        } else {
            return false;
        }
    }

    @Override
    public void removed() {
        te.translatedX = programmerUnit.getTranslatedX();
        te.translatedY = programmerUnit.getTranslatedY();
        te.zoomState = programmerUnit.getLastZoom();
        te.showFlow = showFlow.checked;
        te.showInfo = showInfo.checked;
    }

    public static IProgWidget findWidget(List<IProgWidget> widgets, Class<? extends IProgWidget> cls) {
        return widgets.stream()
                .filter(w -> cls.isAssignableFrom(w.getClass()))
                .findFirst()
                .orElse(null);
    }

    public PointXY mouseToWidgetCoords(double mouseX, double mouseY, ProgWidgetItemFilter p) {
        float scale = programmerUnit.getScale();
        mouseX = (mouseX - programmerUnit.getTranslatedX()) / scale;
        mouseY = (mouseY - programmerUnit.getTranslatedY()) / scale;
        return new PointXY((int) (mouseX - leftPos - p.getWidth() / 3d), (int) (mouseY - topPos - p.getHeight() / 4d));
    }

    public boolean isVisible(IProgWidget w) {
        return !programmerUnit.isOutsideProgrammingArea(w);
    }

    private static class FilterTextField extends WidgetTextField {
        FilterTextField(Font font, int x, int y, int width, int height) {
            super(font, x, y, width, height);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int x, int y, float partialTicks) {
            // this is needed to force the textfield to draw on top of any
            // widgets in the programming area
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
            super.renderWidget(graphics, x, y, partialTicks);
            graphics.pose().popPose();
        }
    }

    private static class DifficultyButton extends WidgetRadioButton {
        final WidgetDifficulty difficulty;

        DifficultyButton(int x, int y, int color, WidgetDifficulty difficulty, Consumer<WidgetRadioButton> pressable) {
            super(x, y, color, xlate(difficulty.getTranslationKey()), pressable);
            this.difficulty = difficulty;
        }
    }

    private static class RemovingWidget {
        final IProgWidget widget;
        double ty = 0;
        double tx = 0;
        final double velX = (ClientUtils.getClientLevel().random.nextDouble() - 0.5) * 3;
        double velY = -4;

        private RemovingWidget(IProgWidget widget) {
            this.widget = widget;
        }

        public void tick() {
            tx += velX;
            ty += velY;
            velY += 0.35;
        }
    }

    private static IntIntPair calculateScreenSize() {
        Window window = Minecraft.getInstance().getWindow();
        int guiWidth = window.getGuiScaledWidth() * 75 / 100;  // leave a little space on the side for JEI etc.
        int guiHeight = window.getGuiScaledHeight() * 39 / 40;
        //noinspection SuspiciousNameCombination
        return IntIntPair.of(guiWidth, guiHeight);
    }

    public static ProgrammerMenu.AreaGeometry calculateAreaBounds() {
        IntIntPair size = calculateScreenSize();
        return new ProgrammerMenu.AreaGeometry(6, 18, size.firstInt() - 53, size.secondInt() - 105);
    }
}
