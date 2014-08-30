package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.inventory.ContainerProgrammer;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiProgrammer extends GuiPneumaticContainerBase{
    public final TileEntityProgrammer te;
    private final EntityPlayer player;

    //    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;

    //  private GuiButton redstoneButton;
    private GuiButtonSpecial importButton;
    private GuiButtonSpecial exportButton;

    private final List<IProgWidget> visibleSpawnWidgets = new ArrayList<IProgWidget>();

    private boolean wasClicking;
    private IProgWidget draggingWidget;
    private int dragMouseStartX, dragMouseStartY;
    private int dragWidgetStartX, dragWidgetStartY;
    private static final int FAULT_MARGIN = 4;
    private int widgetPage;
    private int maxPage;

    public GuiProgrammer(InventoryPlayer player, TileEntityProgrammer te){

        super(new ContainerProgrammer(player, te));
        ySize = 256;
        this.te = te;

        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            widget.setX(132);
        }

        maxPage = 0;
        int y = 0;
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
            y += widgetHeight;
            if(y > ySize - 150) {
                y = 0;
                maxPage++;
            }
        }

        this.player = FMLClientHandler.instance().getClient().thePlayer;
    }

    private void updateVisibleProgWidgets(){
        int y = 0, page = 0;
        visibleSpawnWidgets.clear();
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            int widgetHeight = widget.getHeight() / 2 + (widget.hasStepOutput() ? 5 : 0) + 1;
            y += widgetHeight;
            if(y > ySize - 150) {
                y = 0;
                page++;
            }
            if(page == widgetPage) visibleSpawnWidgets.add(widget);
        }
    }

    /**
     * Causes the screen to lay out its subcomponents again. This is the equivalent of the Java call
     * Container.validate()
     */
    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3){
        super.setWorldAndResolution(par1Minecraft, par2, par3);
    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, yStart + 5, 0xFF8888FF, null, true);
        addProgWidgetTabs(xStart, yStart);

        animatedStatList.add(infoStat);
        infoStat.setText(GuiConstants.INFO_PROGRAMMER);

        //    Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 118, yStart + 30, 117, 20);
        //    redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        //    buttonList.add(redstoneButton);

        importButton = new GuiButtonSpecial(1, xStart + 127, yStart + 3, 20, 15, "<--");
        importButton.setTooltipText("Import program");
        buttonList.add(importButton);

        exportButton = new GuiButtonSpecial(2, xStart + 127, yStart + 20, 20, 15, "-->");
        buttonList.add(exportButton);

        buttonList.add(new GuiButton(3, xStart + 131, yStart + 159, 10, 10, "-"));
        buttonList.add(new GuiButton(4, xStart + 161, yStart + 159, 10, 10, "+"));

        updateVisibleProgWidgets();
    }

    private void addProgWidgetTabs(int xStart, int yStart){
        GuiAnimatedStat stat = infoStat;
        List<IProgWidget> registeredWidgets = TileEntityProgrammer.registeredWidgets;
        for(int i = 0; i < registeredWidgets.size() / 2; i++) {
            IProgWidget widget = registeredWidgets.get(i);
            stat = new GuiAnimatedStat(this, I18n.format("programmingPuzzle." + widget.getWidgetString() + ".name"), ItemProgrammingPuzzle.getStackForWidgetKey(widget.getWidgetString()), xStart, 3, widget.getGuiTabColor(), stat, true);
            stat.setText(widget.getGuiTabText());
            animatedStatList.add(stat);
        }
        stat = null;
        for(int i = registeredWidgets.size() / 2; i < registeredWidgets.size(); i++) {
            IProgWidget widget = registeredWidgets.get(i);
            stat = new GuiAnimatedStat(this, I18n.format("programmingPuzzle." + widget.getWidgetString() + ".name"), ItemProgrammingPuzzle.getStackForWidgetKey(widget.getWidgetString()), xStart + xSize, stat == null ? yStart + 5 : 3, widget.getGuiTabColor(), stat, false);
            stat.setText(widget.getGuiTabText());
            animatedStatList.add(stat);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);
        fontRendererObj.drawString(widgetPage + 1 + "/" + (maxPage + 1), 142, 160, 0xFF000000);

        /*    switch(te.redstoneMode){
                case 0:
                    redstoneButton.displayString = "Never";
                    break;
                case 1:
                    redstoneButton.displayString = "Redstone applied";
                    break;
                case 2:
                    redstoneButton.displayString = "Redstone not applied";
                    break;
            }*/

        for(IProgWidget widget : te.progWidgets) {
            if(widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                List<String> tooltip = new ArrayList<String>();
                widget.getTooltip(tooltip);
                if(tooltip.size() > 0) drawHoveringString(tooltip, x - guiLeft, y - guiTop, fontRendererObj);
            }
        }
        for(IProgWidget widget : visibleSpawnWidgets) {
            if(widget != draggingWidget && x - guiLeft >= widget.getX() && y - guiTop >= widget.getY() && x - guiLeft <= widget.getX() + widget.getWidth() / 2 && y - guiTop <= widget.getY() + widget.getHeight() / 2) {
                List<String> tooltip = new ArrayList<String>();
                widget.getTooltip(tooltip);
                if(tooltip.size() > 0) drawHoveringString(tooltip, x - guiLeft, y - guiTop, fontRendererObj);
            }
        }

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(Textures.GUI_PROGRAMMER);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        for(IProgWidget widget : te.progWidgets) {
            GL11.glPushMatrix();
            GL11.glTranslated(widget.getX() + guiLeft, widget.getY() + guiTop, 0);
            GL11.glScaled(0.5, 0.5, 1);
            widget.render();
            GL11.glPopMatrix();
        }

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
        /*  for(int i = 0; i < statIconWidgets.length; i++) {
              IProgWidget widget = TileEntityProgrammer.registeredWidgets.get(i);
              GL11.glPushMatrix();
              GL11.glTranslated(spawnWidgetStats[i].getBaseX() + (spawnWidgetStats[i].isLeftSided() ? -16 : 1), spawnWidgetStats[i].getAffectedY() + 1, 0);
              double scale = 10D / widget.getHeight();
              GL11.glScaled(scale, scale, 1);
              widget.render();
              GL11.glPopMatrix();
          }*/
        if(draggingWidget != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(draggingWidget.getX() + guiLeft, draggingWidget.getY() + guiTop, 0);
            GL11.glScaled(0.5, 0.5, 1);
            draggingWidget.render();
            GL11.glPopMatrix();
        }

        //     redstoneButton.drawButton = redstoneBehaviourStat.isDoneExpanding();

        if(draggingWidget != null) {
            setConnectingWidgetsToXY(draggingWidget, x - dragMouseStartX + dragWidgetStartX - guiLeft, y - dragMouseStartY + dragWidgetStartY - guiTop);
        }

        boolean isLeftClicking = Mouse.isButtonDown(0);
        boolean isMiddleClicking = Mouse.isButtonDown(2);
        if(isLeftClicking && !wasClicking) {
            for(IProgWidget widget : visibleSpawnWidgets) {
                if(x >= widget.getX() + guiLeft && y >= widget.getY() + guiTop && x <= widget.getX() + guiLeft + widget.getWidth() / 2 && y <= widget.getY() + guiTop + widget.getHeight() / 2) {
                    draggingWidget = widget.copy();
                    te.progWidgets.add(draggingWidget);
                    dragMouseStartX = x - guiLeft;
                    dragMouseStartY = y - guiTop;
                    dragWidgetStartX = widget.getX();
                    dragWidgetStartY = widget.getY();
                    break;
                }
            }
            if(draggingWidget == null) {
                for(IProgWidget widget : te.progWidgets) {
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
        } else if(isMiddleClicking && !wasClicking) {
            for(IProgWidget widget : te.progWidgets) {
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

        if(!isLeftClicking && !isMiddleClicking && draggingWidget != null) {
            if(needsDeletion()) {
                deleteConnectingWidgets(draggingWidget);
            } else {
                handlePuzzleMargins();
                if(!isValidPlaced(draggingWidget)) {
                    setConnectingWidgetsToXY(draggingWidget, dragWidgetStartX, dragWidgetStartY);
                    if(needsDeletion()) deleteConnectingWidgets(draggingWidget);
                }
            }
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(te));
            TileEntityProgrammer.updatePuzzleConnections(te.progWidgets);

            draggingWidget = null;
        }
        wasClicking = isLeftClicking || isMiddleClicking;

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

    @Override
    public void drawScreen(int par1, int par2, float par3){
        super.drawScreen(par1, par2, par3);
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
                            if(parameters[i] == returnValue && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
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
                            if(parameters[i] == widget.returnType() && Math.abs(draggingWidget.getY() + i * 11 - widget.getY()) <= FAULT_MARGIN) {
                                setConnectingWidgetsToXY(draggingWidget, widget.getX() - draggingWidget.getWidth() / 2 - (outerPiece.getX() - draggingWidget.getX()), widget.getY() - i * 11);
                            }
                        }
                    } else {
                        Class<? extends IProgWidget>[] checkingPieceParms = widget.getParameters();
                        if(checkingPieceParms != null) {
                            for(int i = 0; i < checkingPieceParms.length; i++) {
                                if(checkingPieceParms[i] == parameters[0] && Math.abs(widget.getY() + i * 11 - draggingWidget.getY()) <= FAULT_MARGIN) {
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

    private boolean needsDeletion(){
        return draggingWidget.getX() < 0 || draggingWidget.getX() + draggingWidget.getWidth() / 2 > xSize - 51 || draggingWidget.getY() < 0 || draggingWidget.getY() + draggingWidget.getHeight() / 2 > ySize;
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

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Stop item transfer when"); // the spaces are there
                                                        // to create space for
                                                        // the button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
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
        }

        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

    @Override
    public void updateScreen(){
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
        super.updateScreen();
    }

    @Override
    protected void mouseClicked(int x, int y, int par3){
        super.mouseClicked(x, y, par3);

        if(par3 == 1) {
            for(IProgWidget widget : visibleSpawnWidgets) {
                if(x >= widget.getX() + guiLeft && y >= widget.getY() + guiTop && x <= widget.getX() + guiLeft + widget.getWidth() / 2 && y <= widget.getY() + guiTop + widget.getHeight() / 2) {
                    GuiScreen screen = widget.getOptionWindow(this);
                    if(screen != null) mc.displayGuiScreen(screen);
                }
            }
            for(IProgWidget widget : te.progWidgets) {
                if(x >= widget.getX() + guiLeft && y >= widget.getY() + guiTop && x <= widget.getX() + guiLeft + widget.getWidth() / 2 && y <= widget.getY() + guiTop + widget.getHeight() / 2) {
                    GuiScreen screen = widget.getOptionWindow(this);
                    if(screen != null) mc.displayGuiScreen(screen);
                }
            }
        }
    }

}
