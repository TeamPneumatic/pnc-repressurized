package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetOptionBase;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetGuiManager;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammer;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GuiProgrammer extends GuiPneumaticContainerBase<ContainerProgrammer,TileEntityProgrammer> {
    private GuiPastebin pastebinGui;

    private WidgetButtonExtended importButton;
    private WidgetButtonExtended exportButton;
    private WidgetButtonExtended allWidgetsButton;
    private List<WidgetRadioButton> difficultyButtons;
    private WidgetCheckBox showInfo, showFlow;
    private WidgetTextField nameField;
    private WidgetTextField filterField;
    private WidgetButtonExtended undoButton, redoButton;
    private WidgetButtonExtended convertToRelativeButton;

    // those widgets currently visible in the tray
    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<>();
    // widgets being deleted (visual only)
    private final List<RemovingWidget> removingWidgets = new ArrayList<>();
    private BitSet filteredSpawnWidgets;

    private GuiUnitProgrammer programmerUnit;
    private IProgWidget draggingWidget;
    private int lastMouseX, lastMouseY;
    private double dragMouseStartX, dragMouseStartY;
    private double dragWidgetStartX, dragWidgetStartY;
    private static final int FAULT_MARGIN = 4;
    private int widgetPage;
    private int maxPage;

    private boolean showingAllWidgets;
    private int showingWidgetProgress;
    private int oldShowingWidgetProgress;

    private static final Rectangle2d PROGRAMMER_STD_RES = new Rectangle2d(5, 17, 294, 154);
    private static final Rectangle2d PROGRAMMER_HI_RES = new Rectangle2d(5, 17, 644, 410);

    private static final int WIDGET_X_SPACING = 22; // x size of widgets in the widget tray

    private boolean hiRes;

    public GuiProgrammer(ContainerProgrammer container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        hiRes = container.isHiRes();
        xSize = hiRes ? 700 : 350;
        ySize = hiRes ? 512 : 256;
    }

    @Override
    public void init() {
        super.init();

        if (pastebinGui != null && pastebinGui.outputTag != null) {
            if (pastebinGui.shouldMerge) {
                List<IProgWidget> newWidgets = te.mergeWidgetsFromNBT(pastebinGui.outputTag);
                TileEntityProgrammer.updatePuzzleConnections(newWidgets);
                te.setProgWidgets(newWidgets, ClientUtils.getClientPlayer());
            } else {
                te.readProgWidgetsFromNBT(pastebinGui.outputTag);
            }
            pastebinGui = null;
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            te.recentreStartPiece = true;
        }

        if (programmerUnit != null) {
            te.translatedX = programmerUnit.getTranslatedX();
            te.translatedY = programmerUnit.getTranslatedY();
            te.zoomState = programmerUnit.getLastZoom();
        }

        Rectangle2d bounds = getProgrammerBounds();
        programmerUnit = new GuiUnitProgrammer(te.progWidgets, font, guiLeft, guiTop, width, height,
                bounds, te.translatedX, te.translatedY, te.zoomState);
        addButton(programmerUnit.getScrollBar());

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        // right and bottom edges of the programming area
        int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth(); // 299 or 649
        int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight() + 3; // 171 or 427

        importButton = new WidgetButtonExtended(xStart + xRight + 2, yStart + 3, 20, 15, GuiConstants.ARROW_LEFT).withTag("import");
        importButton.setTooltipText(PneumaticCraftUtils.splitString(I18n.format("gui.programmer.button.import"), 40));
        addButton(importButton);

        exportButton = new WidgetButtonExtended(xStart + xRight + 2, yStart + 20, 20, 15, GuiConstants.ARROW_RIGHT).withTag("export");
        addButton(exportButton);

        addButton(new WidgetButtonExtended(xStart + xRight - 3, yStart + yBottom, 13, 10, GuiConstants.TRIANGLE_LEFT, b -> adjustPage(-1)));
        addButton(new WidgetButtonExtended(xStart + xRight + 34, yStart + yBottom, 13, 10, GuiConstants.TRIANGLE_RIGHT, b -> adjustPage(1)));

        allWidgetsButton = new WidgetButtonExtended(xStart + xRight + 22, yStart + yBottom - 16, 10, 10, GuiConstants.TRIANGLE_UP_LEFT, b -> toggleShowWidgets());
        allWidgetsButton.setTooltipText(I18n.format("gui.programmer.button.openPanel.tooltip"));
        addButton(allWidgetsButton);

        difficultyButtons = new ArrayList<>();
        for (WidgetDifficulty difficulty : WidgetDifficulty.values()) {
            DifficultyButton dButton = new DifficultyButton(xStart + xRight - 36, yStart + yBottom + 29 + difficulty.ordinal() * 12,
                    0xFF404040, difficulty, b -> updateDifficulty(difficulty));
            dButton.checked = difficulty == PNCConfig.Client.programmerDifficulty;
            addButton(dButton);
            difficultyButtons.add(dButton);
            dButton.otherChoices = difficultyButtons;
            dButton.setTooltip("gui.programmer.difficulty." + difficulty.toString().toLowerCase() + ".tooltip");
        }

        addButton(new WidgetButtonExtended(xStart + 5, yStart + yBottom + 4, 87, 20,
                I18n.format("gui.programmer.button.showStart"), b -> gotoStart())
                .setTooltipText(I18n.format("gui.programmer.button.showStart.tooltip")));
        addButton(new WidgetButtonExtended(xStart + 5, yStart + yBottom + 26, 87, 20,
                I18n.format("gui.programmer.button.showLatest"), b -> gotoLatest())
                .setTooltipText(I18n.format("gui.programmer.button.showLatest.tooltip")));
        addButton(showInfo = new WidgetCheckBox(xStart + 5, yStart + yBottom + 49, 0xFF404040,
                "gui.programmer.checkbox.showInfo").setChecked(te.showInfo));
        addButton(showFlow = new WidgetCheckBox(xStart + 5, yStart + yBottom + 61, 0xFF404040,
                "gui.programmer.checkbox.showFlow").setChecked(te.showFlow));

        WidgetButtonExtended pastebinButton = new WidgetButtonExtended(guiLeft - 24, guiTop + 44, 20, 20, "",
                b -> pastebin());
        pastebinButton.setTooltipText(I18n.format("gui.remote.button.pastebinButton"));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        addButton(pastebinButton);

        undoButton = new WidgetButtonExtended(guiLeft - 24, guiTop + 2, 20, 20, "").withTag("undo");
        redoButton = new WidgetButtonExtended(guiLeft - 24, guiTop + 23, 20, 20, "").withTag("redo");
        WidgetButtonExtended clearAllButton = new WidgetButtonExtended(guiLeft - 24, guiTop + 65, 20, 20, "", b -> clear());
        convertToRelativeButton = new WidgetButtonExtended(guiLeft - 24, guiTop + 86, 20, 20, "Rel", b -> convertToRelative());

        undoButton.setRenderedIcon(Textures.GUI_UNDO_ICON_LOCATION);
        redoButton.setRenderedIcon(Textures.GUI_REDO_ICON_LOCATION);
        clearAllButton.setRenderedIcon(Textures.GUI_DELETE_ICON_LOCATION);

        undoButton.setTooltipText(I18n.format("gui.programmer.button.undoButton.tooltip"));
        redoButton.setTooltipText(I18n.format("gui.programmer.button.redoButton.tooltip"));
        clearAllButton.setTooltipText(I18n.format("gui.programmer.button.clearAllButton.tooltip"));

        addButton(undoButton);
        addButton(redoButton);
        addButton(clearAllButton);
        addButton(convertToRelativeButton);

        addLabel(title.getFormattedText(), guiLeft + 7, guiTop + 5, 0xFF404040);

        nameField = new WidgetTextField(font, guiLeft + xRight - 99, guiTop + 5, 98, font.FONT_HEIGHT);
        nameField.setResponder(s -> updateDroneName());
        addButton(nameField);

        filterField = new FilterTextField(font, guiLeft + 78, guiTop + 26, 100, font.FONT_HEIGHT);
        filterField.setResponder(s -> filterSpawnWidgets());

        addButton(filterField);

        String name = I18n.format("gui.programmer.name");
        addLabel(name, guiLeft + xRight - 102 - font.getStringWidth(name), guiTop + 5, 0xFF404040);

        updateVisibleProgWidgets();

        for (IProgWidget widget : te.progWidgets) {
            if (!programmerUnit.isOutsideProgrammingArea(widget)) {
                return;
            }
        }
        programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
    }

    public static void onCloseFromContainer() {
        if (Minecraft.getInstance().currentScreen instanceof GuiProgrammer) {
            GuiProgrammer p = (GuiProgrammer) Minecraft.getInstance().currentScreen;
            p.onClose();
        }
    }

    private Rectangle2d getProgrammerBounds() {
        return hiRes ? PROGRAMMER_HI_RES : PROGRAMMER_STD_RES;
    }

    private int getWidgetTrayRight() {
        return hiRes ? 672 : 322;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return hiRes ? Textures.GUI_PROGRAMMER_LARGE : Textures.GUI_PROGRAMMER_STD;
    }

    private void updateVisibleProgWidgets() {
        int y = 0, page = 0;
        int x = getWidgetTrayRight() - maxPage * WIDGET_X_SPACING;
        boolean showAllWidgets = showingWidgetProgress == WIDGET_X_SPACING * maxPage && showingAllWidgets;
        filterField.setVisible(showAllWidgets);

        maxPage = 0;
        visibleSpawnWidgets.clear();
        int difficulty = 0;
        for (int i = 0; i < difficultyButtons.size(); i++) {
            if (difficultyButtons.get(i).checked) {
                difficulty = i;
                break;
            }
        }
        int i = 0;
        for (Supplier<? extends ProgWidgetType> type : ModProgWidgets.WIDGET_LIST) {
            IProgWidget widget = IProgWidget.create(type.get());
            if (difficulty >= widget.getDifficulty().ordinal()) {
                widget.setY(y + 40);
                widget.setX(showAllWidgets ? x : getWidgetTrayRight());
                int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
                y += widgetHeight;

                if (showAllWidgets || page == widgetPage) {
                    visibleSpawnWidgets.add(widget);
                }
                if (y > ySize - (hiRes ? 260 : 160)) {
                    y = 0;
                    x += WIDGET_X_SPACING;
                    page++;
                    if (i < ModProgWidgets.WIDGET_LIST.size() - 1) maxPage++;
                }
            }
            i++;
        }
        maxPage++;

        filterField.x = Math.min(guiLeft + getWidgetTrayRight() - 25 - filterField.getWidth(), guiLeft + getWidgetTrayRight() - (maxPage * WIDGET_X_SPACING) - 2);
        filterSpawnWidgets();

        if (widgetPage >= maxPage) {
            widgetPage = maxPage - 1;
            updateVisibleProgWidgets();
        }
    }

    private void filterSpawnWidgets() {
        String filterText = filterField.getText().trim();
        if (!visibleSpawnWidgets.isEmpty() && !filterText.isEmpty()) {
            filteredSpawnWidgets = new BitSet(visibleSpawnWidgets.size());
            for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
                IProgWidget widget = visibleSpawnWidgets.get(i);
                String widgetName = I18n.format(widget.getTranslationKey());
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
        if (stack != ItemStack.EMPTY && !stack.getDisplayName().getUnformattedComponentText().equals(nameField.getText())) {
            stack.setDisplayName(new StringTextComponent(nameField.getText()));
            sendDelayed(5);
        }
    }

    @Override
    protected void doDelayedAction() {
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    private void adjustPage(int dir) {
        widgetPage += dir;
        if (widgetPage < 0) widgetPage = maxPage -1;
        else if (widgetPage >= maxPage) widgetPage = 0;
        updateVisibleProgWidgets();
    }

    private void toggleShowWidgets() {
        showingAllWidgets = !showingAllWidgets;
        allWidgetsButton.setMessage(showingAllWidgets ? GuiConstants.TRIANGLE_DOWN_RIGHT : GuiConstants.TRIANGLE_UP_LEFT);
        updateVisibleProgWidgets();
        filterField.setFocused2(showingAllWidgets);
    }

    private void updateDifficulty(WidgetDifficulty difficulty) {
        ConfigHelper.setProgrammerDifficulty(difficulty);
        if (showingAllWidgets) toggleShowWidgets();
        updateVisibleProgWidgets();
    }

    private void gotoLatest() {
        if (te.progWidgets.size() > 0) {
            programmerUnit.gotoPiece(te.progWidgets.get(te.progWidgets.size() - 1));
        }
    }

    private void gotoStart() {
        programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
    }

    private void pastebin() {
        CompoundNBT mainTag = te.writeProgWidgetsToNBT(new CompoundNBT());
        minecraft.displayGuiScreen(pastebinGui = new GuiPastebin(this, mainTag));
    }

    private void clear() {
        te.progWidgets.forEach(w -> removingWidgets.add(new RemovingWidget(w)));
        te.progWidgets.clear();
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
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
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth(); // 299 or 649
        int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight(); // 171 or 427

        String str = widgetPage + 1 + "/" + maxPage;
        font.drawString(str, xRight + (22 - font.getStringWidth(str) / 2f), yBottom + 4, 0xFF404040);
        font.drawString(I18n.format("gui.programmer.difficulty"), xRight - 36, yBottom + 20, 0xFF404040);

        if (showingWidgetProgress == 0) {
            programmerUnit.renderForeground(x, y, draggingWidget);
        }

        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            if (widget != draggingWidget && x - guiLeft >= widget.getX()
                    && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2
                    && y - guiTop <= widget.getY() + widget.getHeight() / 2
                    && (!showingAllWidgets || filteredSpawnWidgets == null || filteredSpawnWidgets.get(i))) {
                List<ITextComponent> tooltip = new ArrayList<>();
                widget.getTooltip(tooltip);
                ThirdPartyManager.instance().docsProvider.addTooltip(tooltip, showingAllWidgets);
                if (!tooltip.isEmpty()) {
                    drawHoveringString(tooltip.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()), x - guiLeft, y - guiTop, font);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderBackground();
        bindGuiTexture();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        blit(xStart, yStart, 0, 0, xSize, ySize, xSize, ySize);
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        programmerUnit.render(mouseX, mouseY, showFlow.checked, showInfo.checked && showingWidgetProgress == 0);

        // draw expanding widget tray
        if (showingWidgetProgress > 0) {
            int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth(); // 299 or 649
            int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight(); // 171 or 427

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            int width = (int)MathHelper.lerp(partialTicks, (float)oldShowingWidgetProgress, (float)showingWidgetProgress);
            for (int i = 0; i < width; i++) {
                blit(xStart + xRight + 21 - i, yStart + 36, xRight + 24, 36, 1, yBottom - 35, xSize, ySize);
            }
            blit(xStart + xRight + 20 - width, yStart + 36, xRight + 20, 36, 2, yBottom - 35, xSize, ySize);

            if (showingAllWidgets && draggingWidget != null) toggleShowWidgets();
        }
        // draw widgets in the widget tray
        GlStateManager.enableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            GlStateManager.pushMatrix();
            GlStateManager.translated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GlStateManager.scaled(0.5, 0.5, 1);
            if (showingAllWidgets && filteredSpawnWidgets != null && !filteredSpawnWidgets.get(i)) {
                GlStateManager.color4f(1, 1, 1, 0.2f);
            } else {
                GlStateManager.color4f(1, 1, 1, 1);
            }
            widget.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();

        // draw the widget currently being dragged, if any
        float scale = programmerUnit.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translated(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        GlStateManager.scaled(scale, scale, 1);
        if (draggingWidget != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translated(draggingWidget.getX() + guiLeft, draggingWidget.getY() + guiTop, 0);
            GlStateManager.scaled(0.5, 0.5, 1);
            draggingWidget.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        if (!removingWidgets.isEmpty()) drawRemovingWidgets();
    }

    /**
     * This doesn't achieve much other than look really cool.
     */
    private void drawRemovingWidgets() {
        Iterator<RemovingWidget> iter = removingWidgets.iterator();
        int h = minecraft.mainWindow.getScaledHeight();
        float scale = programmerUnit.getScale();
        GlStateManager.pushMatrix();
        GlStateManager.translated(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        GlStateManager.scaled(scale, scale, 1);
        while (iter.hasNext()) {
            RemovingWidget rw = iter.next();
            IProgWidget w = rw.widget;
            if (w.getY() + rw.ty > h / scale) {
                iter.remove();
            } else {
                GlStateManager.pushMatrix();
                GlStateManager.translated(w.getX() + rw.tx + guiLeft, w.getY() + rw.ty + guiTop, 0);
                GlStateManager.scaled(0.5, 0.5, 1);
                w.render();
                GlStateManager.popMatrix();
                rw.ty += rw.velY;
                rw.tx += rw.velX;
                rw.velY += 0.3;

            }
        }
        GlStateManager.popMatrix();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return super.keyPressed(keyCode, scanCode, modifiers);

        if (nameField.isFocused()) {
            return nameField.keyPressed(keyCode, scanCode, modifiers);
        } else if (filterField.isFocused() && keyCode != GLFW.GLFW_KEY_TAB) {
            return filterField.keyPressed(keyCode, scanCode, modifiers);
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_I:
                return showWidgetDocs();
            case GLFW.GLFW_KEY_R:
                if (exportButton.isHovered()) {
                    NetworkHandler.sendToServer(new PacketGuiButton("redstone"));
                }
                return true;
            case GLFW.GLFW_KEY_TAB:
                toggleShowWidgets();
                return true;
            case GLFW.GLFW_KEY_DELETE:
                if (ClientUtils.hasShiftDown()) {
                    clear();
                } else {
                    IProgWidget widget = programmerUnit.getHoveredWidget(lastMouseX, lastMouseY);
                    if (widget != null) {
                        removingWidgets.add(new RemovingWidget(widget));
                        te.progWidgets.remove(widget);
                        NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
                    }
                }
                return true;
            case GLFW.GLFW_KEY_Z:
                NetworkHandler.sendToServer(new PacketGuiButton("undo"));
                return true;
            case GLFW.GLFW_KEY_Y:
                NetworkHandler.sendToServer(new PacketGuiButton("redo"));
                return true;
            case GLFW.GLFW_KEY_HOME:
                gotoStart();
                break;
            case GLFW.GLFW_KEY_END:
                gotoLatest();
                break;

        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean showWidgetDocs() {
        int x = lastMouseX;
        int y = lastMouseY;

        IProgWidget hoveredWidget = programmerUnit.getHoveredWidget(x, y);
        if (hoveredWidget != null) {
            ThirdPartyManager.instance().docsProvider.showWidgetDocs(getWidgetId(hoveredWidget));
            return true;
        } else {
            for (IProgWidget widget : visibleSpawnWidgets) {
                if (widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                    ThirdPartyManager.instance().docsProvider.showWidgetDocs(getWidgetId(widget));
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
        Rectangle2d draggingRect = new Rectangle2d(widget1.getX(), widget1.getY(), widget1.getWidth() / 2, widget1.getHeight() / 2);
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
     * Here's where we "snap together" nearby widgets which can connect.  Called when a widget is placed (on mouse release).
     */
    private void handlePuzzleMargins() {
        // Check for connection to the left of the dragged widget.
        ProgWidgetType returnValue = draggingWidget.returnType();
        if (returnValue != null) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget != draggingWidget && Math.abs(widget.getX() + widget.getWidth() / 2 - draggingWidget.getX()) <= FAULT_MARGIN) {
                    List<ProgWidgetType<?>> parameters = widget.getParameters();
                    for (int i = 0; i < parameters.size(); i++) {
                        if (widget.canSetParameter(i) && parameters.get(i) == returnValue
                                && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
                            setConnectingWidgetsToXY(draggingWidget, widget.getX() + widget.getWidth() / 2, widget.getY() + i * 11);
                            return;
                        }
                    }
                }
            }
        }

        // check for connection to the right of the dragged widget.
        List<ProgWidgetType<?>> parameters = draggingWidget.getParameters();
        if (!parameters.isEmpty()) {
            for (IProgWidget widget : te.progWidgets) {
                IProgWidget outerPiece = draggingWidget;
                if (outerPiece.returnType() != null) {//When the piece is a parameter pice (area, item filter, text).
                    while (outerPiece.getConnectedParameters()[0] != null) {
                        outerPiece = outerPiece.getConnectedParameters()[0];
                    }
                }
                if (widget != draggingWidget && Math.abs(outerPiece.getX() + outerPiece.getWidth() / 2 - widget.getX()) <= FAULT_MARGIN) {
                    if (widget.returnType() != null) {
                        for (int i = 0; i < parameters.size(); i++) {
                            if (draggingWidget.canSetParameter(i) && parameters.get(i) == widget.returnType() && Math.abs(draggingWidget.getY() + i * 11 - widget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() - i * 11);
                            }
                        }
                    } else {
                        List<ProgWidgetType<?>> checkingPieceParms = widget.getParameters();
                        for (int i = 0; i < checkingPieceParms.size(); i++) {
                            if (widget.canSetParameter(i + parameters.size()) && checkingPieceParms.get(i) == parameters.get(0) && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() + i * 11);
                            }
                        }
                    }
                }
            }
        }

        // check for connection to the top of the dragged widget.
        if (draggingWidget.hasStepInput()) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget.hasStepOutput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() + widget.getHeight() / 2 - draggingWidget.getY()) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() + widget.getHeight() / 2);
                }
            }
        }

        // check for connection to the bottom of the dragged widget.
        if (draggingWidget.hasStepOutput()) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget.hasStepInput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() - draggingWidget.getY() - draggingWidget.getHeight() / 2) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() - draggingWidget.getHeight() / 2);
                }
            }
        }
    }

    /**
     * Set the position of the given widget and (recursively) all widgets which connect to it on the side or below
     * (but not above).
     *
     * @param widget the widget
     * @param x new X pos
     * @param y new Y pos
     */
    private void setConnectingWidgetsToXY(IProgWidget widget, int x, int y) {
        widget.setX(x);
        widget.setY(y);
        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if (connectingWidgets != null) {
            for (int i = 0; i < connectingWidgets.length; i++) {
                if (connectingWidgets[i] != null) {
                    if (i < connectingWidgets.length / 2) {
                        setConnectingWidgetsToXY(connectingWidgets[i], x + widget.getWidth() / 2, y + i * 11);
                    } else {
                        int totalWidth = 0;
                        IProgWidget branch = connectingWidgets[i];
                        while (branch != null) {
                            totalWidth += branch.getWidth() / 2;
                            branch = branch.getConnectedParameters()[0];
                        }
                        setConnectingWidgetsToXY(connectingWidgets[i], x - totalWidth, y + (i - connectingWidgets.length / 2) * 11);
                    }
                }
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if (outputWidget != null) setConnectingWidgetsToXY(outputWidget, x, y + widget.getHeight() / 2);
    }

    /**
     * Called when shift + middle-clicking: copy this widget and all connecting widgets to the side or below (but not
     * above).
     * @param original original widget being copied
     * @param copy new copy of the widget
     */
    private void copyAndConnectConnectingWidgets(IProgWidget original, IProgWidget copy) {
        IProgWidget[] connectingWidgets = original.getConnectedParameters();
        if (connectingWidgets != null) {
            for (int i = 0; i < connectingWidgets.length; i++) {
                if (connectingWidgets[i] != null) {
                    IProgWidget c = connectingWidgets[i].copy();
                    te.progWidgets.add(c);
                    copy.setParameter(i, c);
                    copyAndConnectConnectingWidgets(connectingWidgets[i], c);
                }
            }
        }
        IProgWidget outputWidget = original.getOutputWidget();
        if (outputWidget != null) {
            IProgWidget c = outputWidget.copy();
            te.progWidgets.add(c);
            copy.setOutputWidget(c);
            copyAndConnectConnectingWidgets(outputWidget, c);
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
    public void tick() {
        super.tick();

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

        ItemStack programmedItem = te.getItemInProgrammingSlot();
        oldShowingWidgetProgress = showingWidgetProgress;
        int maxProgress = maxPage * WIDGET_X_SPACING;
        if (showingAllWidgets) {
            if (showingWidgetProgress < maxProgress) {
                showingWidgetProgress += maxProgress / 5;
                if (showingWidgetProgress >= maxProgress) {
                    showingWidgetProgress = maxProgress;
                    updateVisibleProgWidgets();
                }
            } else {
                setFocused(filterField);
            }
        } else {
            showingWidgetProgress -= maxProgress / 5;
            if (showingWidgetProgress < 0) showingWidgetProgress = 0;
        }

        List<ITextComponent> errors = new ArrayList<>();
        List<ITextComponent> warnings = new ArrayList<>();
        for (IProgWidget w : te.progWidgets) {
            w.addErrors(errors, te.progWidgets);
            w.addWarnings(warnings, te.progWidgets);
        }

        boolean isDeviceInserted = !programmedItem.isEmpty();
        importButton.active = isDeviceInserted;
        exportButton.active = isDeviceInserted && errors.size() == 0;

        updateExportButtonTooltip(programmedItem, errors, warnings);

        if (!programmedItem.isEmpty()) {
            nameField.setEnabled(true);
            if (!nameField.getText().equals(programmedItem.getDisplayName().getUnformattedComponentText())) {
                nameField.setText(programmedItem.getDisplayName().getFormattedText());
            }
        } else {
            nameField.setEnabled(false);
            nameField.setText("");
        }
    }

    private void updateExportButtonTooltip(ItemStack programmedItem, List<ITextComponent> errors, List<ITextComponent> warnings) {
        List<String> exportButtonTooltip = new ArrayList<>();
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export"));
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.programmingWhen", I18n.format("gui.programmer.button.export." + (te.redstoneMode == 0 ? "pressingButton" : "onItemInsert"))));
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.pressRToChange"));
        if (!programmedItem.isEmpty()) {
            int required = te.getRequiredPuzzleCount();
            if (required != 0) exportButtonTooltip.add("");
            int r = minecraft.player.isCreative() ? 0 : required;
            if (required > 0) {
                exportButtonTooltip.add(I18n.format("gui.tooltip.programmable.requiredPieces", r));
            } else if (required < 0) {
                exportButtonTooltip.add(I18n.format("gui.tooltip.programmable.returnedPieces", -r));
            }
            if (required != 0 && minecraft.player.isCreative()) exportButtonTooltip.add("(Creative mode)");
        } else {
            exportButtonTooltip.add(TextFormatting.GOLD + I18n.format("gui.programmer.button.export.noProgrammableItem"));
        }

        if (errors.size() > 0)
            exportButtonTooltip.add(TextFormatting.RED + I18n.format("gui.programmer.errorCount", errors.size()));
        if (warnings.size() > 0)
            exportButtonTooltip.add(TextFormatting.YELLOW + I18n.format("gui.programmer.warningCount", warnings.size()));

        exportButton.setTooltipText(exportButtonTooltip);
    }

    private void updateConvertRelativeState() {
        convertToRelativeButton.active = false;
        List<String> tooltip = new ArrayList<>();
        tooltip.add("gui.programmer.button.convertToRelative.desc");

        boolean startFound = false;
        for (IProgWidget startWidget : te.progWidgets) {
            if (startWidget instanceof ProgWidgetStart) {
                startFound = true;
                IProgWidget widget = startWidget.getOutputWidget();
                if (widget instanceof ProgWidgetCoordinateOperator) {
                    ProgWidgetCoordinateOperator operatorWidget = (ProgWidgetCoordinateOperator) widget;
                    if (!operatorWidget.getVariable().equals("")) {
                        try {
                            if (generateRelativeOperators(operatorWidget, tooltip, true)) {
                                convertToRelativeButton.active = true;
                            } else {
                                tooltip.add("gui.programmer.button.convertToRelative.notEnoughRoom");
                            }
                        } catch (NullPointerException e) {
                            tooltip.add("gui.programmer.button.convertToRelative.cantHaveVariables");
                        }
                    } else {
                        tooltip.add("gui.programmer.button.convertToRelative.noVariableName");
                    }
                } else {
                    tooltip.add("gui.programmer.button.convertToRelative.noBaseCoordinate");
                }
            }
        }
        if (!startFound) tooltip.add("gui.programmer.button.convertToRelative.noStartPiece");

        List<String> localizedTooltip = new ArrayList<>();
        for (String s : tooltip) {
            localizedTooltip.addAll(PneumaticCraftUtils.splitString(I18n.format(s), 40));
        }
        convertToRelativeButton.setTooltipText(localizedTooltip);
    }

    private boolean generateRelativeOperators(ProgWidgetCoordinateOperator baseWidget, List<String> tooltip, boolean simulate) {
        BlockPos baseCoord = ProgWidgetCoordinateOperator.calculateCoordinate(baseWidget, 0, baseWidget.getOperator());
        Map<BlockPos, String> offsetToVariableNames = new HashMap<>();
        for (IProgWidget widget : te.progWidgets) {
            if (widget instanceof ProgWidgetArea) {
                ProgWidgetArea area = (ProgWidgetArea) widget;
                if (area.getCoord1Variable().equals("") && (area.x1 != 0 || area.y1 != 0 || area.z1 != 0)) {
                    BlockPos offset = new BlockPos(area.x1 - baseCoord.getX(), area.y1 - baseCoord.getY(), area.z1 - baseCoord.getZ());
                    String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                    if (!simulate) area.setCoord1Variable(var);
                }
                if (area.getCoord2Variable().equals("") && (area.x2 != 0 || area.y2 != 0 || area.z2 != 0)) {
                    BlockPos offset = new BlockPos(area.x2 - baseCoord.getX(), area.y2 - baseCoord.getY(), area.z2 - baseCoord.getZ());
                    String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                    if (!simulate) area.setCoord2Variable(var);
                }
            } else if (widget instanceof ProgWidgetCoordinate && baseWidget.getConnectedParameters()[0] != widget) {
                ProgWidgetCoordinate coordinate = (ProgWidgetCoordinate) widget;
                if (!coordinate.isUsingVariable()) {
                    BlockPos c = coordinate.getCoordinate();
                    String chunkString = "(" + c.getX() + ", " + c.getY() + ", " + c.getZ() + ")";
                    if (PneumaticCraftUtils.distBetween(c, 0, 0, 0) < 64) {
                        // When the coordinate value is close to 0, there's a low chance it means a position, and rather an offset.
                        if (tooltip != null)
                            tooltip.add(I18n.format("gui.programmer.button.convertToRelative.coordIsNotChangedWarning", chunkString));
                    } else {
                        if (tooltip != null)
                            tooltip.add(I18n.format("gui.programmer.button.convertToRelative.coordIsChangedWarning", chunkString));
                        if (!simulate) {
                            BlockPos offset = new BlockPos(c.getX() - baseCoord.getX(), c.getY() - baseCoord.getY(), c.getZ() - baseCoord.getZ());
                            String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                            coordinate.setVariable(var);
                            coordinate.setUsingVariable(true);
                        }
                    }
                }
            }
        }
        if (offsetToVariableNames.size() > 0) {
            ProgWidgetCoordinateOperator firstOperator = null;
            ProgWidgetCoordinateOperator prevOperator = baseWidget;
            int x = baseWidget.getX();
            for (Map.Entry<BlockPos, String> entry : offsetToVariableNames.entrySet()) {
                ProgWidgetCoordinateOperator operator = new ProgWidgetCoordinateOperator();
                operator.setVariable(entry.getValue());

                int y = prevOperator.getY() + prevOperator.getHeight() / 2;
                operator.setX(x);
                operator.setY(y);
                if (!isValidPlaced(operator)) return false;

                ProgWidgetCoordinate coordinatePiece1 = new ProgWidgetCoordinate();
                coordinatePiece1.setX(x + prevOperator.getWidth() / 2);
                coordinatePiece1.setY(y);
                coordinatePiece1.setVariable(baseWidget.getVariable());
                coordinatePiece1.setUsingVariable(true);
                if (!isValidPlaced(coordinatePiece1)) return false;

                ProgWidgetCoordinate coordinatePiece2 = new ProgWidgetCoordinate();
                coordinatePiece2.setX(x + prevOperator.getWidth() / 2 + coordinatePiece1.getWidth() / 2);
                coordinatePiece2.setY(y);
                coordinatePiece2.setCoordinate(entry.getKey());
                if (!isValidPlaced(coordinatePiece2)) return false;

                if (!simulate) {
                    te.progWidgets.add(operator);
                    te.progWidgets.add(coordinatePiece1);
                    te.progWidgets.add(coordinatePiece2);
                }
                if (firstOperator == null) firstOperator = operator;
                prevOperator = operator;
            }
            if (!simulate) {
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
                TileEntityProgrammer.updatePuzzleConnections(te.progWidgets);
            }
            return true;
        } else {
            return true; //When there's nothing to place there's always room.
        }
    }

    private String getOffsetVariable(Map<BlockPos, String> offsetToVariableNames, String baseVariable, BlockPos offset) {
        if (offset.equals(BlockPos.ZERO))
            return baseVariable;
        return offsetToVariableNames.computeIfAbsent(offset, k -> "var" + (offsetToVariableNames.size() + 1));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double origX = mouseX;
        double origY = mouseY;

        float scale = programmerUnit.getScale();
        mouseX = (mouseX - programmerUnit.getTranslatedX()) / scale;
        mouseY = (mouseY - programmerUnit.getTranslatedY()) / scale;

        if (button == 0) {
            // left-click
            IProgWidget hovered = programmerUnit.getHoveredWidget((int) origX, (int) origY);

            // first check if we're clicking with a gps tool in hand
            ItemStack heldItem = minecraft.player.inventory.getItemStack();
            if (heldItem.getItem() instanceof IPositionProvider) {
                ProgWidgetArea areaToolWidget = heldItem.getItem() instanceof ItemGPSAreaTool ? ItemGPSAreaTool.getArea(heldItem) : null;
                if (hovered != null) {
                    // clicked an existing widget: update any area or coordinate widgets from the held item
                    if (areaToolWidget != null && hovered instanceof ProgWidgetArea) {
                        CompoundNBT tag = new CompoundNBT();
                        areaToolWidget.writeToNBT(tag);
                        hovered.readFromNBT(tag);
                    } else if (heldItem.getItem() == ModItems.GPS_TOOL.get()) {
                        if (hovered instanceof ProgWidgetCoordinate) {
                            ((ProgWidgetCoordinate) hovered).loadFromGPSTool(heldItem);
                        } else if (hovered instanceof ProgWidgetArea) {
                            BlockPos pos = ItemGPSTool.getGPSLocation(heldItem);
                            String var = ItemGPSTool.getVariable(heldItem);
                            ProgWidgetArea areaHovered = (ProgWidgetArea) hovered;
                            if (pos != null) areaHovered.setP1(pos);
                            areaHovered.setP2(BlockPos.ZERO);
                            areaHovered.setCoord1Variable(var);
                            areaHovered.setCoord2Variable("");
                        }
                    }
                } else {
                    // clicked on an empty area: create a new area or coordinate widget
                    ProgWidget toCreate = null;
                    if (areaToolWidget != null) {
                        toCreate = areaToolWidget;
                    } else if (heldItem.getItem() == ModItems.GPS_TOOL.get()) {
                        if (Screen.hasShiftDown()) {
                            BlockPos pos = ItemGPSTool.getGPSLocation(heldItem);
                            ProgWidgetArea areaWidget = ProgWidgetArea.fromPositions(pos, BlockPos.ZERO);
                            String var = ItemGPSTool.getVariable(heldItem);
                            if (!var.isEmpty()) areaWidget.setCoord1Variable(var);
                            toCreate = areaWidget;
                        } else {
                            ProgWidgetCoordinate coordWidget = new ProgWidgetCoordinate();
                            coordWidget.loadFromGPSTool(heldItem);
                            toCreate = coordWidget;
                        }
                    }
                    if (toCreate != null) {
                        toCreate.setX((int) (mouseX - guiLeft - toCreate.getWidth() / 3d));
                        toCreate.setY((int) (mouseY - guiTop - toCreate.getHeight() / 4d));
                        if (!programmerUnit.isOutsideProgrammingArea(toCreate)) {
                            te.progWidgets.add(toCreate);
                        }
                    }
                }
                return true;
            } else {
                // just a regular click, nothing of interest held
                if (hovered != null) {
                    // clicking on a widget in the main area
                    draggingWidget = hovered;
                    dragMouseStartX = mouseX - guiLeft;
                    dragMouseStartY = mouseY - guiTop;
                    dragWidgetStartX = hovered.getX();
                    dragWidgetStartY = hovered.getY();
                    return true;
                } else {
                    // clicking on a widget in the tray?
                    for (IProgWidget widget : visibleSpawnWidgets) {
                        if (origX >= widget.getX() + guiLeft
                                && origY >= widget.getY() + guiTop
                                && origX <= widget.getX() + guiLeft + widget.getWidth() / 2
                                && origY <= widget.getY() + guiTop + widget.getHeight() / 2)
                        {
                            draggingWidget = widget.copy();
                            te.progWidgets.add(draggingWidget);
                            dragMouseStartX = mouseX - (int) (guiLeft / scale);
                            dragMouseStartY = mouseY - (int) (guiTop / scale);
                            dragWidgetStartX = (int) ((widget.getX() - programmerUnit.getTranslatedX()) / scale);
                            dragWidgetStartY = (int) ((widget.getY() - programmerUnit.getTranslatedY()) / scale);
                            return true;
                        }
                    }
                }
            }
        } else if (button == 2) {
            // middle-click: copy widget, or show docs if clicked on widget tray
            if (showingWidgetProgress == 0) {
                IProgWidget widget = programmerUnit.getHoveredWidget((int) origX, (int) origY);
                if (widget != null) {
                    draggingWidget = widget.copy();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = mouseX - guiLeft;
                    dragMouseStartY = mouseY - guiTop;
                    dragWidgetStartX = widget.getX();// - (mouseX - guiLeft);
                    dragWidgetStartY = widget.getY();// - (mouseY - guiTop);
                    if (Screen.hasShiftDown()) copyAndConnectConnectingWidgets(widget, draggingWidget);
                    return true;
                }
            } else {
                return showWidgetDocs();
            }
        } else if (button == 1 && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget((int)origX, (int)origY);
            if (widget != null) {
                // right click a prog widget: show its options screen, if any
                GuiProgWidgetOptionBase gui = ProgWidgetGuiManager.getGui(widget, this);
                if (gui != null) {
                    minecraft.displayGuiScreen(gui);
                }
            }
            return true;
        }
        return super.mouseClicked(origX, origY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingWidget != null) {
            if (programmerUnit.isOutsideProgrammingArea(draggingWidget)) {
                deleteConnectingWidgets(draggingWidget);
            } else {
                handlePuzzleMargins();
                if (!isValidPlaced(draggingWidget)) {
                    setConnectingWidgetsToXY(draggingWidget, (int)dragWidgetStartX, (int)dragWidgetStartY);
                    if (programmerUnit.isOutsideProgrammingArea(draggingWidget) || !isValidPlaced(draggingWidget))
                        deleteConnectingWidgets(draggingWidget);
                }
            }
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            TileEntityProgrammer.updatePuzzleConnections(te.progWidgets);
            draggingWidget = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        return programmerUnit.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (draggingWidget != null) {
            mouseX = (mouseX - programmerUnit.getTranslatedX()) / programmerUnit.getScale();
            mouseY = (mouseY - programmerUnit.getTranslatedY()) / programmerUnit.getScale();
            setConnectingWidgetsToXY(draggingWidget,
                    (int)(mouseX - dragMouseStartX + dragWidgetStartX - guiLeft),
                    (int)(mouseY - dragMouseStartY + dragWidgetStartY - guiTop));
            return true;
        } else {
            return programmerUnit.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        }
    }

    @Override
    public void onClose() {
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

    private class FilterTextField extends WidgetTextField {
        FilterTextField(FontRenderer font, int x, int y, int width, int height) {
            super(font, x, y, width, height);
        }

        @Override
        public void renderButton(int x, int y, float partialTicks) {
            // this is needed to force the textfield to draw on top of any
            // widgets in the programming area
            GlStateManager.translated(0, 0, 300);
            super.renderButton(x, y, partialTicks);
            GlStateManager.translated(0, 0, -300);
        }
    }

    private class DifficultyButton extends WidgetRadioButton {
        final WidgetDifficulty difficulty;

        DifficultyButton(int x, int y, int color, WidgetDifficulty difficulty, Consumer<WidgetRadioButton> pressable) {
            super(x, y, color, difficulty.getTranslationKey(), pressable);
            this.difficulty = difficulty;
        }
    }

    private class RemovingWidget {
        final IProgWidget widget;
        double ty = 0;
        double tx = 0;
        double velX = (Minecraft.getInstance().world.rand.nextDouble() - 0.5) * 3;
        double velY = -4;

        private RemovingWidget(IProgWidget widget) {
            this.widget = widget;
        }
    }
}
