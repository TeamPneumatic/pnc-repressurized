package pneumaticCraft.client.gui;

import igwmod.gui.GuiWiki;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.client.gui.widget.WidgetVerticalScrollbar;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.inventory.ContainerProgrammer;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.progwidgets.IJump;
import pneumaticCraft.common.progwidgets.ILabel;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
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

    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<IProgWidget>();

    private boolean wasClicking;
    private boolean wasFocused;
    private IProgWidget draggingWidget;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private static final int FAULT_MARGIN = 4;
    private int widgetPage;
    private int maxPage;
    private WidgetVerticalScrollbar scaleScroll;
    private static final float SCALE_PER_STEP = 0.2F;
    private int translatedX, translatedY;
    private int lastMouseX, lastMouseY;
    private int lastZoom;
    private boolean showingAllWidgets;
    private int showingWidgetProgress;
    private int oldShowingWidgetProgress;
    private static final int PROGRAMMING_START_Y = 17;

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
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
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
        //pastebinButton.setRenderStacks(new ItemStack(Itemss.advancedPCB));
        pastebinButton.setRenderedIcon(Textures.GUI_PASTEBIN_ICON_LOCATION);
        buttonList.add(pastebinButton);

        scaleScroll = new WidgetVerticalScrollbar(xStart + 302, yStart + 40, 129).setStates(9).setCurrentState(te.zoomState).setListening(true);
        addWidget(scaleScroll);

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName() + ".name");
        addLabel(containerName, guiLeft + 7, guiTop + 5);

        nameField = new WidgetTextField(fontRendererObj, guiLeft + 200, guiTop + 5, 98, fontRendererObj.FONT_HEIGHT);
        addWidget(nameField);

        String name = I18n.format("gui.programmer.name");
        addLabel(name, guiLeft + 197 - fontRendererObj.getStringWidth(name), guiTop + 5);

        lastZoom = te.zoomState;
        translatedX = te.translatedX;
        translatedY = te.translatedY;

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

    private float getScale(){
        return 2.0F - scaleScroll.getState() * SCALE_PER_STEP;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);

        boolean igwLoaded = Loader.isModLoaded(ModIds.IGWMOD);
        fontRendererObj.drawString(widgetPage + 1 + "/" + (maxPage + 1), 305, 175, 0xFF000000);
        fontRendererObj.drawString(I18n.format("gui.programmer.difficulty"), 263, 190, 0xFF000000);

        float scale = getScale();

        if(showingWidgetProgress == 0) {
            for(IProgWidget widget : te.progWidgets) {
                if(!isOutsideProgrammingArea(widget)) {
                    if(widget != draggingWidget && (x - translatedX) / scale - guiLeft >= widget.getX() && (y - translatedY) / scale - guiTop >= widget.getY() && (x - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2 && (y - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2) {
                        List<String> tooltip = new ArrayList<String>();
                        widget.getTooltip(tooltip);

                        List<String> errors = new ArrayList<String>();
                        widget.addErrors(errors);
                        if(errors.size() > 0) {
                            tooltip.add(EnumChatFormatting.RED + I18n.format("gui.programmer.errors"));
                            for(String s : errors) {
                                String[] lines = WordUtils.wrap("-" + I18n.format(s), 30).split(System.getProperty("line.separator"));
                                for(String line : lines) {
                                    tooltip.add(EnumChatFormatting.RED + "   " + line);
                                }
                            }
                        }

                        List<String> warnings = new ArrayList<String>();
                        widget.addWarnings(warnings);
                        if(warnings.size() > 0) {
                            tooltip.add(EnumChatFormatting.YELLOW + I18n.format("gui.programmer.warnings"));
                            for(String s : warnings) {
                                String[] lines = WordUtils.wrap("-" + I18n.format(s), 30).split(System.getProperty("line.separator"));
                                for(String line : lines) {
                                    tooltip.add(EnumChatFormatting.YELLOW + "   " + line);
                                }
                            }
                        }

                        if(igwLoaded) tooltip.add(I18n.format("gui.programmer.pressIForInfo"));
                        if(tooltip.size() > 0) drawHoveringString(tooltip, x - guiLeft, y - guiTop, fontRendererObj);
                    }
                }
            }
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
    }

    @Optional.Method(modid = ModIds.IGWMOD)
    private void onIGWAction(){
        int x = lastMouseX;
        int y = lastMouseY;
        float scale = getScale();

        for(IProgWidget widget : te.progWidgets) {
            if(!isOutsideProgrammingArea(widget)) {
                if(widget != draggingWidget && (x - translatedX) / scale - guiLeft >= widget.getX() && (y - translatedY) / scale - guiTop >= widget.getY() && (x - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2 && (y - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2) {
                    GuiWiki gui = new GuiWiki();
                    FMLClientHandler.instance().showGuiScreen(gui);
                    gui.setCurrentFile("pneumaticcraft:progwidget/" + widget.getWidgetString());
                }
            }
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

        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int origX = x;
        int origY = y;
        x -= translatedX;
        y -= translatedY;
        float scale = getScale();
        x = (int)(x / scale);
        y = (int)(y / scale);

        if(scaleScroll.getState() != lastZoom) {
            float shift = SCALE_PER_STEP * (scaleScroll.getState() - lastZoom);
            if(new Rectangle(guiLeft + 5, guiTop + PROGRAMMING_START_Y, 294, 171 - PROGRAMMING_START_Y).contains(origX, origY)) {
                translatedX += shift * x;
                translatedY += shift * y;
            } else {
                translatedX += 294 / 2 * shift;
                translatedY += 166 / 2 * shift;
            }
        }
        lastZoom = scaleScroll.getState();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GL11.glScissor((guiLeft + 5) * sr.getScaleFactor(), (sr.getScaledHeight() - (171 - PROGRAMMING_START_Y) - (guiTop + PROGRAMMING_START_Y)) * sr.getScaleFactor(), 294 * sr.getScaleFactor(), (171 - PROGRAMMING_START_Y) * sr.getScaleFactor());
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GL11.glPushMatrix();

        GL11.glTranslated(translatedX, translatedY, 0);
        GL11.glScaled(scale, scale, 1);

        if(showFlow.checked) showFlow();

        for(IProgWidget widget : te.progWidgets) {
            GL11.glPushMatrix();
            GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GL11.glScaled(0.5, 0.5, 1);
            widget.render();
            GL11.glPopMatrix();
        }

        for(IProgWidget widget : te.progWidgets) {
            List<String> errors = new ArrayList<String>();
            widget.addErrors(errors);
            if(errors.size() > 0) {
                drawBorder(widget, 0xFFFF0000);
            } else {
                List<String> warnings = new ArrayList<String>();
                widget.addWarnings(warnings);
                if(warnings.size() > 0) {
                    drawBorder(widget, 0xFFFFFF00);
                }
            }
        }
        GL11.glColor4d(1, 1, 1, 1);

        if(showInfo.checked && showingWidgetProgress == 0) {
            for(IProgWidget widget : te.progWidgets) {
                GL11.glPushMatrix();
                GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
                GL11.glScaled(0.5, 0.5, 1);
                widget.renderExtraInfo();
                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

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
        for(IProgWidget widget : visibleSpawnWidgets) {
            GL11.glPushMatrix();
            GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GL11.glScaled(0.5, 0.5, 1);
            widget.render();
            GL11.glPopMatrix();
        }

        GL11.glPushMatrix();
        GL11.glTranslated(translatedX, translatedY, 0);
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
        } else if(isLeftClicking && wasClicking && new Rectangle(guiLeft + 5, guiTop + PROGRAMMING_START_Y, 294, 171 - PROGRAMMING_START_Y).contains(origX, origY)) {
            translatedX += origX - lastMouseX;
            translatedY += origY - lastMouseY;
        }

        if(isLeftClicking && !wasClicking) {
            for(IProgWidget widget : visibleSpawnWidgets) {
                if(origX >= widget.getX() + guiLeft && origY >= widget.getY() + guiTop && origX <= widget.getX() + guiLeft + widget.getWidth() / 2 && origY <= widget.getY() + guiTop + widget.getHeight() / 2) {
                    draggingWidget = widget.copy();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = x - (int)(guiLeft / scale);
                    dragMouseStartY = y - (int)(guiTop / scale);
                    dragWidgetStartX = (int)((widget.getX() - translatedX) / scale);
                    dragWidgetStartY = (int)((widget.getY() - translatedY) / scale);
                    break;
                }
            }
            if(draggingWidget == null && showingWidgetProgress == 0) {
                for(IProgWidget widget : te.progWidgets) {
                    if(!isOutsideProgrammingArea(widget)) {
                        if(x >= widget.getX() + guiLeft && y >= widget.getY() + guiTop && x <= widget.getX() + guiLeft + widget.getWidth() / 2 && y <= widget.getY() + guiTop + widget.getHeight() / 2) {
                            draggingWidget = widget;
                            dragMouseStartX = x - guiLeft;
                            dragMouseStartY = y - guiTop;
                            dragWidgetStartX = widget.getX();
                            dragWidgetStartY = widget.getY();
                            break;
                        }
                    }
                }
            }
        } else if(isMiddleClicking && !wasClicking && showingWidgetProgress == 0) {
            for(IProgWidget widget : te.progWidgets) {
                if(!isOutsideProgrammingArea(widget)) {
                    if(x >= widget.getX() + guiLeft && y >= widget.getY() + guiTop && x <= widget.getX() + guiLeft + widget.getWidth() / 2 && y <= widget.getY() + guiTop + widget.getHeight() / 2) {
                        draggingWidget = widget.copy();
                        te.progWidgets.add(draggingWidget);
                        dragMouseStartX = 0;
                        dragMouseStartY = 0;
                        dragWidgetStartX = widget.getX() - (x - guiLeft);
                        dragWidgetStartY = widget.getY() - (y - guiTop);
                        if(PneumaticCraft.proxy.isSneakingInGui()) copyAndConnectConnectingWidgets(widget, draggingWidget);
                        break;
                    }
                }
            }
        }

        if(!isLeftClicking && !isMiddleClicking && draggingWidget != null) {
            if(isOutsideProgrammingArea(draggingWidget)) {
                deleteConnectingWidgets(draggingWidget);
            } else {
                handlePuzzleMargins();
                if(!isValidPlaced(draggingWidget)) {
                    setConnectingWidgetsToXY(draggingWidget, dragWidgetStartX, dragWidgetStartY);
                    if(isOutsideProgrammingArea(draggingWidget)) deleteConnectingWidgets(draggingWidget);
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

    private void drawBorder(IProgWidget widget, int color){
        GL11.glPushMatrix();
        GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
        GL11.glScaled(0.5, 0.5, 1);
        drawVerticalLine(0, 0, widget.getHeight(), color);
        drawVerticalLine(widget.getWidth(), 0, widget.getHeight(), color);
        drawHorizontalLine(widget.getWidth(), 0, 0, color);
        drawHorizontalLine(widget.getWidth(), 0, widget.getHeight(), color);
        GL11.glPopMatrix();
    }

    private void showFlow(){
        GL11.glLineWidth(1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_LINES);

        for(IProgWidget widget : te.progWidgets) {
            if(widget instanceof IJump) {
                List<String> jumpLocations = ((IJump)widget).getPossibleJumpLocations();
                if(jumpLocations != null) {
                    for(String jumpLocation : jumpLocations) {
                        if(jumpLocation != null) {
                            for(IProgWidget w : te.progWidgets) {
                                if(w instanceof ILabel) {
                                    String label = ((ILabel)w).getLabel();
                                    if(label != null && jumpLocation.equals(label)) {
                                        GL11.glVertex3d(guiLeft + widget.getX() + widget.getWidth() / 4, guiTop + widget.getY() + widget.getHeight() / 4, zLevel);
                                        GL11.glVertex3d(guiLeft + w.getX() + w.getWidth() / 4, guiTop + w.getY() + w.getHeight() / 4, zLevel);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
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

    private boolean isOutsideProgrammingArea(IProgWidget widget){
        float scale = getScale();
        int x = (int)((widget.getX() + guiLeft) * scale);
        int y = (int)((widget.getY() + guiTop) * scale);
        x += translatedX - guiLeft;
        y += translatedY - guiTop;

        return x < 5 || x + widget.getWidth() * scale / 2 > xSize - 51 || y < PROGRAMMING_START_Y || y + widget.getHeight() * scale / 2 > 171;
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
                        gotoPiece(widget);
                        break;
                    }
                }
                return;
            case 6:
                if(te.progWidgets.size() > 0) {
                    gotoPiece(te.progWidgets.get(te.progWidgets.size() - 1));
                }
                return;
            case 7:
                NBTTagCompound mainTag = new NBTTagCompound();
                te.writeProgWidgetsToNBT(mainTag);
                FMLClientHandler.instance().showGuiScreen(pastebinGui = new GuiPastebin(this, mainTag));
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

    private void gotoPiece(IProgWidget widget){
        scaleScroll.currentScroll = 0;
        lastZoom = 0;
        translatedX = -widget.getX() * 2 + 294 / 2 - guiLeft;
        translatedY = -widget.getY() * 2 + 166 / 2 - guiTop;
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

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
            w.addErrors(errors);
            w.addWarnings(warnings);
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
                    List<String> rawList = new ArrayList<String>();
                    rawList.add(stack.getDisplayName());
                    String prefix;
                    if(te.hasEnoughPuzzleStacks(player, stack)) {
                        prefix = EnumChatFormatting.GREEN.toString();
                    } else {
                        prefix = EnumChatFormatting.RED.toString();
                        exportButton.enabled = player.capabilities.isCreativeMode && errors.size() == 0;
                    }
                    exportButtonTooltip.add(prefix + "-" + stack.stackSize + "x " + rawList.get(0));
                }
            }
            if(!returnedPieces.isEmpty()) {
                exportButtonTooltip.add("Returned Programming Puzzles:");
                if(player.capabilities.isCreativeMode) exportButtonTooltip.add("(Creative mode, nothing's given)");
                for(ItemStack stack : returnedPieces) {
                    List<String> rawList = new ArrayList<String>();
                    stack.getItem().addInformation(stack, null, rawList, false);
                    exportButtonTooltip.add("-" + stack.stackSize + "x " + rawList.get(0));
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

    @Override
    protected void mouseClicked(int x, int y, int par3){
        ItemStack programmedItem = te.getStackInSlot(TileEntityProgrammer.PROGRAM_SLOT);
        if(nameField.isFocused() && programmedItem != null) {
            programmedItem.setStackDisplayName(nameField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        }
        super.mouseClicked(x, y, par3);

        if(par3 == 1 && showingWidgetProgress == 0) {
            x -= translatedX;
            y -= translatedY;
            float scale = getScale();
            x = (int)(x / scale);
            y = (int)(y / scale);

            for(IProgWidget widget : te.progWidgets) {
                if(!isOutsideProgrammingArea(widget)) {
                    if(x >= widget.getX() + guiLeft && y >= widget.getY() + guiTop && x <= widget.getX() + guiLeft + widget.getWidth() / 2 && y <= widget.getY() + guiTop + widget.getHeight() / 2) {
                        GuiScreen screen = widget.getOptionWindow(this);
                        if(screen != null) mc.displayGuiScreen(screen);
                    }
                }
            }
        }
    }

    @Override
    public void onGuiClosed(){
        te.translatedX = translatedX;
        te.translatedY = translatedY;
        te.zoomState = lastZoom;
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
