package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetOptionBase;
import me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetGuiManager;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.client.util.ProgWidgetRenderer;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
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

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgrammer extends GuiPneumaticContainerBase<ContainerProgrammer,TileEntityProgrammer> {
    private GuiPastebin pastebinGui;

    private WidgetButtonExtended importButton;
    private WidgetButtonExtended exportButton;
    private WidgetButtonExtended allWidgetsButton;
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

    private ProgrammerWidgetAreaRenderer programmerUnit;
    private IProgWidget draggingWidget;
    private int lastMouseX, lastMouseY;
    private double dragMouseStartX, dragMouseStartY;
    private double dragWidgetStartX, dragWidgetStartY;
    private boolean draggingBG;
    private static final int FAULT_MARGIN = 4;
    private int widgetPage;
    private int maxPage;

    private boolean showingAllWidgets;
    private int showingWidgetProgress;
    private int oldShowingWidgetProgress;

    private static final Rectangle2d PROGRAMMER_STD_RES = new Rectangle2d(5, 17, 294, 154);
    private static final Rectangle2d PROGRAMMER_HI_RES = new Rectangle2d(5, 17, 644, 410);

    private static final int WIDGET_X_SPACING = 22; // x size of widgets in the widget tray

    private final boolean hiRes;

    public GuiProgrammer(ContainerProgrammer container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        hiRes = container.isHiRes();
        imageWidth = hiRes ? 700 : 350;
        imageHeight = hiRes ? 512 : 256;
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
        programmerUnit = new ProgrammerWidgetAreaRenderer(this, te.progWidgets, leftPos, topPos,
                bounds, te.translatedX, te.translatedY, te.zoomState);
        addButton(programmerUnit.getScrollBar());

        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;

        // right and bottom edges of the programming area
        int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth(); // 299 or 649
        int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight() + 3; // 171 or 427

        importButton = new WidgetButtonExtended(xStart + xRight + 2, yStart + 3, 20, 15, Symbols.ARROW_LEFT)
                .withTag("import")
                .setTooltipKey("pneumaticcraft.gui.programmer.button.import");
        addButton(importButton);

        exportButton = new WidgetButtonExtended(xStart + xRight + 2, yStart + 20, 20, 15, Symbols.ARROW_RIGHT)
                .withTag("export");
        addButton(exportButton);

        addButton(new WidgetButtonExtended(xStart + xRight - 3, yStart + yBottom, 13, 10, Symbols.TRIANGLE_LEFT, b -> adjustPage(-1)));
        addButton(new WidgetButtonExtended(xStart + xRight + 34, yStart + yBottom, 13, 10, Symbols.TRIANGLE_RIGHT, b -> adjustPage(1)));

        allWidgetsButton = new WidgetButtonExtended(xStart + xRight + 22, yStart + yBottom - 16, 10, 10, Symbols.TRIANGLE_UP_LEFT, b -> toggleShowWidgets());
        allWidgetsButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.openPanel.tooltip"));
        addButton(allWidgetsButton);

        WidgetRadioButton.Builder<DifficultyButton> rbb = WidgetRadioButton.Builder.create();
        for (WidgetDifficulty wd : WidgetDifficulty.values()) {
            DifficultyButton dButton = new DifficultyButton(xStart + xRight - 36, yStart + yBottom + 29 + wd.ordinal() * 12,
                    0xFF404040, wd, b -> updateDifficulty(wd));
            dButton.setTooltip(xlate(wd.getTooltipTranslationKey()));
            rbb.addRadioButton(dButton, wd == ConfigHelper.client().general.programmerDifficulty.get());
        }
        rbb.build(this::addButton);

        addButton(new WidgetButtonExtended(xStart + 5, yStart + yBottom + 4, 87, 20,
                xlate("pneumaticcraft.gui.programmer.button.showStart"), b -> gotoStart())
                .setTooltipText(xlate("pneumaticcraft.gui.programmer.button.showStart.tooltip")));
        addButton(new WidgetButtonExtended(xStart + 5, yStart + yBottom + 26, 87, 20,
                xlate("pneumaticcraft.gui.programmer.button.showLatest"), b -> gotoLatest())
                .setTooltipText(xlate("pneumaticcraft.gui.programmer.button.showLatest.tooltip")));
        addButton(showInfo = new WidgetCheckBox(xStart + 5, yStart + yBottom + 49, 0xFF404040,
                xlate("pneumaticcraft.gui.programmer.checkbox.showInfo")).setChecked(te.showInfo));
        addButton(showFlow = new WidgetCheckBox(xStart + 5, yStart + yBottom + 61, 0xFF404040,
                xlate("pneumaticcraft.gui.programmer.checkbox.showFlow")).setChecked(te.showFlow));

        WidgetButtonExtended pastebinButton = new WidgetButtonExtended(leftPos - 24, topPos + 44, 20, 20, "",
                b -> pastebin());
        pastebinButton.setTooltipText(xlate("pneumaticcraft.gui.remote.button.pastebinButton"));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        addButton(pastebinButton);

        undoButton = new WidgetButtonExtended(leftPos - 24, topPos + 2, 20, 20, "").withTag("undo");
        redoButton = new WidgetButtonExtended(leftPos - 24, topPos + 23, 20, 20, "").withTag("redo");
        WidgetButtonExtended clearAllButton = new WidgetButtonExtended(leftPos - 24, topPos + 65, 20, 20, StringTextComponent.EMPTY, b -> clear());
        convertToRelativeButton = new WidgetButtonExtended(leftPos - 24, topPos + 86, 20, 20, "R", b -> convertToRelative());

        undoButton.setRenderedIcon(Textures.GUI_UNDO_ICON_LOCATION);
        redoButton.setRenderedIcon(Textures.GUI_REDO_ICON_LOCATION);
        clearAllButton.setRenderedIcon(Textures.GUI_DELETE_ICON_LOCATION);

        undoButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.undoButton.tooltip"));
        redoButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.redoButton.tooltip"));
        clearAllButton.setTooltipText(xlate("pneumaticcraft.gui.programmer.button.clearAllButton.tooltip"));

        addButton(undoButton);
        addButton(redoButton);
        addButton(clearAllButton);
        addButton(convertToRelativeButton);

        addLabel(title, leftPos + 7, topPos + 5, 0xFF404040);

        nameField = new WidgetTextField(font, leftPos + xRight - 99, topPos + 5, 98, font.lineHeight);
        nameField.setResponder(s -> updateDroneName());
        addButton(nameField);

        filterField = new FilterTextField(font, leftPos + 78, topPos + 26, 100, font.lineHeight);
        filterField.setResponder(s -> filterSpawnWidgets());

        addButton(filterField);

        ITextComponent name = xlate("pneumaticcraft.gui.programmer.name");
        addLabel(name, leftPos + xRight - 102 - font.width(name), topPos + 5, 0xFF404040);

        updateVisibleProgWidgets();

        for (IProgWidget widget : te.progWidgets) {
            if (!programmerUnit.isOutsideProgrammingArea(widget)) {
                return;
            }
        }
        programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
    }

    @Override
    public boolean isPauseScreen() {
        return ConfigHelper.client().general.programmerGuiPauses.get();
    }

    public static void onCloseFromContainer() {
        if (Minecraft.getInstance().screen instanceof GuiProgrammer) {
            GuiProgrammer p = (GuiProgrammer) Minecraft.getInstance().screen;
            p.removed();
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
        updateVisibleProgWidgets(ConfigHelper.client().general.programmerDifficulty.get());
    }

    private void updateVisibleProgWidgets(WidgetDifficulty difficulty) {
        int y = 0, page = 0;
        int x = getWidgetTrayRight() - maxPage * WIDGET_X_SPACING;
        boolean showAllWidgets = showingWidgetProgress == WIDGET_X_SPACING * maxPage && showingAllWidgets;
        filterField.setVisible(showAllWidgets);

        maxPage = 0;
        visibleSpawnWidgets.clear();
        int idx = 0;
        int nWidgets = ModProgWidgets.PROG_WIDGETS.get().getValues().size();
        for (ProgWidgetType<?> type : ModProgWidgets.PROG_WIDGETS.get().getValues()) {
            IProgWidget widget = IProgWidget.create(type);
            if (widget.isAvailable() && widget.isDifficultyOK(difficulty)) {
                widget.setY(y + 40);
                widget.setX(showAllWidgets ? x : getWidgetTrayRight());
                int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
                y += widgetHeight;

                if (showAllWidgets || page == widgetPage) {
                    visibleSpawnWidgets.add(widget);
                }
                if (y > imageHeight - (hiRes ? 260 : 160)) {
                    y = 0;
                    x += WIDGET_X_SPACING;
                    page++;
                    if (idx < nWidgets - 1) maxPage++;
                }
            }
            idx++;
        }
        maxPage++;

        filterField.x = Math.min(leftPos + getWidgetTrayRight() - 25 - filterField.getWidth(), leftPos + getWidgetTrayRight() - (maxPage * WIDGET_X_SPACING) - 2);
        filterSpawnWidgets();

        if (widgetPage >= maxPage) {
            widgetPage = maxPage - 1;
            updateVisibleProgWidgets();
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
        if (stack != ItemStack.EMPTY && !stack.getHoverName().getContents().equals(nameField.getValue())) {
            stack.setHoverName(new StringTextComponent(nameField.getValue()));
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

    private static final ITextComponent TDR = new StringTextComponent(Symbols.TRIANGLE_DOWN_RIGHT);
    private static final ITextComponent TUL = new StringTextComponent(Symbols.TRIANGLE_UP_LEFT);

    private void toggleShowWidgets() {
        showingAllWidgets = !showingAllWidgets;
        allWidgetsButton.setMessage(showingAllWidgets ? TDR : TUL);
        updateVisibleProgWidgets();
        filterField.setFocus(showingAllWidgets);
    }

    private void updateDifficulty(WidgetDifficulty difficulty) {
        ConfigHelper.setProgrammerDifficulty(difficulty);
        if (showingAllWidgets) toggleShowWidgets();
        updateVisibleProgWidgets(difficulty);
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
        minecraft.setScreen(pastebinGui = new GuiPastebin(this, mainTag));
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
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        super.renderLabels(matrixStack, x, y);

        int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth(); // 299 or 649
        int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight(); // 171 or 427

        String str = widgetPage + 1 + "/" + maxPage;
        font.draw(matrixStack, str, xRight + 22 - font.width(str) / 2f, yBottom + 4, 0x404040);
        font.draw(matrixStack, xlate("pneumaticcraft.gui.programmer.difficulty").getVisualOrderText(), xRight - 36, yBottom + 20, 0x404040);

        if (showingWidgetProgress == 0) {
            programmerUnit.renderForeground(matrixStack, x, y, draggingWidget, font);
        }

        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            if (widget != draggingWidget && x - leftPos >= widget.getX()
                    && y - topPos >= widget.getY() && x - leftPos <= widget.getX() + widget.getWidth() / 2
                    && y - topPos <= widget.getY() + widget.getHeight() / 2
                    && (!showingAllWidgets || filteredSpawnWidgets == null || filteredSpawnWidgets.get(i))) {
                List<ITextComponent> tooltip = new ArrayList<>();
                widget.getTooltip(tooltip);
                ThirdPartyManager.instance().getDocsProvider().addTooltip(tooltip, showingAllWidgets);
                if (Minecraft.getInstance().options.advancedItemTooltips) {
                    tooltip.add(new StringTextComponent(widget.getType().getRegistryName().toString()).withStyle(TextFormatting.DARK_GRAY));
                }
                if (!tooltip.isEmpty()) {
                    drawHoveringString(matrixStack, tooltip, x - leftPos, y - topPos, font);
                }
            }
        }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderBackground(matrixStack);
        bindGuiTexture();
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        blit(matrixStack, xStart, yStart, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        programmerUnit.render(matrixStack, mouseX, mouseY, showFlow.checked, showInfo.checked && showingWidgetProgress == 0);

        // draw expanding widget tray
        if (showingWidgetProgress > 0) {
            int xRight = getProgrammerBounds().getX() + getProgrammerBounds().getWidth(); // 299 or 649
            int yBottom = getProgrammerBounds().getY() + getProgrammerBounds().getHeight(); // 171 or 427

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            int width = (int)MathHelper.lerp(partialTicks, (float)oldShowingWidgetProgress, (float)showingWidgetProgress);
            for (int i = 0; i < width; i++) {
                blit(matrixStack, xStart + xRight + 21 - i, yStart + 36, xRight + 24, 36, 1, yBottom - 35, imageWidth, imageHeight);
            }
            blit(matrixStack, xStart + xRight + 20 - width, yStart + 36, xRight + 20, 36, 2, yBottom - 35, imageWidth, imageHeight);

            if (showingAllWidgets && draggingWidget != null) toggleShowWidgets();
        }
        // draw widgets in the widget tray
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            matrixStack.pushPose();
            matrixStack.translate(widget.getX() + leftPos, widget.getY() + topPos, 0);
            matrixStack.scale(0.5f, 0.5f, 1f);
            if (showingAllWidgets && filteredSpawnWidgets != null && !filteredSpawnWidgets.get(i)) {
                ProgWidgetRenderer.renderProgWidget2d(matrixStack, widget, 48);
            } else {
                ProgWidgetRenderer.renderProgWidget2d(matrixStack, widget);
            }
            matrixStack.popPose();
        }

        // draw the widget currently being dragged, if any
        float scale = programmerUnit.getScale();
        matrixStack.pushPose();
        matrixStack.translate(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        matrixStack.scale(scale, scale, 1f);
        if (draggingWidget != null) {
            matrixStack.pushPose();
            matrixStack.translate(draggingWidget.getX() + leftPos, draggingWidget.getY() + topPos, 0);
            matrixStack.scale(0.5f, 0.5f, 1f);
            ProgWidgetRenderer.renderProgWidget2d(matrixStack, draggingWidget);
            matrixStack.popPose();
        }
        matrixStack.popPose();

        RenderSystem.disableBlend();

        if (!removingWidgets.isEmpty()) drawRemovingWidgets(matrixStack);
    }

    /**
     * This doesn't achieve much other than look really cool.
     */
    private void drawRemovingWidgets(MatrixStack matrixStack) {
        Iterator<RemovingWidget> iter = removingWidgets.iterator();
        int h = minecraft.getWindow().getGuiScaledHeight();
        float scale = programmerUnit.getScale();

        matrixStack.pushPose();
        matrixStack.translate(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        matrixStack.scale(scale, scale, 1f);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        while (iter.hasNext()) {
            RemovingWidget rw = iter.next();
            IProgWidget w = rw.widget;
            if (w.getY() + rw.ty > h / scale) {
                iter.remove();
            } else {
                matrixStack.pushPose();
                matrixStack.translate(w.getX() + rw.tx + leftPos, w.getY() + rw.ty + topPos, 0);
                matrixStack.scale(0.5f, 0.5f, 1f);
                ProgWidgetRenderer.renderProgWidget2d(matrixStack, w);
                matrixStack.popPose();
                rw.tick();

            }
        }
        RenderSystem.disableBlend();
        matrixStack.popPose();
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
                    NetworkHandler.sendToServer(new PacketGuiButton("program_when"));
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
        ProgWidgetType<?> returnValue = draggingWidget.returnType();
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

        boolean isDeviceInserted = !programmedItem.isEmpty();
        importButton.active = isDeviceInserted;
        exportButton.active = isDeviceInserted && programmerUnit.getTotalErrors() == 0;

        updateExportButtonTooltip(programmedItem);

        if (!programmedItem.isEmpty()) {
            nameField.setEditable(true);
            if (!nameField.getValue().equals(programmedItem.getHoverName().getContents())) {
                nameField.setValue(programmedItem.getHoverName().getString());
            }
        } else {
            nameField.setEditable(false);
            nameField.setValue("");
        }
    }

    private void updateExportButtonTooltip(ItemStack programmedItem) {
        List<ITextComponent> exportButtonTooltip = new ArrayList<>();
        exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export"));
        exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export.programmingWhen",
                xlate("pneumaticcraft.gui.programmer.button.export." + (te.programOnInsert ? "onItemInsert" : "pressingButton")))
                .withStyle(TextFormatting.AQUA));
        exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export.pressRToChange")
                .withStyle(TextFormatting.ITALIC, TextFormatting.GRAY));

        if (!programmedItem.isEmpty()) {
            int required = te.getRequiredPuzzleCount();
            if (required != 0) exportButtonTooltip.add(StringTextComponent.EMPTY);
            int effectiveRequired = minecraft.player.isCreative() ? 0 : required;
            int available = te.availablePuzzlePieces + countPlayerPuzzlePieces();
            exportButton.active = effectiveRequired <= available;
            if (required > 0) {
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.requiredPieces", effectiveRequired)
                        .withStyle(TextFormatting.YELLOW));
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.availablePieces", available)
                        .withStyle(TextFormatting.YELLOW));
            } else if (required < 0) {
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.returnedPieces", -effectiveRequired)
                        .withStyle(TextFormatting.GREEN));
            }
            if (required != 0 && minecraft.player.isCreative()) {
                exportButtonTooltip.add(new StringTextComponent("(Creative mode)").withStyle(TextFormatting.LIGHT_PURPLE));
            }
            if (effectiveRequired > available) {
                exportButtonTooltip.add(xlate("pneumaticcraft.gui.tooltip.programmable.notEnoughPieces")
                        .withStyle(TextFormatting.RED));
            }
        } else {
            exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.button.export.noProgrammableItem")
                    .withStyle(TextFormatting.GOLD));
        }

        if (programmerUnit.getTotalErrors() > 0) {
            exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.errorCount", programmerUnit.getTotalErrors()).withStyle(TextFormatting.RED));
        }
        if (programmerUnit.getTotalWarnings() > 0)
            exportButtonTooltip.add(xlate("pneumaticcraft.gui.programmer.warningCount", programmerUnit.getTotalWarnings()).withStyle(TextFormatting.YELLOW));

        exportButton.setTooltipText(exportButtonTooltip);
    }

    private int countPlayerPuzzlePieces() {
        int count = 0;
        for (ItemStack stack : ClientUtils.getClientPlayer().inventory.items) {
            if (stack.getItem() == ModItems.PROGRAMMING_PUZZLE.get()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void updateConvertRelativeState() {
        convertToRelativeButton.active = false;
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.desc"));

        boolean startFound = false;
        for (IProgWidget startWidget : te.progWidgets) {
            if (startWidget instanceof ProgWidgetStart) {
                startFound = true;
                IProgWidget widget = startWidget.getOutputWidget();
                if (widget instanceof ProgWidgetCoordinateOperator) {
                    ProgWidgetCoordinateOperator operatorWidget = (ProgWidgetCoordinateOperator) widget;
                    if (!operatorWidget.getVariable().isEmpty()) {
                        try {
                            if (generateRelativeOperators(operatorWidget, tooltip, true)) {
                                convertToRelativeButton.active = true;
                            } else {
                                tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.notEnoughRoom"));
                            }
                        } catch (NullPointerException e) {
                            tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.cantHaveVariables"));
                        }
                    } else {
                        tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.noVariableName"));
                    }
                } else {
                    tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.noBaseCoordinate"));
                }
            }
        }
        if (!startFound) tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.noStartPiece"));
        convertToRelativeButton.setTooltipText(tooltip);
    }

    private boolean generateRelativeOperators(ProgWidgetCoordinateOperator baseWidget, List<ITextComponent> tooltip, boolean simulate) {
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
                    BlockPos coord = coordinate.getCoordinate();
                    String coordStr = PneumaticCraftUtils.posToString(coord);
                    if (PneumaticCraftUtils.distBetweenSq(coord, 0, 0, 0) < 4096) {
                        // When the coordinate value is close to 0, there's a low chance it means a position, and rather an offset.
                        if (tooltip != null)
                            tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.coordIsNotChangedWarning", coordStr));
                    } else {
                        if (tooltip != null)
                            tooltip.add(xlate("pneumaticcraft.gui.programmer.button.convertToRelative.coordIsChangedWarning", coordStr));
                        if (!simulate) {
                            BlockPos offset = coord.subtract(baseCoord);
                            String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
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
        }
        return true;
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
            ItemStack heldItem = minecraft.player.inventory.getCarried();
            if (heldItem.getItem() instanceof IPositionProvider) {
                return mouseClickedWithPosProvider(mouseX, mouseY, hovered, heldItem);
            } if (!heldItem.isEmpty()) {
                return mouseClickedWithItem(mouseX, mouseY, hovered, heldItem);
            } else {
                if (mouseClickedEmpty(mouseX, mouseY, origX, origY, scale, hovered))
                    return true;
            }
        } else if (button == 2) {
            // middle-click: copy widget, or show docs if clicked on widget tray
            if (showingWidgetProgress == 0) {
                IProgWidget widget = programmerUnit.getHoveredWidget((int) origX, (int) origY);
                if (widget != null) {
                    draggingWidget = widget.copy();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = mouseX - leftPos;
                    dragMouseStartY = mouseY - topPos;
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
                GuiProgWidgetOptionBase<?> gui = ProgWidgetGuiManager.getGui(widget, this);
                if (gui != null) {
                    minecraft.setScreen(gui);
                }
                return true;
            }
        }
        return super.mouseClicked(origX, origY, button);
    }

    private boolean mouseClickedWithPosProvider(double mouseX, double mouseY, IProgWidget hovered, ItemStack heldItem) {
        ProgWidgetArea areaToolWidget = heldItem.getItem() instanceof ItemGPSAreaTool ? ItemGPSAreaTool.getArea(heldItem) : null;
        if (hovered != null) {
            // clicked an existing widget: update any area or coordinate widgets from the held item
            if (areaToolWidget != null && hovered instanceof ProgWidgetArea) {
                CompoundNBT tag = new CompoundNBT();
                areaToolWidget.writeToNBT(tag);
                tag.putInt("x", hovered.getX());
                tag.putInt("y", hovered.getY());
                hovered.readFromNBT(tag);
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
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
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            }
        } else {
            // clicked on an empty area: create a new area or coordinate widget(s)
            List<IProgWidget> toCreate = new ArrayList<>();
            if (areaToolWidget != null) {
                if (Screen.hasShiftDown()) {
                    ProgWidgetCoordinate pc = new ProgWidgetCoordinate();
                    pc.setCoordinate(new BlockPos(areaToolWidget.x1, areaToolWidget.y1, areaToolWidget.z1));
                    toCreate.add(pc);
                    if (areaToolWidget.x2 != 0 && areaToolWidget.y2 != 0 && areaToolWidget.z2 != 0) {
                        ProgWidgetCoordinate pc2 = new ProgWidgetCoordinate();
                        pc2.setCoordinate(new BlockPos(areaToolWidget.x2, areaToolWidget.y2, areaToolWidget.z2));
                        toCreate.add(pc2);
                    }
                } else {
                    toCreate.add(areaToolWidget);
                }
            } else if (heldItem.getItem() == ModItems.GPS_TOOL.get()) {
                if (Screen.hasShiftDown()) {
                    BlockPos pos = ItemGPSTool.getGPSLocation(heldItem);
                    ProgWidgetArea areaWidget = ProgWidgetArea.fromPositions(pos, BlockPos.ZERO);
                    String var = ItemGPSTool.getVariable(heldItem);
                    if (!var.isEmpty()) areaWidget.setCoord1Variable(var);
                    toCreate.add(areaWidget);
                } else {
                    ProgWidgetCoordinate coordWidget = new ProgWidgetCoordinate();
                    coordWidget.loadFromGPSTool(heldItem);
                    toCreate.add(coordWidget);
                }
            }
            for (int i = 0; i < toCreate.size(); i++) {
                IProgWidget p = toCreate.get(i);
                p.setX((int) (mouseX - leftPos - p.getWidth() / 3d));
                p.setY((int) (mouseY - topPos - p.getHeight() / 4d) + i * p.getHeight());
                if (!programmerUnit.isOutsideProgrammingArea(p)) {
                    te.progWidgets.add(p);
                }
            }
            if (!toCreate.isEmpty()) {
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            }
        }
        return true;
    }

    private boolean mouseClickedWithItem(double mouseX, double mouseY, IProgWidget hovered, ItemStack heldItem) {
        // mouse clicked with an item on the cursor, maybe create or update an item filter widget
        if (hovered == null) {
            ProgWidgetItemFilter p = new ProgWidgetItemFilter();
            p.setFilter(heldItem.copy());
            p.setX((int) (mouseX - leftPos - p.getWidth() / 3d));
            p.setY((int) (mouseY - topPos - p.getHeight() / 4d));
            if (!programmerUnit.isOutsideProgrammingArea(p)) {
                te.progWidgets.add(p);
            }
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            return true;
        } else if (hovered instanceof ProgWidgetItemFilter) {
            ProgWidgetItemFilter p = (ProgWidgetItemFilter) hovered;
            p.setFilter(heldItem.copy());
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
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
                    draggingWidget = widget.copy();
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

    private static class FilterTextField extends WidgetTextField {
        FilterTextField(FontRenderer font, int x, int y, int width, int height) {
            super(font, x, y, width, height);
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int x, int y, float partialTicks) {
            // this is needed to force the textfield to draw on top of any
            // widgets in the programming area
            matrixStack.pushPose();
            matrixStack.translate(0, 0, 300);
            super.renderButton(matrixStack, x, y, partialTicks);
            matrixStack.popPose();
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
        final double velX = (Minecraft.getInstance().level.random.nextDouble() - 0.5) * 3;
        double velY = -4;

        private RemovingWidget(IProgWidget widget) {
            this.widget = widget;
        }

        public void tick() {
            ty += velY;
            tx += velX;
            velY += 0.35;
        }
    }
}
