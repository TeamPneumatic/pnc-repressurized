package me.desht.pneumaticcraft.client.gui;

import com.google.common.base.CaseFormat;
import igwmod.api.WikiRegistry;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammer;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiProgrammer extends GuiPneumaticContainerBase<TileEntityProgrammer> {
    private final EntityPlayer player;
    private GuiPastebin pastebinGui;

    private GuiButtonSpecial importButton;
    private GuiButtonSpecial exportButton;
    private GuiButtonSpecial allWidgetsButton;
    private List<GuiRadioButton> difficultyButtons;
    private GuiCheckBox showInfo, showFlow;
    private WidgetTextField nameField;
    private WidgetTextField filterField;
    private GuiButtonSpecial undoButton, redoButton;
    private GuiButtonSpecial convertToRelativeButton;

    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<>();
    private BitSet filteredSpawnWidgets;

    private GuiUnitProgrammer programmerUnit;
    private boolean wasClicking;
    private boolean wasFocused;
    private IProgWidget draggingWidget;
    private int lastMouseX, lastMouseY;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private static final int FAULT_MARGIN = 4;
    private int widgetPage;
    private int maxPage;

    private boolean showingAllWidgets;
    private int showingWidgetProgress;
    private int oldShowingWidgetProgress;
    private static final int PROGRAMMING_START_Y = 17;
    private static final int PROGRAMMING_START_X = 5;
    private static final int PROGRAMMING_WIDTH = 294;
    private static final int PROGRAMMING_HEIGHT = 154;

    private static final int WIDGET_TRAY_RIGHT = 322; // distance from gui left to right hand side of expanded widget tray
    private static final int WIDGET_X_SPACING = 22; // x size of widgets in the widget tray

    public GuiProgrammer(InventoryPlayer player, TileEntityProgrammer te) {

        super(new ContainerProgrammer(player, te), te, Textures.GUI_PROGRAMMER);
        ySize = 256;
        xSize = 350;

        this.player = FMLClientHandler.instance().getClient().player;
    }

    private void updateVisibleProgWidgets() {
        int y = 0, page = 0;
        int x = WIDGET_TRAY_RIGHT - maxPage * WIDGET_X_SPACING;
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
        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            if (difficulty >= widget.getDifficulty().ordinal()) {
                widget.setY(y + 40);
                widget.setX(showAllWidgets ? x : WIDGET_TRAY_RIGHT);
                int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
                y += widgetHeight;

                if (showAllWidgets || page == widgetPage) {
                    visibleSpawnWidgets.add(widget);
                }
                if (y > ySize - 160) {
                    y = 0;
                    x += WIDGET_X_SPACING;
                    page++;
                    maxPage++;
                }

            }
        }

        filterField.x = guiLeft + WIDGET_TRAY_RIGHT - (maxPage * WIDGET_X_SPACING) - 2;
        filterSpawnWidgets();

        if (widgetPage > maxPage) {
            widgetPage = maxPage;
            updateVisibleProgWidgets();
        }
    }

    private void filterSpawnWidgets() {
        String filterText = filterField.getText().trim();
        if (!visibleSpawnWidgets.isEmpty() && !filterText.isEmpty()) {
            filteredSpawnWidgets = new BitSet(visibleSpawnWidgets.size());
            for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
                IProgWidget widget = visibleSpawnWidgets.get(i);
                String widgetName = I18n.format("programmingPuzzle." + widget.getWidgetString() + ".name");
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

    @Override
    public void initGui() {
        if (pastebinGui != null && pastebinGui.outputTag != null) {
            te.readProgWidgetsFromNBT(pastebinGui.outputTag);
            pastebinGui = null;
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
        }

        super.initGui();

        if (programmerUnit != null) {
            te.translatedX = programmerUnit.getTranslatedX();
            te.translatedY = programmerUnit.getTranslatedY();
            te.zoomState = programmerUnit.getLastZoom();
        }

        programmerUnit = new GuiUnitProgrammer(te.progWidgets, fontRenderer, guiLeft, guiTop, xSize, width, height, PROGRAMMING_START_X, PROGRAMMING_START_Y, PROGRAMMING_WIDTH, PROGRAMMING_HEIGHT, te.translatedX, te.translatedY, te.zoomState);
        addWidget(programmerUnit.getScrollBar());

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        //    addProgWidgetTabs(xStart, yStart);

        importButton = new GuiButtonSpecial(1, xStart + 301, yStart + 3, 20, 15, "<--");
        importButton.setTooltipText("Import program");
        buttonList.add(importButton);

        exportButton = new GuiButtonSpecial(2, xStart + 301, yStart + 20, 20, 15, "-->");
        buttonList.add(exportButton);

        buttonList.add(new GuiButton(3, xStart + 294, yStart + 174, 10, 10, "-"));
        buttonList.add(new GuiButton(4, xStart + 335, yStart + 174, 10, 10, "+"));

        allWidgetsButton = new GuiButtonSpecial(8, xStart + 321, yStart + 159, 10, 10, "<");
        allWidgetsButton.setTooltipText(I18n.format("gui.programmer.button.openPanel.tooltip"));
        addWidget(allWidgetsButton);

        difficultyButtons = new ArrayList<>();
        for (int i = 0; i < IProgWidget.WidgetDifficulty.values().length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, xStart + 263, yStart + 200 + i * 12, 0xFF000000, IProgWidget.WidgetDifficulty.values()[i].getLocalizedName());
            radioButton.checked = ConfigHandler.getProgrammerDifficulty() == i;
            addWidget(radioButton);
            difficultyButtons.add(radioButton);
            radioButton.otherChoices = difficultyButtons;
            if (i == 1) radioButton.setTooltip(I18n.format("gui.programmer.difficulty.medium.tooltip"));
            if (i == 2) radioButton.setTooltip(I18n.format("gui.programmer.difficulty.advanced.tooltip"));
        }

        buttonList.add(new GuiButton(5, xStart + 5, yStart + 175, 87, 20, I18n.format("gui.programmer.button.showStart")));
        buttonList.add(new GuiButton(6, xStart + 5, yStart + 197, 87, 20, I18n.format("gui.programmer.button.showLatest")));
        addWidget(showInfo = new GuiCheckBox(-1, xStart + 5, yStart + 220, 0xFF000000, "gui.programmer.checkbox.showInfo").setChecked(te.showInfo));
        addWidget(showFlow = new GuiCheckBox(-1, xStart + 5, yStart + 232, 0xFF000000, "gui.programmer.checkbox.showFlow").setChecked(te.showFlow));

        GuiButtonSpecial pastebinButton = new GuiButtonSpecial(7, guiLeft - 24, guiTop + 44, 20, 20, "");
        pastebinButton.setTooltipText(I18n.format("gui.remote.button.pastebinButton"));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        buttonList.add(pastebinButton);

        undoButton = new GuiButtonSpecial(9, guiLeft - 24, guiTop + 2, 20, 20, "");
        redoButton = new GuiButtonSpecial(10, guiLeft - 24, guiTop + 23, 20, 20, "");
        GuiButtonSpecial clearAllButton = new GuiButtonSpecial(11, guiLeft - 24, guiTop + 65, 20, 20, "");
        convertToRelativeButton = new GuiButtonSpecial(12, guiLeft - 24, guiTop + 86, 20, 20, "Rel");

        undoButton.setRenderedIcon(Textures.GUI_UNDO_ICON_LOCATION);
        redoButton.setRenderedIcon(Textures.GUI_REDO_ICON_LOCATION);
        clearAllButton.setRenderedIcon(Textures.GUI_DELETE_ICON_LOCATION);

        undoButton.setTooltipText(I18n.format("gui.programmer.button.undoButton.tooltip"));
        redoButton.setTooltipText(I18n.format("gui.programmer.button.redoButton.tooltip"));
        clearAllButton.setTooltipText(I18n.format("gui.programmer.button.clearAllButton.tooltip"));

        buttonList.add(undoButton);
        buttonList.add(redoButton);
        buttonList.add(clearAllButton);
        buttonList.add(convertToRelativeButton);

        String containerName = I18n.format(te.getName() + ".name");
        addLabel(containerName, guiLeft + 7, guiTop + 5, 0xFF404040);

        nameField = new WidgetTextField(fontRenderer, guiLeft + 200, guiTop + 5, 98, fontRenderer.FONT_HEIGHT);
        addWidget(nameField);

        filterField = new FilterTextField(fontRenderer, guiLeft + 78, guiTop + 26, 100, fontRenderer.FONT_HEIGHT);
        filterField.setListener(this);

        addWidget(filterField);

        String name = I18n.format("gui.programmer.name");
        addLabel(name, guiLeft + 197 - fontRenderer.getStringWidth(name), guiTop + 5, 0xFF404040);

        updateVisibleProgWidgets();
    }

    @Override
    protected Point getInvNameOffset() {
        return null;
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        boolean igwLoaded = Loader.isModLoaded(ModIds.IGWMOD);
        fontRenderer.drawString(widgetPage + 1 + "/" + (maxPage + 1), 305, 175, 0xFF000000);
        fontRenderer.drawString(I18n.format("gui.programmer.difficulty"), 263, 191, 0xFF000000);

        if (showingWidgetProgress == 0) {
            programmerUnit.renderForeground(x, y, draggingWidget);
        }

        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            if (widget != draggingWidget && x - guiLeft >= widget.getX()
                    && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2
                    && y - guiTop <= widget.getY() + widget.getHeight() / 2
                    && (!showingAllWidgets || filteredSpawnWidgets == null || filteredSpawnWidgets.get(i))) {
                List<String> tooltip = new ArrayList<>();
                widget.getTooltip(tooltip);
                if (igwLoaded) tooltip.add(I18n.format("gui.programmer.pressIForInfo"));
                if (tooltip.size() > 0) drawHoveringString(tooltip, x - guiLeft, y - guiTop, fontRenderer);
            }
        }

    }

    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        super.keyTyped(key, keyCode);

        if (nameField.isFocused() || filterField.isFocused() && keyCode != Keyboard.KEY_TAB) {
            return;
        }

        if (Keyboard.KEY_I == keyCode && Loader.isModLoaded(ModIds.IGWMOD)) {
            onIGWAction();
        }
        if (Keyboard.KEY_R == keyCode) {
            if (exportButton.getBounds().contains(lastMouseX, lastMouseY)) {
                NetworkHandler.sendToServer(new PacketGuiButton(0));
            }
        }
        if (Keyboard.KEY_SPACE == keyCode || Keyboard.KEY_TAB == keyCode) {
            toggleShowWidgets();
        }
        if (Keyboard.KEY_DELETE == keyCode) {
            IProgWidget widget = programmerUnit.getHoveredWidget(lastMouseX, lastMouseY);
            if (widget != null) {
                te.progWidgets.remove(widget);
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            }
        }
        if (Keyboard.KEY_Z == keyCode) {
            NetworkHandler.sendToServer(new PacketGuiButton(undoButton.id));
        }
        if (Keyboard.KEY_Y == keyCode) {
            NetworkHandler.sendToServer(new PacketGuiButton(redoButton.id));
        }
    }

    @Optional.Method(modid = ModIds.IGWMOD)
    private void onIGWAction() {
        int x = lastMouseX;
        int y = lastMouseY;

        IProgWidget hoveredWidget = programmerUnit.getHoveredWidget(x, y);
        if(hoveredWidget != null) {
            WikiRegistry.getWikiHooks().showWikiGui("pneumaticcraft:progwidget/" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, hoveredWidget.getWidgetString()));
        }

        for(IProgWidget widget : visibleSpawnWidgets) {
            if(widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                WikiRegistry.getWikiHooks().showWikiGui("pneumaticcraft:progwidget/" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, widget.getWidgetString()));
            }
        }
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawDefaultBackground();
        bindGuiTexture();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawModalRectWithCustomSizedTexture(xStart, yStart, 0, 0, xSize, ySize, xSize, ySize);

        programmerUnit.getScrollBar().setEnabled(showingWidgetProgress == 0);
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        if (showingWidgetProgress > 0) programmerUnit.getScrollBar().setCurrentState(programmerUnit.getLastZoom());

        programmerUnit.render(x, y, showFlow.checked, showInfo.checked && showingWidgetProgress == 0, draggingWidget == null);

        int origX = x;
        int origY = y;
        x -= programmerUnit.getTranslatedX();
        y -= programmerUnit.getTranslatedY();
        float scale = programmerUnit.getScale();
        x = (int) (x / scale);
        y = (int) (y / scale);

        if (showingWidgetProgress > 0) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            int width = oldShowingWidgetProgress + (int) ((showingWidgetProgress - oldShowingWidgetProgress) * partialTicks);
            for (int i = 0; i < width; i++) {
                drawModalRectWithCustomSizedTexture(xStart + 320 - i, yStart + 36, 323, 36, 1, 136, xSize, ySize);
            }
            drawModalRectWithCustomSizedTexture(xStart + 319 - width, yStart + 36, 319, 36, 2, 136, xSize, ySize);

            if (showingAllWidgets && draggingWidget != null) toggleShowWidgets();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        for (int i = 0; i < visibleSpawnWidgets.size(); i++) {
            IProgWidget widget = visibleSpawnWidgets.get(i);
            GlStateManager.pushMatrix();
            GlStateManager.translate(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GlStateManager.scale(0.5, 0.5, 1);
            if (showingAllWidgets && filteredSpawnWidgets != null && !filteredSpawnWidgets.get(i)) {
                GlStateManager.color(1, 1, 1, 0.2f);
            } else {
                GlStateManager.color(1, 1, 1, 1);
            }
            widget.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();

        GlStateManager.pushMatrix();
        GlStateManager.translate(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        GlStateManager.scale(scale, scale, 1);
        if (draggingWidget != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(draggingWidget.getX() + guiLeft, draggingWidget.getY() + guiTop, 0);
            GlStateManager.scale(0.5, 0.5, 1);
            draggingWidget.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        boolean isLeftClicking = Mouse.isButtonDown(0);
        boolean isMiddleClicking = GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindPickBlock);

        if (draggingWidget != null) {
            setConnectingWidgetsToXY(draggingWidget, x - dragMouseStartX + dragWidgetStartX - guiLeft, y - dragMouseStartY + dragWidgetStartY - guiTop);
        }

        if (isLeftClicking && !wasClicking) {
            for (IProgWidget widget : visibleSpawnWidgets) {
                if (origX >= widget.getX() + guiLeft && origY >= widget.getY() + guiTop && origX <= widget.getX() + guiLeft + widget.getWidth() / 2 && origY <= widget.getY() + guiTop + widget.getHeight() / 2) {
                    draggingWidget = widget.copy();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = x - (int) (guiLeft / scale);
                    dragMouseStartY = y - (int) (guiTop / scale);
                    dragWidgetStartX = (int) ((widget.getX() - programmerUnit.getTranslatedX()) / scale);
                    dragWidgetStartY = (int) ((widget.getY() - programmerUnit.getTranslatedY()) / scale);
                    break;
                }
            }
            
            // create area widgets straight from GPS Area Tools
            ItemStack heldItem = mc.player.inventory.getItemStack();
            ProgWidgetArea areaToolWidget = heldItem.getItem() instanceof ItemGPSAreaTool ? ItemGPSAreaTool.getArea(heldItem) : null;

            if (draggingWidget == null && showingWidgetProgress == 0) {
                IProgWidget widget = programmerUnit.getHoveredWidget(origX, origY);
                if (widget != null) {
                    draggingWidget = widget;
                    dragMouseStartX = x - guiLeft;
                    dragMouseStartY = y - guiTop;
                    dragWidgetStartX = widget.getX();
                    dragWidgetStartY = widget.getY();
                    
                    if (areaToolWidget != null && widget instanceof ProgWidgetArea) {
                        NBTTagCompound tag = new NBTTagCompound();
                        areaToolWidget.writeToNBT(tag);
                        widget.readFromNBT(tag);
                    } else if (heldItem.getItem() == Itemss.GPS_TOOL) {
                        if (widget instanceof ProgWidgetCoordinate) {
                            ((ProgWidgetCoordinate) widget).loadFromGPSTool(heldItem);
                        } else if (widget instanceof ProgWidgetArea) {
                            BlockPos pos = ItemGPSTool.getGPSLocation(heldItem);
                            String var = ItemGPSTool.getVariable(heldItem);
                            if (pos != null) ((ProgWidgetArea) widget).setP1(pos);
                            ((ProgWidgetArea) widget).setP2(new BlockPos(0, 0, 0));
                            ((ProgWidgetArea) widget).setCoord1Variable(var);
                            ((ProgWidgetArea) widget).setCoord2Variable("");
                        }
                    }
                }
            }

            // Create a new widget from a GPS Area tool when nothing was selected
            if (draggingWidget == null) {
                if (areaToolWidget != null) {
                    draggingWidget = areaToolWidget;
                } else if (heldItem.getItem() == Itemss.GPS_TOOL) {
                    if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                        BlockPos pos = ItemGPSTool.getGPSLocation(heldItem);
                        ProgWidgetArea areaWidget = ProgWidgetArea.fromPositions(pos, new BlockPos(0, 0, 0));
                        String var = ItemGPSTool.getVariable(heldItem);
                        if (!var.isEmpty()) areaWidget.setCoord1Variable(var);
                        draggingWidget = areaWidget;
                    } else {
                        ProgWidgetCoordinate coordWidget = new ProgWidgetCoordinate();
                        draggingWidget = coordWidget;
                        coordWidget.loadFromGPSTool(heldItem);
                    }
                }

                if (draggingWidget != null) {
                    draggingWidget.setX(Integer.MAX_VALUE);
                    draggingWidget.setY(Integer.MAX_VALUE);
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = draggingWidget.getWidth() / 3;
                    dragMouseStartY = draggingWidget.getHeight() / 4;
                    dragWidgetStartX = 0;
                    dragWidgetStartY = 0;
                }
            }
        } else if (isMiddleClicking && !wasClicking && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget(origX, origY);
            if (widget != null) {
                draggingWidget = widget.copy();
                te.progWidgets.add(draggingWidget);
                dragMouseStartX = 0;
                dragMouseStartY = 0;
                dragWidgetStartX = widget.getX() - (x - guiLeft);
                dragWidgetStartY = widget.getY() - (y - guiTop);
                if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) copyAndConnectConnectingWidgets(widget, draggingWidget);
            }
        } else if (isMiddleClicking && showingAllWidgets && Loader.isModLoaded(ModIds.IGWMOD)) {
            onIGWAction();
        }

        if (!isLeftClicking && !isMiddleClicking && draggingWidget != null) {
            if (programmerUnit.isOutsideProgrammingArea(draggingWidget)) {
                deleteConnectingWidgets(draggingWidget);
            } else {
                handlePuzzleMargins();
                if (!isValidPlaced(draggingWidget)) {
                    setConnectingWidgetsToXY(draggingWidget, dragWidgetStartX, dragWidgetStartY);
                    if (programmerUnit.isOutsideProgrammingArea(draggingWidget))
                        deleteConnectingWidgets(draggingWidget);
                }
            }
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            TileEntityProgrammer.updatePuzzleConnections(te.progWidgets);

            draggingWidget = null;
        }
        wasClicking = isLeftClicking || isMiddleClicking;
        lastMouseX = origX;
        lastMouseY = origY;
    }

    private boolean isValidPlaced(IProgWidget widget1) {
        Rectangle draggingRect = new Rectangle(widget1.getX(), widget1.getY(), widget1.getWidth() / 2, widget1.getHeight() / 2);
        for (IProgWidget widget : te.progWidgets) {
            if (widget != widget1) {
                if (draggingRect.intersects(widget.getX(), widget.getY(), widget.getWidth() / 2, widget.getHeight() / 2)) {
                    return false;
                }
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

    private void handlePuzzleMargins() {
        //Check for connection to the left of the dragged widget.
        Class<? extends IProgWidget> returnValue = draggingWidget.returnType();
        if (returnValue != null) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget != draggingWidget && Math.abs(widget.getX() + widget.getWidth() / 2 - draggingWidget.getX()) <= FAULT_MARGIN) {
                    Class<? extends IProgWidget>[] parameters = widget.getParameters();
                    if (parameters != null) {
                        for (int i = 0; i < parameters.length; i++) {
                            if (widget.canSetParameter(i) && parameters[i] == returnValue && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() + widget.getWidth() / 2, widget.getY() + i * 11);
                                return;
                            }
                        }
                    }
                }
            }
        }

        //check for connection to the right of the dragged widget.
        Class<? extends IProgWidget>[] parameters = draggingWidget.getParameters();
        if (parameters != null) {
            for (IProgWidget widget : te.progWidgets) {
                IProgWidget outerPiece = draggingWidget;
                if (outerPiece.returnType() != null) {//When the piece is a parameter pice (area, item filter, text).
                    while (outerPiece.getConnectedParameters()[0] != null) {
                        outerPiece = outerPiece.getConnectedParameters()[0];
                    }
                }
                if (widget != draggingWidget && Math.abs(outerPiece.getX() + outerPiece.getWidth() / 2 - widget.getX()) <= FAULT_MARGIN) {
                    if (widget.returnType() != null) {
                        for (int i = 0; i < parameters.length; i++) {
                            if (draggingWidget.canSetParameter(i) && parameters[i] == widget.returnType() && Math.abs(draggingWidget.getY() + i * 11 - widget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() - i * 11);
                            }
                        }
                    } else {
                        Class<? extends IProgWidget>[] checkingPieceParms = widget.getParameters();
                        if (checkingPieceParms != null) {
                            for (int i = 0; i < checkingPieceParms.length; i++) {
                                if (widget.canSetParameter(i + parameters.length) && checkingPieceParms[i] == parameters[0] && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
                                    setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() + i * 11);
                                }
                            }
                        }
                    }
                }
            }
        }

        //check for connection to the top of the dragged widget.
        if (draggingWidget.hasStepInput()) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget.hasStepOutput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() + widget.getHeight() / 2 - draggingWidget.getY()) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() + widget.getHeight() / 2);
                }
            }
        }

        //check for connection to the bottom of the dragged widget.
        if (draggingWidget.hasStepOutput()) {
            for (IProgWidget widget : te.progWidgets) {
                if (widget.hasStepInput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() - draggingWidget.getY() - draggingWidget.getHeight() / 2) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() - draggingWidget.getHeight() / 2);
                }
            }
        }
    }

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

    private void deleteConnectingWidgets(IProgWidget widget) {
        te.progWidgets.remove(widget);
        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if (connectingWidgets != null) {
            for (IProgWidget widg : connectingWidgets) {
                if (widg != null) deleteConnectingWidgets(widg);
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if (outputWidget != null) deleteConnectingWidgets(outputWidget);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:// redstone button
                //          redstoneBehaviourStat.closeWindow();
                break;
            case 3:
                if (--widgetPage < 0) widgetPage = maxPage;
                updateVisibleProgWidgets();
                return;
            case 4:
                if (++widgetPage > maxPage) widgetPage = 0;
                updateVisibleProgWidgets();
                return;
            case 5:
                programmerUnit.gotoPiece(findWidget(te.progWidgets, ProgWidgetStart.class));
//                for (IProgWidget widget : te.progWidgets) {
//                    if (widget instanceof ProgWidgetStart) {
//                        programmerUnit.gotoPiece(widget);
//                        break;
//                    }
//                }
                return;
            case 6:
                if (te.progWidgets.size() > 0) {
                    programmerUnit.gotoPiece(te.progWidgets.get(te.progWidgets.size() - 1));
                }
                return;
            case 7:
                NBTTagCompound mainTag = new NBTTagCompound();
                te.writeProgWidgetsToNBT(mainTag);
                FMLClientHandler.instance().showGuiScreen(pastebinGui = new GuiPastebin(this, mainTag));
                break;
            case 11:
                te.progWidgets.clear();
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
                break;
            case 12:
                for (IProgWidget widget : te.progWidgets) {
                    if (widget instanceof ProgWidgetStart) {
                        generateRelativeOperators((ProgWidgetCoordinateOperator) widget.getOutputWidget(), null, false);
                        break;
                    }
                }
                break;
        }

        NetworkHandler.sendToServer(new PacketGuiButton(button.id));
    }

    private void toggleShowWidgets() {
        showingAllWidgets = !showingAllWidgets;
        allWidgetsButton.displayString = showingAllWidgets ? ">" : "<";
        updateVisibleProgWidgets();
        filterField.setFocused(showingAllWidgets);
    }

    @Override
    public void actionPerformed(IGuiWidget button) {
        if (button == allWidgetsButton) {
            toggleShowWidgets();
        } else {
            for (int i = 0; i < difficultyButtons.size(); i++) {
                if (difficultyButtons.get(i).checked) {
                    ConfigHandler.setProgrammerDifficulty(i);
                    break;
                }
            }
            if (showingAllWidgets) {
                toggleShowWidgets();
            }
            updateVisibleProgWidgets();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        undoButton.enabled = te.canUndo;
        redoButton.enabled = te.canRedo;

        updateConvertRelativeState();

        ItemStack programmedItem = te.getIteminProgrammingSlot();
        oldShowingWidgetProgress = showingWidgetProgress;
        if (showingAllWidgets) {
            int maxProgress = maxPage * WIDGET_X_SPACING;
            if (showingWidgetProgress < maxProgress) {
                showingWidgetProgress += 60;
                if (showingWidgetProgress >= maxProgress) {
                    showingWidgetProgress = maxProgress;
                    updateVisibleProgWidgets();
                }
            }
        } else {
            showingWidgetProgress -= 60;
            if (showingWidgetProgress < 0) showingWidgetProgress = 0;
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (IProgWidget w : te.progWidgets) {
            w.addErrors(errors, te.progWidgets);
            w.addWarnings(warnings, te.progWidgets);
        }

        boolean isDeviceInserted = !programmedItem.isEmpty();
        importButton.enabled = isDeviceInserted;
        exportButton.enabled = isDeviceInserted && errors.size() == 0;

        List<String> exportButtonTooltip = new ArrayList<>();
        exportButtonTooltip.add("Export program");
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.programmingWhen", I18n.format("gui.programmer.button.export." + (te.redstoneMode == 0 ? "pressingButton" : "onItemInsert"))));
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.pressRToChange"));
        if (!programmedItem.isEmpty()) {
            List<ItemStack> requiredPieces = te.getRequiredPuzzleStacks();
            List<ItemStack> returnedPieces = te.getReturnedPuzzleStacks();
            if (!requiredPieces.isEmpty() || !returnedPieces.isEmpty()) exportButtonTooltip.add("");
            if (!requiredPieces.isEmpty()) {
                exportButtonTooltip.add(I18n.format("gui.tooltip.programmable.requiredPieces"));
                if (player.capabilities.isCreativeMode)
                    exportButtonTooltip.add("(Creative mode, so the following is free)");
                for (ItemStack stack : requiredPieces) {
                    String prefix;
                    if (te.hasEnoughPuzzleStacks(player, stack)) {
                        prefix = TextFormatting.GREEN.toString();
                    } else {
                        prefix = TextFormatting.RED.toString();
                        exportButton.enabled = player.capabilities.isCreativeMode && errors.size() == 0;
                    }
                    exportButtonTooltip.add(prefix + "-" + stack.getCount() + "x " + stack.getDisplayName());
                }
            }
            if (!returnedPieces.isEmpty()) {
                exportButtonTooltip.add("Returned Programming Puzzles:");
                if (player.capabilities.isCreativeMode) exportButtonTooltip.add("(Creative mode, nothing's given)");
                for (ItemStack stack : returnedPieces) {
                    exportButtonTooltip.add("-" + stack.getCount() + "x " + stack.getDisplayName());
                }
            }
        } else {
            exportButtonTooltip.add("No programmable item inserted.");
        }

        if (errors.size() > 0)
            exportButtonTooltip.add(TextFormatting.RED + I18n.format("gui.programmer.errorCount", errors.size()));
        if (warnings.size() > 0)
            exportButtonTooltip.add(TextFormatting.YELLOW + I18n.format("gui.programmer.warningCount", warnings.size()));

        exportButton.setTooltipText(exportButtonTooltip);
        if (!programmedItem.isEmpty()) {
            nameField.setEnabled(true);
            if (!nameField.isFocused()) {
                if (wasFocused) {
                    programmedItem.setStackDisplayName(nameField.getText());
                    NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
                }
                nameField.setText(programmedItem.getDisplayName());
                wasFocused = false;
            } else {
                wasFocused = true;
            }
        } else {
            nameField.setEnabled(false);
            nameField.setText("");
            wasFocused = false;
        }
    }

    private void updateConvertRelativeState() {
        convertToRelativeButton.enabled = false;
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
                                convertToRelativeButton.enabled = true;
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
            localizedTooltip.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(s), 40));
        }
        convertToRelativeButton.setTooltipText(localizedTooltip);
    }

    /**
     * @param baseWidget
     * @param simulate
     * @return true if successful
     */
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
                    if (PneumaticCraftUtils.distBetween(c, 0, 0, 0) < 64) { //When the coordinate value is close to 0, there's a low chance it means a position, and rather an offset.
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
        if (offset.equals(new BlockPos(0, 0, 0)))
            return baseVariable;
        return offsetToVariableNames.computeIfAbsent(offset, k -> "var" + (offsetToVariableNames.size() + 1));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ItemStack programmedItem = te.getIteminProgrammingSlot();
        if (nameField.isFocused() && !programmedItem.isEmpty()) {
            programmedItem.setStackDisplayName(nameField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 1 && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget(mouseX, mouseY);
            if (widget != null) {
                GuiScreen screen = widget.getOptionWindow(this);
                if (screen != null) mc.displayGuiScreen(screen);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        te.translatedX = programmerUnit.getTranslatedX();
        te.translatedY = programmerUnit.getTranslatedY();
        te.zoomState = programmerUnit.getLastZoom();
        te.showFlow = showFlow.checked;
        te.showInfo = showInfo.checked;
        super.onGuiClosed();
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
        if (widget.getID() == filterField.getID()) {
            filterSpawnWidgets();
        }
    }

    public static IProgWidget findWidget(List<IProgWidget> widgets, Class<? extends IProgWidget> cls) {
        for (IProgWidget w : widgets) {
            if (cls.isAssignableFrom(w.getClass())) return w;
        }
        return null;
    }

    private class FilterTextField extends WidgetTextField {
        FilterTextField(FontRenderer fontRenderer, int x, int y, int width, int height) {
            super(fontRenderer, x, y, width, height);
        }

        @Override
        public void drawTextBox() {
            // this is needed to force the textfield to draw on top of any
            // widgets in the programming area
            GlStateManager.translate(0, 0, 300);
            super.drawTextBox();
            GlStateManager.translate(0, 0, -300);
        }
    }
}
