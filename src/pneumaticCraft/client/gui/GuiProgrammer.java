package pneumaticCraft.client.gui;

import igwmod.gui.GuiWiki;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkPosition;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.inventory.ContainerProgrammer;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetCoordinate;
import pneumaticCraft.common.progwidgets.ProgWidgetCoordinateOperator;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;
import pneumaticCraft.common.progwidgets.WidgetRegistrator;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Textures;
import codechicken.nei.VisiblityData;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiProgrammer extends GuiPneumaticContainerBase<TileEntityProgrammer>{
    private final EntityPlayer player;
    private GuiPastebin pastebinGui;

    //  private GuiButton redstoneButton;
    private GuiButtonSpecial importButton;
    private GuiButtonSpecial exportButton;
    private GuiButtonSpecial allWidgetsButton;
    private List<GuiRadioButton> difficultyButtons;
    private GuiCheckBox showInfo, showFlow;
    private WidgetTextField nameField;
    private GuiButtonSpecial undoButton, redoButton;
    private GuiButtonSpecial convertToRelativeButton;

    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<IProgWidget>();

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

    public GuiProgrammer(InventoryPlayer player, TileEntityProgrammer te){

        super(new ContainerProgrammer(player, te), te, Textures.GUI_PROGRAMMER);
        ySize = 256;
        xSize = 350;

        this.player = FMLClientHandler.instance().getClient().thePlayer;
    }

    private void updateVisibleProgWidgets(){
        int y = 0, page = 0;
        int xSpacing = 22;
        int x = 322 - maxPage * xSpacing;
        boolean showAllWidgets = showingWidgetProgress == xSpacing * maxPage && showingAllWidgets;
        maxPage = 0;
        visibleSpawnWidgets.clear();
        int difficulty = 0;
        for(int i = 0; i < difficultyButtons.size(); i++) {
            if(difficultyButtons.get(i).checked) {
                difficulty = i;
                break;
            }
        }
        for(IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            if(difficulty >= widget.getDifficulty().ordinal()) {
                widget.setY(y + 40);
                widget.setX(showAllWidgets ? x : 322);
                int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
                y += widgetHeight;

                if(showAllWidgets || page == widgetPage) visibleSpawnWidgets.add(widget);
                if(y > ySize - 160) {
                    y = 0;
                    x += xSpacing;
                    page++;
                    maxPage++;
                }

            }
        }
        if(widgetPage > maxPage) {
            widgetPage = maxPage;
            updateVisibleProgWidgets();
        }
    }

    @Override
    protected boolean shouldAddInfoTab(){
        return false;
    }

    @Override
    public void initGui(){
        if(pastebinGui != null && pastebinGui.outputTag != null) {
            te.readProgWidgetsFromNBT(pastebinGui.outputTag);
            pastebinGui = null;
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
        }

        super.initGui();

        if(programmerUnit != null) {
            te.translatedX = programmerUnit.getTranslatedX();
            te.translatedY = programmerUnit.getTranslatedY();
            te.zoomState = programmerUnit.getLastZoom();
        }

        programmerUnit = new GuiUnitProgrammer(te.progWidgets, fontRendererObj, guiLeft, guiTop, xSize, width, height, PROGRAMMING_START_X, PROGRAMMING_START_Y, PROGRAMMING_WIDTH, PROGRAMMING_HEIGHT, te.translatedX, te.translatedY, te.zoomState);
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

        difficultyButtons = new ArrayList<GuiRadioButton>();
        for(int i = 0; i < IProgWidget.WidgetDifficulty.values().length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, xStart + 263, yStart + 200 + i * 12, 0xFF000000, IProgWidget.WidgetDifficulty.values()[i].getLocalizedName());
            radioButton.checked = Config.getProgrammerDifficulty() == i;
            addWidget(radioButton);
            difficultyButtons.add(radioButton);
            radioButton.otherChoices = difficultyButtons;
            if(i == 1) radioButton.setTooltip(I18n.format("gui.programmer.difficulty.medium.tooltip"));
            if(i == 2) radioButton.setTooltip(I18n.format("gui.programmer.difficulty.advanced.tooltip"));
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

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName() + ".name");
        addLabel(containerName, guiLeft + 7, guiTop + 5);

        nameField = new WidgetTextField(fontRendererObj, guiLeft + 200, guiTop + 5, 98, fontRendererObj.FONT_HEIGHT);
        addWidget(nameField);

        String name = I18n.format("gui.programmer.name");
        addLabel(name, guiLeft + 197 - fontRendererObj.getStringWidth(name), guiTop + 5);

        updateVisibleProgWidgets();
    }

    @Override
    protected Point getInvNameOffset(){
        return null;
    }

    @Override
    protected Point getInvTextOffset(){
        return null;
    }

    @Override
    protected boolean shouldAddProblemTab(){
        return false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);

        boolean igwLoaded = Loader.isModLoaded(ModIds.IGWMOD);
        fontRendererObj.drawString(widgetPage + 1 + "/" + (maxPage + 1), 305, 175, 0xFF000000);
        fontRendererObj.drawString(I18n.format("gui.programmer.difficulty"), 263, 190, 0xFF000000);

        if(showingWidgetProgress == 0) {
            programmerUnit.renderForeground(x, y, draggingWidget);
        }

        for(IProgWidget widget : visibleSpawnWidgets) {
            if(widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                List<String> tooltip = new ArrayList<String>();
                widget.getTooltip(tooltip);
                if(igwLoaded) tooltip.add(I18n.format("gui.programmer.pressIForInfo"));
                if(tooltip.size() > 0) drawHoveringString(tooltip, x - guiLeft, y - guiTop, fontRendererObj);
            }
        }

    }

    @Override
    protected void keyTyped(char key, int keyCode){
        super.keyTyped(key, keyCode);

        if(Keyboard.KEY_I == keyCode && Loader.isModLoaded(ModIds.IGWMOD)) {
            onIGWAction();
        }
        if(Keyboard.KEY_R == keyCode) {
            if(exportButton.getBounds().contains(lastMouseX, lastMouseY)) {
                NetworkHandler.sendToServer(new PacketGuiButton(0));
            }
        }
        if(Keyboard.KEY_SPACE == keyCode) {
            toggleShowWidgets();
        }
        if(Keyboard.KEY_DELETE == keyCode) {
            IProgWidget widget = programmerUnit.getHoveredWidget(lastMouseX, lastMouseY);
            if(widget != null) {
                te.progWidgets.remove(widget);
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            }
        }
        if(Keyboard.KEY_Z == keyCode) {
            NetworkHandler.sendToServer(new PacketGuiButton(undoButton.id));
        }
        if(Keyboard.KEY_Y == keyCode) {
            NetworkHandler.sendToServer(new PacketGuiButton(redoButton.id));
        }
    }

    @Optional.Method(modid = ModIds.IGWMOD)
    private void onIGWAction(){
        int x = lastMouseX;
        int y = lastMouseY;

        IProgWidget hoveredWidget = programmerUnit.getHoveredWidget(x, y);
        if(hoveredWidget != null) {
            GuiWiki gui = new GuiWiki();
            FMLClientHandler.instance().showGuiScreen(gui);
            gui.setCurrentFile("pneumaticcraft:progwidget/" + hoveredWidget.getWidgetString());
        }

        for(IProgWidget widget : visibleSpawnWidgets) {
            if(widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                GuiWiki gui = new GuiWiki();
                FMLClientHandler.instance().showGuiScreen(gui);
                gui.setCurrentFile("pneumaticcraft:progwidget/" + widget.getWidgetString());
            }
        }
    }

    @Override
    protected boolean shouldDrawBackground(){
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindGuiTexture();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        func_146110_a(xStart, yStart, 0, 0, xSize, ySize, xSize, ySize);

        programmerUnit.getScrollBar().setEnabled(showingWidgetProgress == 0);
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        if(showingWidgetProgress > 0) programmerUnit.getScrollBar().setCurrentState(programmerUnit.getLastZoom());

        programmerUnit.render(x, y, showFlow.checked, showInfo.checked && showingWidgetProgress == 0, draggingWidget == null);

        int origX = x;
        int origY = y;
        x -= programmerUnit.getTranslatedX();
        y -= programmerUnit.getTranslatedY();
        float scale = programmerUnit.getScale();
        x = (int)(x / scale);
        y = (int)(y / scale);

        if(showingWidgetProgress > 0) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            int width = oldShowingWidgetProgress + (int)((showingWidgetProgress - oldShowingWidgetProgress) * partialTicks);
            for(int i = 0; i < width; i++) {
                func_146110_a(xStart + 320 - i, yStart + 36, 323, 36, 1, 136, xSize, ySize);
            }
            func_146110_a(xStart + 319 - width, yStart + 36, 319, 36, 2, 136, xSize, ySize);

            if(showingAllWidgets && draggingWidget != null) toggleShowWidgets();
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        for(IProgWidget widget : visibleSpawnWidgets) {
            GL11.glPushMatrix();
            GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GL11.glScaled(0.5, 0.5, 1);
            widget.render();
            GL11.glPopMatrix();
        }

        GL11.glPushMatrix();
        GL11.glTranslated(programmerUnit.getTranslatedX(), programmerUnit.getTranslatedY(), 0);
        GL11.glScaled(scale, scale, 1);
        if(draggingWidget != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(draggingWidget.getX() + guiLeft, draggingWidget.getY() + guiTop, 0);
            GL11.glScaled(0.5, 0.5, 1);
            draggingWidget.render();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        boolean isLeftClicking = Mouse.isButtonDown(0);
        boolean isMiddleClicking = GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindPickBlock);

        if(draggingWidget != null) {
            setConnectingWidgetsToXY(draggingWidget, x - dragMouseStartX + dragWidgetStartX - guiLeft, y - dragMouseStartY + dragWidgetStartY - guiTop);
        }

        if(isLeftClicking && !wasClicking) {
            for(IProgWidget widget : visibleSpawnWidgets) {
                if(origX >= widget.getX() + guiLeft && origY >= widget.getY() + guiTop && origX <= widget.getX() + guiLeft + widget.getWidth() / 2 && origY <= widget.getY() + guiTop + widget.getHeight() / 2) {
                    draggingWidget = widget.copy();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = x - (int)(guiLeft / scale);
                    dragMouseStartY = y - (int)(guiTop / scale);
                    dragWidgetStartX = (int)((widget.getX() - programmerUnit.getTranslatedX()) / scale);
                    dragWidgetStartY = (int)((widget.getY() - programmerUnit.getTranslatedY()) / scale);
                    break;
                }
            }
            if(draggingWidget == null && showingWidgetProgress == 0) {
                IProgWidget widget = programmerUnit.getHoveredWidget(origX, origY);
                if(widget != null) {
                    draggingWidget = widget;
                    dragMouseStartX = x - guiLeft;
                    dragMouseStartY = y - guiTop;
                    dragWidgetStartX = widget.getX();
                    dragWidgetStartY = widget.getY();
                }
            }
        } else if(isMiddleClicking && !wasClicking && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget(origX, origY);
            if(widget != null) {
                draggingWidget = widget.copy();
                te.progWidgets.add(draggingWidget);
                dragMouseStartX = 0;
                dragMouseStartY = 0;
                dragWidgetStartX = widget.getX() - (x - guiLeft);
                dragWidgetStartY = widget.getY() - (y - guiTop);
                if(PneumaticCraft.proxy.isSneakingInGui()) copyAndConnectConnectingWidgets(widget, draggingWidget);
            }
        }

        if(!isLeftClicking && !isMiddleClicking && draggingWidget != null) {
            if(programmerUnit.isOutsideProgrammingArea(draggingWidget)) {
                deleteConnectingWidgets(draggingWidget);
            } else {
                handlePuzzleMargins();
                if(!isValidPlaced(draggingWidget)) {
                    setConnectingWidgetsToXY(draggingWidget, dragWidgetStartX, dragWidgetStartY);
                    if(programmerUnit.isOutsideProgrammingArea(draggingWidget)) deleteConnectingWidgets(draggingWidget);
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

    private boolean isValidPlaced(IProgWidget widget1){
        Rectangle draggingRect = new Rectangle(widget1.getX(), widget1.getY(), widget1.getWidth() / 2, widget1.getHeight() / 2);
        for(IProgWidget widget : te.progWidgets) {
            if(widget != widget1) {
                if(draggingRect.intersects(widget.getX(), widget.getY(), widget.getWidth() / 2, widget.getHeight() / 2)) {
                    return false;
                }
            }
        }
        IProgWidget[] parameters = widget1.getConnectedParameters();
        if(parameters != null) {
            for(IProgWidget widget : parameters) {
                if(widget != null && !isValidPlaced(widget)) return false;
            }
        }
        IProgWidget outputWidget = widget1.getOutputWidget();
        if(outputWidget != null && !isValidPlaced(outputWidget)) return false;
        return true;
    }

    private void handlePuzzleMargins(){
        //Check for connection to the left of the dragged widget.
        Class<? extends IProgWidget> returnValue = draggingWidget.returnType();
        if(returnValue != null) {
            for(IProgWidget widget : te.progWidgets) {
                if(widget != draggingWidget && Math.abs(widget.getX() + widget.getWidth() / 2 - draggingWidget.getX()) <= FAULT_MARGIN) {
                    Class<? extends IProgWidget>[] parameters = widget.getParameters();
                    if(parameters != null) {
                        for(int i = 0; i < parameters.length; i++) {
                            if(widget.canSetParameter(i) && parameters[i] == returnValue && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
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
        if(parameters != null) {
            for(IProgWidget widget : te.progWidgets) {
                IProgWidget outerPiece = draggingWidget;
                if(outerPiece.returnType() != null) {//When the piece is a parameter pice (area, item filter, text).
                    while(outerPiece.getConnectedParameters()[0] != null) {
                        outerPiece = outerPiece.getConnectedParameters()[0];
                    }
                }
                if(widget != draggingWidget && Math.abs(outerPiece.getX() + outerPiece.getWidth() / 2 - widget.getX()) <= FAULT_MARGIN) {
                    if(widget.returnType() != null) {
                        for(int i = 0; i < parameters.length; i++) {
                            if(draggingWidget.canSetParameter(i) && parameters[i] == widget.returnType() && Math.abs(draggingWidget.getY() + i * 11 - widget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() - i * 11);
                            }
                        }
                    } else {
                        Class<? extends IProgWidget>[] checkingPieceParms = widget.getParameters();
                        if(checkingPieceParms != null) {
                            for(int i = 0; i < checkingPieceParms.length; i++) {
                                if(widget.canSetParameter(i + parameters.length) && checkingPieceParms[i] == parameters[0] && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
                                    setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() + i * 11);
                                }
                            }
                        }
                    }
                }
            }
        }

        //check for connection to the top of the dragged widget.
        if(draggingWidget.hasStepInput()) {
            for(IProgWidget widget : te.progWidgets) {
                if(widget.hasStepOutput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() + widget.getHeight() / 2 - draggingWidget.getY()) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() + widget.getHeight() / 2);
                }
            }
        }

        //check for connection to the bottom of the dragged widget.
        if(draggingWidget.hasStepOutput()) {
            for(IProgWidget widget : te.progWidgets) {
                if(widget.hasStepInput() && Math.abs(widget.getX() - draggingWidget.getX()) <= FAULT_MARGIN && Math.abs(widget.getY() - draggingWidget.getY() - draggingWidget.getHeight() / 2) <= FAULT_MARGIN) {
                    setConnectingWidgetsToXY(draggingWidget, widget.getX(), widget.getY() - draggingWidget.getHeight() / 2);
                }
            }
        }
    }

    private void setConnectingWidgetsToXY(IProgWidget widget, int x, int y){
        widget.setX(x);
        widget.setY(y);
        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if(connectingWidgets != null) {
            for(int i = 0; i < connectingWidgets.length; i++) {
                if(connectingWidgets[i] != null) {
                    if(i < connectingWidgets.length / 2) {
                        setConnectingWidgetsToXY(connectingWidgets[i], x + widget.getWidth() / 2, y + i * 11);
                    } else {
                        int totalWidth = 0;
                        IProgWidget branch = connectingWidgets[i];
                        while(branch != null) {
                            totalWidth += branch.getWidth() / 2;
                            branch = branch.getConnectedParameters()[0];
                        }
                        setConnectingWidgetsToXY(connectingWidgets[i], x - totalWidth, y + (i - connectingWidgets.length / 2) * 11);
                    }
                }
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if(outputWidget != null) setConnectingWidgetsToXY(outputWidget, x, y + widget.getHeight() / 2);
    }

    private void copyAndConnectConnectingWidgets(IProgWidget original, IProgWidget copy){
        IProgWidget[] connectingWidgets = original.getConnectedParameters();
        if(connectingWidgets != null) {
            for(int i = 0; i < connectingWidgets.length; i++) {
                if(connectingWidgets[i] != null) {
                    IProgWidget c = connectingWidgets[i].copy();
                    te.progWidgets.add(c);
                    copy.setParameter(i, c);
                    copyAndConnectConnectingWidgets(connectingWidgets[i], c);
                }
            }
        }
        IProgWidget outputWidget = original.getOutputWidget();
        if(outputWidget != null) {
            IProgWidget c = outputWidget.copy();
            te.progWidgets.add(c);
            copy.setOutputWidget(c);
            copyAndConnectConnectingWidgets(outputWidget, c);
        }
    }

    private void deleteConnectingWidgets(IProgWidget widget){
        te.progWidgets.remove(widget);
        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if(connectingWidgets != null) {
            for(IProgWidget widg : connectingWidgets) {
                if(widg != null) deleteConnectingWidgets(widg);
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if(outputWidget != null) deleteConnectingWidgets(outputWidget);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        switch(button.id){
            case 0:// redstone button
                   //          redstoneBehaviourStat.closeWindow();
                break;
            case 3:
                if(--widgetPage < 0) widgetPage = maxPage;
                updateVisibleProgWidgets();
                return;
            case 4:
                if(++widgetPage > maxPage) widgetPage = 0;
                updateVisibleProgWidgets();
                return;
            case 5:
                for(IProgWidget widget : te.progWidgets) {
                    if(widget instanceof ProgWidgetStart) {
                        programmerUnit.gotoPiece(widget);
                        break;
                    }
                }
                return;
            case 6:
                if(te.progWidgets.size() > 0) {
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
                for(IProgWidget widget : te.progWidgets) {
                    if(widget instanceof ProgWidgetStart) {
                        generateRelativeOperators((ProgWidgetCoordinateOperator)widget.getOutputWidget(), null, false);
                        break;
                    }
                }
                break;
        }

        NetworkHandler.sendToServer(new PacketGuiButton(button.id));
    }

    private void toggleShowWidgets(){
        showingAllWidgets = !showingAllWidgets;
        allWidgetsButton.displayString = showingAllWidgets ? ">" : "<";
        updateVisibleProgWidgets();
    }

    @Override
    public void actionPerformed(IGuiWidget button){
        if(button == allWidgetsButton) {
            toggleShowWidgets();
        } else {
            for(int i = 0; i < difficultyButtons.size(); i++) {
                if(difficultyButtons.get(i).checked) {
                    Config.setProgrammerDifficulty(i);
                    break;
                }
            }
            if(showingAllWidgets) {
                toggleShowWidgets();
            }
            updateVisibleProgWidgets();
        }
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        undoButton.enabled = te.canUndo;
        redoButton.enabled = te.canRedo;

        updateConvertRelativeState();

        ItemStack programmedItem = te.getStackInSlot(TileEntityProgrammer.PROGRAM_SLOT);
        oldShowingWidgetProgress = showingWidgetProgress;
        if(showingAllWidgets) {
            int maxProgress = maxPage * 22;
            if(showingWidgetProgress < maxProgress) {
                showingWidgetProgress += 30;
                if(showingWidgetProgress >= maxProgress) {
                    showingWidgetProgress = maxProgress;
                    updateVisibleProgWidgets();
                }
            }
        } else {
            showingWidgetProgress -= 30;
            if(showingWidgetProgress < 0) showingWidgetProgress = 0;
        }

        List<String> errors = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();
        for(IProgWidget w : te.progWidgets) {
            w.addErrors(errors, te.progWidgets);
            w.addWarnings(warnings, te.progWidgets);
        }

        boolean isDeviceInserted = programmedItem != null;
        importButton.enabled = isDeviceInserted;
        exportButton.enabled = isDeviceInserted && errors.size() == 0;

        List<String> exportButtonTooltip = new ArrayList<String>();
        exportButtonTooltip.add("Export program");
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.programmingWhen", I18n.format("gui.programmer.button.export." + (te.redstoneMode == 0 ? "pressingButton" : "onItemInsert"))));
        exportButtonTooltip.add(I18n.format("gui.programmer.button.export.pressRToChange"));
        if(programmedItem != null) {
            List<ItemStack> requiredPieces = te.getRequiredPuzzleStacks();
            List<ItemStack> returnedPieces = te.getReturnedPuzzleStacks();
            if(!requiredPieces.isEmpty() || !returnedPieces.isEmpty()) exportButtonTooltip.add("");
            if(!requiredPieces.isEmpty()) {
                exportButtonTooltip.add("Required Programming Puzzles:");
                if(player.capabilities.isCreativeMode) exportButtonTooltip.add("(Creative mode, so the following is free)");
                for(ItemStack stack : requiredPieces) {
                    String prefix;
                    if(te.hasEnoughPuzzleStacks(player, stack)) {
                        prefix = EnumChatFormatting.GREEN.toString();
                    } else {
                        prefix = EnumChatFormatting.RED.toString();
                        exportButton.enabled = player.capabilities.isCreativeMode && errors.size() == 0;
                    }
                    exportButtonTooltip.add(prefix + "-" + stack.stackSize + "x " + stack.getDisplayName());
                }
            }
            if(!returnedPieces.isEmpty()) {
                exportButtonTooltip.add("Returned Programming Puzzles:");
                if(player.capabilities.isCreativeMode) exportButtonTooltip.add("(Creative mode, nothing's given)");
                for(ItemStack stack : returnedPieces) {
                    exportButtonTooltip.add("-" + stack.stackSize + "x " + stack.getDisplayName());
                }
            }
        } else {
            exportButtonTooltip.add("No programmable item inserted.");
        }

        if(errors.size() > 0) exportButtonTooltip.add(EnumChatFormatting.RED + I18n.format("gui.programmer.errorCount", errors.size()));
        if(warnings.size() > 0) exportButtonTooltip.add(EnumChatFormatting.YELLOW + I18n.format("gui.programmer.warningCount", warnings.size()));

        exportButton.setTooltipText(exportButtonTooltip);
        if(programmedItem != null) {
            nameField.setEnabled(true);
            if(!nameField.isFocused()) {
                if(wasFocused) {
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

    private void updateConvertRelativeState(){
        convertToRelativeButton.enabled = false;
        List<String> tooltip = new ArrayList<String>();
        tooltip.add("gui.programmer.button.convertToRelative.desc");

        boolean startFound = false;
        for(IProgWidget startWidget : te.progWidgets) {
            if(startWidget instanceof ProgWidgetStart) {
                startFound = true;
                IProgWidget widget = startWidget.getOutputWidget();
                if(widget instanceof ProgWidgetCoordinateOperator) {
                    ProgWidgetCoordinateOperator operatorWidget = (ProgWidgetCoordinateOperator)widget;
                    if(!operatorWidget.getVariable().equals("")) {
                        try {
                            if(generateRelativeOperators(operatorWidget, tooltip, true)) {
                                convertToRelativeButton.enabled = true;
                            } else {
                                tooltip.add("gui.programmer.button.convertToRelative.notEnoughRoom");
                            }
                        } catch(NullPointerException e) {
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
        if(!startFound) tooltip.add("gui.programmer.button.convertToRelative.noStartPiece");

        List<String> localizedTooltip = new ArrayList<String>();
        for(String s : tooltip) {
            localizedTooltip.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format(s), 40));
        }
        convertToRelativeButton.setTooltipText(localizedTooltip);
    }

    /**
     * 
     * @param baseWidget
     * @param simulate
     * @return true if successful
     */
    private boolean generateRelativeOperators(ProgWidgetCoordinateOperator baseWidget, List<String> tooltip, boolean simulate){
        ChunkPosition baseCoord = ProgWidgetCoordinateOperator.calculateCoordinate(baseWidget, 0, baseWidget.getOperator());
        Map<ChunkPosition, String> offsetToVariableNames = new HashMap<ChunkPosition, String>();
        for(IProgWidget widget : te.progWidgets) {
            if(widget instanceof ProgWidgetArea) {
                ProgWidgetArea area = (ProgWidgetArea)widget;
                if(area.getCoord1Variable().equals("") && (area.x1 != 0 || area.y1 != 0 || area.z1 != 0)) {
                    ChunkPosition offset = new ChunkPosition(area.x1 - baseCoord.chunkPosX, area.y1 - baseCoord.chunkPosY, area.z1 - baseCoord.chunkPosZ);
                    String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                    if(!simulate) area.setCoord1Variable(var);
                }
                if(area.getCoord2Variable().equals("") && (area.x2 != 0 || area.y2 != 0 || area.z2 != 0)) {
                    ChunkPosition offset = new ChunkPosition(area.x2 - baseCoord.chunkPosX, area.y2 - baseCoord.chunkPosY, area.z2 - baseCoord.chunkPosZ);
                    String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                    if(!simulate) area.setCoord2Variable(var);
                }
            } else if(widget instanceof ProgWidgetCoordinate && baseWidget.getConnectedParameters()[0] != widget) {
                ProgWidgetCoordinate coordinate = (ProgWidgetCoordinate)widget;
                if(!coordinate.isUsingVariable()) {
                    ChunkPosition c = coordinate.getCoordinate();
                    String chunkString = "(" + c.chunkPosX + ", " + c.chunkPosY + ", " + c.chunkPosZ + ")";
                    if(PneumaticCraftUtils.distBetween(c, 0, 0, 0) < 64) { //When the coordinate value is close to 0, there's a low chance it means a position, and rather an offset.
                        if(tooltip != null) tooltip.add(I18n.format("gui.programmer.button.convertToRelative.coordIsNotChangedWarning", chunkString));
                    } else {
                        if(tooltip != null) tooltip.add(I18n.format("gui.programmer.button.convertToRelative.coordIsChangedWarning", chunkString));
                        if(!simulate) {
                            ChunkPosition offset = new ChunkPosition(c.chunkPosX - baseCoord.chunkPosX, c.chunkPosY - baseCoord.chunkPosY, c.chunkPosZ - baseCoord.chunkPosZ);
                            String var = getOffsetVariable(offsetToVariableNames, baseWidget.getVariable(), offset);
                            if(!simulate) {
                                coordinate.setVariable(var);
                                coordinate.setUsingVariable(true);
                            }
                        }
                    }
                }
            }
        }
        if(offsetToVariableNames.size() > 0) {
            ProgWidgetCoordinateOperator firstOperator = null;
            ProgWidgetCoordinateOperator prevOperator = baseWidget;
            int x = baseWidget.getX();
            for(Map.Entry<ChunkPosition, String> entry : offsetToVariableNames.entrySet()) {
                ProgWidgetCoordinateOperator operator = new ProgWidgetCoordinateOperator();
                operator.setVariable(entry.getValue());

                int y = prevOperator.getY() + prevOperator.getHeight() / 2;
                operator.setX(x);
                operator.setY(y);
                if(!isValidPlaced(operator)) return false;

                ProgWidgetCoordinate coordinatePiece1 = new ProgWidgetCoordinate();
                coordinatePiece1.setX(x + prevOperator.getWidth() / 2);
                coordinatePiece1.setY(y);
                coordinatePiece1.setVariable(baseWidget.getVariable());
                coordinatePiece1.setUsingVariable(true);
                if(!isValidPlaced(coordinatePiece1)) return false;

                ProgWidgetCoordinate coordinatePiece2 = new ProgWidgetCoordinate();
                coordinatePiece2.setX(x + prevOperator.getWidth() / 2 + coordinatePiece1.getWidth() / 2);
                coordinatePiece2.setY(y);
                coordinatePiece2.setCoordinate(entry.getKey());
                if(!isValidPlaced(coordinatePiece2)) return false;

                if(!simulate) {
                    te.progWidgets.add(operator);
                    te.progWidgets.add(coordinatePiece1);
                    te.progWidgets.add(coordinatePiece2);
                }
                if(firstOperator == null) firstOperator = operator;
                prevOperator = operator;
            }
            if(!simulate) {
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
                TileEntityProgrammer.updatePuzzleConnections(te.progWidgets);
            }
            return true;
        } else {
            return true; //When there's nothing to place there's always room.
        }
    }

    private String getOffsetVariable(Map<ChunkPosition, String> offsetToVariableNames, String baseVariable, ChunkPosition offset){
        if(offset.equals(new ChunkPosition(0, 0, 0))) return baseVariable;
        String var = offsetToVariableNames.get(offset);
        if(var == null) {
            var = "var" + (offsetToVariableNames.size() + 1);
            offsetToVariableNames.put(offset, var);
        }
        return var;
    }

    @Override
    protected void mouseClicked(int x, int y, int par3){
        ItemStack programmedItem = te.getStackInSlot(TileEntityProgrammer.PROGRAM_SLOT);
        if(nameField.isFocused() && programmedItem != null) {
            programmedItem.setStackDisplayName(nameField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        }
        super.mouseClicked(x, y, par3);

        if(par3 == 1 && showingWidgetProgress == 0) {
            IProgWidget widget = programmerUnit.getHoveredWidget(x, y);
            if(widget != null) {
                GuiScreen screen = widget.getOptionWindow(this);
                if(screen != null) mc.displayGuiScreen(screen);
            }
        }
    }

    @Override
    public void onGuiClosed(){
        te.translatedX = programmerUnit.getTranslatedX();
        te.translatedY = programmerUnit.getTranslatedY();
        te.zoomState = programmerUnit.getLastZoom();
        te.showFlow = showFlow.checked;
        te.showInfo = showInfo.checked;
        super.onGuiClosed();
    }

    @Override
    @Optional.Method(modid = ModIds.NEI)
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility){
        currentVisibility.showNEI = false;
        return currentVisibility;
    }

}
