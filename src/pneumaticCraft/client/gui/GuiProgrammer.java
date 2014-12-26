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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetVerticalScrollbar;
import pneumaticCraft.common.inventory.ContainerProgrammer;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
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

    //  private GuiButton redstoneButton;
    private GuiButtonSpecial importButton;
    private GuiButtonSpecial exportButton;
    private List<GuiCheckBox> filters;
    private GuiCheckBox showInfo, showFlow;

    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<IProgWidget>();

    private boolean wasClicking;
    private IProgWidget draggingWidget;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private static final int FAULT_MARGIN = 4;
    private int widgetPage;
    private int maxPage;
    private WidgetVerticalScrollbar scaleScroll;
    private static final float SCALE_PER_STEP = 0.1F;
    private int translatedX, translatedY;
    private int lastMouseX, lastMouseY;
    private int lastZoom;

    public GuiProgrammer(InventoryPlayer player, TileEntityProgrammer te){

        super(new ContainerProgrammer(player, te), te, Textures.GUI_PROGRAMMER);
        ySize = 256;
        xSize = 350;

        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            widget.setX(322);
        }

        this.player = FMLClientHandler.instance().getClient().thePlayer;
    }

    private void updateVisibleProgWidgets(){
        int y = 0, page = 0;
        maxPage = 0;
        visibleSpawnWidgets.clear();
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            if(filters.get(widget.getCategory().ordinal()).checked) {
                int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
                y += widgetHeight;
                if(y > ySize - 160) {
                    y = 0;
                    page++;
                    maxPage++;
                }
                if(page == widgetPage) visibleSpawnWidgets.add(widget);
            }
        }
        if(widgetPage > maxPage) {
            widgetPage = maxPage;
            updateVisibleProgWidgets();
        }
    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        //    addProgWidgetTabs(xStart, yStart);

        importButton = new GuiButtonSpecial(1, xStart + 301, yStart + 3, 20, 15, "<--");
        importButton.setTooltipText("Import program");
        buttonList.add(importButton);

        exportButton = new GuiButtonSpecial(2, xStart + 301, yStart + 20, 20, 15, "-->");
        buttonList.add(exportButton);

        buttonList.add(new GuiButton(3, xStart + 305, yStart + 174, 10, 10, "-"));
        buttonList.add(new GuiButton(4, xStart + 335, yStart + 174, 10, 10, "+"));

        filters = new ArrayList<GuiCheckBox>();
        for(int i = 0; i < IProgWidget.WidgetCategory.values().length; i++) {
            GuiCheckBox checkBox = new GuiCheckBox(i, xStart + 263, yStart + 190 + i * 12, 0xFF000000, IProgWidget.WidgetCategory.values()[i].getLocalizedName());
            checkBox.checked = te.filters[i];
            addWidget(checkBox);
            filters.add(checkBox);
        }

        buttonList.add(new GuiButton(5, xStart + 5, yStart + 175, 87, 20, I18n.format("gui.programmer.button.showStart")));
        buttonList.add(new GuiButton(6, xStart + 5, yStart + 197, 87, 20, I18n.format("gui.programmer.button.showLatest")));
        addWidget(showInfo = new GuiCheckBox(-1, xStart + 5, yStart + 220, 0xFF000000, "gui.programmer.checkbox.showInfo").setChecked(te.showInfo));
        addWidget(showFlow = new GuiCheckBox(-1, xStart + 5, yStart + 232, 0xFF000000, "gui.programmer.checkbox.showFlow").setChecked(te.showFlow));

        scaleScroll = new WidgetVerticalScrollbar(xStart + 302, yStart + 40, 129).setStates(9).setCurrentState(te.zoomState).setListening(true);
        addWidget(scaleScroll);

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

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);

        boolean igwLoaded = Loader.isModLoaded(ModIds.IGWMOD);
        fontRendererObj.drawString(widgetPage + 1 + "/" + (maxPage + 1), 316, 175, 0xFF000000);
        fontRendererObj.drawString(I18n.format("gui.programmer.filters"), 263, 180, 0xFF000000);

        float scale = 1.0F - scaleScroll.getState() * SCALE_PER_STEP;

        for(IProgWidget widget : te.progWidgets) {
            if(!isOutsideProgrammingArea(widget)) {
                if(widget != draggingWidget && (x - translatedX) / scale - guiLeft >= widget.getX() && (y - translatedY) / scale - guiTop >= widget.getY() && (x - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2 && (y - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2) {
                    List<String> tooltip = new ArrayList<String>();
                    widget.getTooltip(tooltip);
                    if(igwLoaded) tooltip.add(I18n.format("gui.programmer.pressIForInfo"));
                    if(tooltip.size() > 0) drawHoveringString(tooltip, x - guiLeft, y - guiTop, fontRendererObj);
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
    @Optional.Method(modid = ModIds.IGWMOD)
    protected void keyTyped(char key, int keyCode){
        super.keyTyped(key, keyCode);

        if(Keyboard.KEY_I == keyCode) {
            int x = lastMouseX;
            int y = lastMouseY;
            float scale = 1.0F - scaleScroll.getState() * SCALE_PER_STEP;

            for(IProgWidget widget : te.progWidgets) {
                if(!isOutsideProgrammingArea(widget)) {
                    if(widget != draggingWidget && (x - translatedX) / scale - guiLeft >= widget.getX() && (y - translatedY) / scale - guiTop >= widget.getY() && (x - translatedX) / scale - guiLeft <= widget.getX() + widget.getWidth() / 2 && (y - translatedY) / scale - guiTop <= widget.getY() + widget.getHeight() / 2) {
                        GuiWiki gui = new GuiWiki();
                        FMLClientHandler.instance().showGuiScreen(gui);
                        gui.setCurrentFile("progwidget/" + widget.getWidgetString());
                    }
                }
            }

            for(IProgWidget widget : visibleSpawnWidgets) {
                if(widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                    GuiWiki gui = new GuiWiki();
                    FMLClientHandler.instance().showGuiScreen(gui);
                    gui.setCurrentFile("progwidget/" + widget.getWidgetString());
                }
            }
        }
    }

    @Override
    protected boolean shouldDrawBackground(){
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindGuiTexture();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        func_146110_a(xStart, yStart, 0, 0, xSize, ySize, xSize, ySize);

        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        int origX = x;
        int origY = y;
        x -= translatedX;
        y -= translatedY;
        float scale = 1.0F - scaleScroll.getState() * SCALE_PER_STEP;
        x = (int)(x / scale);
        y = (int)(y / scale);

        if(scaleScroll.getState() != lastZoom) {
            float shift = SCALE_PER_STEP * (scaleScroll.getState() - lastZoom);
            if(new Rectangle(guiLeft + 5, guiTop + 5, 294, 166).contains(origX, origY)) {
                translatedX += shift * x;
                translatedY += shift * y;
            } else {
                translatedX += 294 / 2 * shift;
                translatedY += 166 / 2 * shift;
            }
        }
        lastZoom = scaleScroll.getState();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GL11.glScissor((guiLeft + 5) * sr.getScaleFactor(), (sr.getScaledHeight() - 166 - (guiTop + 5)) * sr.getScaleFactor(), 294 * sr.getScaleFactor(), 166 * sr.getScaleFactor());
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
            if(showInfo.checked) widget.renderExtraInfo();
            GL11.glPopMatrix();
        }

        if(showInfo.checked) {
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

        int curY = 40;
        for(int i = 0; i < visibleSpawnWidgets.size(); i++) {
            visibleSpawnWidgets.get(i).setY(curY);
            curY += visibleSpawnWidgets.get(i).getHeight() / 2 + (visibleSpawnWidgets.get(i).hasStepOutput() ? 5 : 0) + 1;
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
        boolean isMiddleClicking = Mouse.isButtonDown(2);

        if(draggingWidget != null) {
            setConnectingWidgetsToXY(draggingWidget, x - dragMouseStartX + dragWidgetStartX - guiLeft, y - dragMouseStartY + dragWidgetStartY - guiTop);
        } else if(isLeftClicking && wasClicking && new Rectangle(guiLeft + 5, guiTop + 5, 294, 166).contains(origX, origY)) {
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
            if(draggingWidget == null) {
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
        } else if(isMiddleClicking && !wasClicking) {
            for(IProgWidget widget : te.progWidgets) {
                if(!isOutsideProgrammingArea(widget)) {
                    if(x >= widget.getX() + guiLeft && y >= widget.getY() + guiTop && x <= widget.getX() + guiLeft + widget.getWidth() / 2 && y <= widget.getY() + guiTop + widget.getHeight() / 2) {
                        draggingWidget = widget.copy();
                        te.progWidgets.add(draggingWidget);
                        dragMouseStartX = 0;
                        dragMouseStartY = 0;
                        dragWidgetStartX = widget.getX() - (x - guiLeft);
                        dragWidgetStartY = widget.getY() - (y - guiTop);
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
        float scale = 1.0F - scaleScroll.getState() * SCALE_PER_STEP;
        int x = (int)((widget.getX() + guiLeft) * scale);
        int y = (int)((widget.getY() + guiTop) * scale);
        x += translatedX - guiLeft;
        y += translatedY - guiTop;

        return x < 5 || x + widget.getWidth() * scale / 2 > xSize - 51 || y < 5 || y + widget.getHeight() * scale / 2 > 171;
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
        }

        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

    @Override
    public void actionPerformed(IGuiWidget button){
        updateVisibleProgWidgets();
    }

    private void gotoPiece(IProgWidget widget){
        scaleScroll.currentScroll = 0;
        lastZoom = 0;
        translatedX = -widget.getX() + 294 / 2;
        translatedY = -widget.getY() + 166 / 2;
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        boolean isDeviceInserted = te.getStackInSlot(TileEntityProgrammer.PROGRAM_SLOT) != null;
        importButton.enabled = isDeviceInserted;
        exportButton.enabled = isDeviceInserted;

        List<String> exportButtonTooltip = new ArrayList<String>();
        exportButtonTooltip.add("Export program");
        if(te.getStackInSlot(TileEntityProgrammer.PROGRAM_SLOT) != null) {
            List<ItemStack> requiredPieces = te.getRequiredPuzzleStacks();
            List<ItemStack> returnedPieces = te.getReturnedPuzzleStacks();
            if(!requiredPieces.isEmpty()) {
                exportButtonTooltip.add("Required Programming Puzzles:");
                if(player.capabilities.isCreativeMode) exportButtonTooltip.add("(Creative mode, so the following is free)");
                for(ItemStack stack : requiredPieces) {
                    List<String> rawList = new ArrayList<String>();
                    stack.getItem().addInformation(stack, null, rawList, false);
                    String prefix;
                    if(te.hasEnoughPuzzleStacks(player, stack)) {
                        prefix = EnumChatFormatting.GREEN.toString();
                    } else {
                        prefix = EnumChatFormatting.RED.toString();
                        exportButton.enabled = player.capabilities.isCreativeMode;
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
        exportButton.setTooltipText(exportButtonTooltip);
    }

    @Override
    protected void mouseClicked(int x, int y, int par3){
        super.mouseClicked(x, y, par3);

        if(par3 == 1) {
            x -= translatedX;
            y -= translatedY;
            float scale = 1.0F - scaleScroll.getState() * SCALE_PER_STEP;
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
        for(int i = 0; i < filters.size(); i++) {
            te.filters[i] = filters.get(i).checked;
        }
        super.onGuiClosed();
    }

    @Override
    @Optional.Method(modid = ModIds.NEI)
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility){
        currentVisibility.showNEI = false;
        return currentVisibility;
    }

}
