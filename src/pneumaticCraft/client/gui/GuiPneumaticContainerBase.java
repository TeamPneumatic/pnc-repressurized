package pneumaticCraft.client.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.IWidgetListener;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.IMinWorkingPressure;
import pneumaticCraft.common.tileentity.IRedstoneControl;
import pneumaticCraft.common.tileentity.IRedstoneControlled;
import pneumaticCraft.common.tileentity.TileEntityBase;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Textures;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = ModIds.NEI)
public class GuiPneumaticContainerBase<Tile extends TileEntityBase> extends GuiContainer implements INEIGuiHandler,
        IWidgetListener{

    public final Tile te;
    private final ResourceLocation guiTexture;
    /**
     * Any GuiAnimatedStat added to this list will be tracked for mouseclicks, tooltip renders, rendering,updating (resolution and expansion).
     */
    protected final List<IGuiWidget> widgets = new ArrayList<IGuiWidget>();
    private IGuiAnimatedStat lastLeftStat, lastRightStat;

    private GuiAnimatedStat pressureStat;
    protected GuiAnimatedStat problemTab;
    private GuiAnimatedStat redstoneTab;
    protected GuiButtonSpecial redstoneButton;

    public GuiPneumaticContainerBase(Container par1Container, Tile te, String guiTexture){
        super(par1Container);
        this.te = te;
        this.guiTexture = guiTexture != null ? new ResourceLocation(guiTexture) : null;
    }

    protected GuiAnimatedStat addAnimatedStat(String title, ItemStack icon, int color, boolean leftSided){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        GuiAnimatedStat stat = new GuiAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        addWidget(stat);
        if(leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
        return stat;
    }

    protected GuiAnimatedStat addAnimatedStat(String title, String icon, int color, boolean leftSided){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        GuiAnimatedStat stat = new GuiAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        addWidget(stat);
        if(leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
        return stat;
    }

    protected void addWidget(IGuiWidget widget){
        widgets.add(widget);
        widget.setListener(this);
    }

    protected void addWidgets(Iterable<IGuiWidget> widgets){
        for(IGuiWidget widget : widgets) {
            addWidget(widget);
        }
    }

    protected void addLabel(String text, int x, int y){
        addWidget(new WidgetLabel(x, y, text));
    }

    protected void removeWidget(IGuiWidget widget){
        widgets.remove(widget);
    }

    @Override
    public void initGui(){
        super.initGui();
        lastLeftStat = lastRightStat = null;
        if(shouldAddPressureTab() && te instanceof TileEntityPneumaticBase) {
            pressureStat = this.addAnimatedStat("gui.tab.pressure", new ItemStack(Blockss.pressureTube), 0xFF00AA00, false);
        }
        if(shouldAddProblemTab()) {
            problemTab = addAnimatedStat("gui.tab.problems", Textures.GUI_PROBLEMS_TEXTURE, 0xFFFF0000, false);
        }
        if(shouldAddRedstoneTab() && te instanceof IRedstoneControl) {
            redstoneTab = addAnimatedStat("gui.tab.redstoneBehaviour", new ItemStack(Items.redstone), 0xFFCC0000, true);
            List<String> curInfo = new ArrayList<String>();
            curInfo.add(I18n.format(getRedstoneString()));
            for(int i = 0; i < 3; i++)
                curInfo.add("                                      ");// create some space for the button
            redstoneTab.setTextWithoutCuttingString(curInfo);
            Rectangle buttonRect = redstoneTab.getButtonScaledRectangle(-170, 24, 170, 20);
            redstoneButton = new GuiButtonSpecial(0, buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height, "-");//getButtonFromRectangle(0, buttonRect, "-");
            redstoneTab.addWidget(redstoneButton);
        }
        if(te instanceof IInventory) {
            if(shouldAddInfoTab()) {
                String info = "gui.tab.info." + ((IInventory)te).getInventoryName();
                String translatedInfo = I18n.format(info);
                if(!translatedInfo.equals(info)) {
                    addInfoTab(translatedInfo);
                }
            }
            if(te instanceof IHeatExchanger) {
                addAnimatedStat("gui.tab.info.heat.title", new ItemStack(Blocks.fire), 0xFFFF5500, false).setText("gui.tab.info.heat");
            }
            if(shouldAddUpgradeTab()) {
                String upgrades = "gui.tab.upgrades." + ((IInventory)te).getInventoryName();
                String translatedUpgrades = I18n.format(upgrades);
                List<String> upgradeText = new ArrayList<String>();
                if(te instanceof TileEntityPneumaticBase) {
                    upgradeText.add("gui.tab.upgrades.volume");
                    upgradeText.add("gui.tab.upgrades.security");
                }
                if(te instanceof IHeatExchanger) {
                    upgradeText.add("gui.tab.upgrades.volumeCapacity");
                }
                if(!translatedUpgrades.equals(upgrades)) upgradeText.add(upgrades);

                if(upgradeText.size() > 0) addAnimatedStat("gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText(upgradeText);
            }
        }
    }

    protected void addInfoTab(String info){
        if(!Loader.isModLoaded(ModIds.IGWMOD)) info += " \\n \\n" + I18n.format("gui.tab.info.assistIGW");
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText(info);
    }

    protected boolean shouldAddRedstoneTab(){
        return true;
    }

    protected boolean shouldAddPressureTab(){
        return true;
    }

    protected boolean shouldAddUpgradeTab(){
        return true;
    }

    protected boolean shouldAddInfoTab(){
        return true;
    }

    protected boolean shouldAddProblemTab(){
        return true;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j){
        if(shouldDrawBackground()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            int xStart = (width - xSize) / 2;
            int yStart = (height - ySize) / 2;
            drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
        }

        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        for(IGuiWidget widget : widgets) {
            widget.render(i, j, partialTicks);
        }
        for(IGuiWidget widget : widgets) {
            widget.postRender(i, j, partialTicks);
        }

        if(pressureStat != null) {
            TileEntityPneumaticBase pneu = (TileEntityPneumaticBase)te;
            Point gaugeLocation = getGaugeLocation();
            if(gaugeLocation != null) GuiUtils.drawPressureGauge(fontRendererObj, -1, pneu.CRITICAL_PRESSURE, pneu.DANGER_PRESSURE, te instanceof IMinWorkingPressure ? ((IMinWorkingPressure)te).getMinWorkingPressure() : -1, pneu.getPressure(ForgeDirection.UNKNOWN), gaugeLocation.x, gaugeLocation.y, zLevel);
        }
    }

    protected boolean shouldDrawBackground(){
        return true;
    }

    protected void bindGuiTexture(){
        if(guiTexture != null) {
            mc.getTextureManager().bindTexture(guiTexture);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    protected Point getGaugeLocation(){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        if(getInvNameOffset() != null && te instanceof IInventory) {
            IInventory inv = (IInventory)te;
            String containerName = inv.hasCustomInventoryName() ? inv.getInventoryName() : StatCollector.translateToLocal(inv.getInventoryName() + ".name");
            fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2 + getInvNameOffset().x, 6 + getInvNameOffset().y, 4210752);
        }
        if(getInvTextOffset() != null) fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8 + getInvTextOffset().x, ySize - 94 + getInvTextOffset().y, 4210752);
    }

    protected Point getInvNameOffset(){
        return new Point(0, 0);
    }

    protected Point getInvTextOffset(){
        return new Point(0, 0);
    }

    @Override
    public void drawScreen(int x, int y, float partialTick){
        super.drawScreen(x, y, partialTick);

        List<String> tooltip = new ArrayList<String>();
        for(Object obj : buttonList) {
            if(obj instanceof GuiButtonSpecial) {
                GuiButtonSpecial button = (GuiButtonSpecial)obj;
                if(button.xPosition < x && button.xPosition + button.getWidth() > x && button.yPosition < y && button.yPosition + button.getHeight() > y) {
                    button.getTooltip(tooltip);
                }
            }
        }

        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        for(IGuiWidget widget : widgets) {
            if(widget.getBounds().contains(x, y)) widget.addTooltip(x, y, tooltip, PneumaticCraft.proxy.isSneakingInGui());
        }

        if(tooltip.size() > 0) {
            drawHoveringString(tooltip, x, y, fontRendererObj);
            tooltip.clear();
        }

        /* TODO boolean shift = PneumaticCraft.proxy.isSneakingInGui();
         for(IGuiWidget widget : widgets) {
             if(widget.getBounds().contains(x, y)) widget.addTooltip(tooltip, shift);
         }
         if(!tooltip.isEmpty()) {
             List<String> localizedTooltip = new ArrayList<String>();
             for(String line : tooltip) {
                 String localizedLine = I18n.format(line);
                 String[] lines = WordUtils.wrap(localizedLine, 50).split(System.getProperty("line.separator"));
                 for(String locLine : lines) {
                     localizedTooltip.add(locLine);
                 }
             }
             drawHoveringText(localizedTooltip, x, y, fontRendererObj);
         }*/
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        for(IGuiWidget widget : widgets)
            widget.update();

        if(pressureStat != null) {
            List<String> curInfo = new ArrayList<String>();
            addPressureStatInfo(curInfo);
            pressureStat.setText(curInfo);
        }
        if(problemTab != null) {
            List<String> curInfo = new ArrayList<String>();
            addProblems(curInfo);
            if(curInfo.size() == 0) {
                curInfo.add("gui.tab.problems.noProblems");
            }
            problemTab.setText(curInfo);
        }
        if(redstoneTab != null) {
            redstoneButton.displayString = I18n.format(getRedstoneButtonText(((IRedstoneControl)te).getRedstoneMode()));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button){
        sendPacketToServer(button.id);
    }

    public String getRedstoneButtonText(int mode){
        switch(mode){
            case 0:
                return "gui.tab.redstoneBehaviour.button.anySignal";
            case 1:
                return "gui.tab.redstoneBehaviour.button.highSignal";
            case 2:
                return "gui.tab.redstoneBehaviour.button.lowSignal";
        }
        return "<ERROR>";
    }

    public String getRedstoneString(){
        return te instanceof IRedstoneControlled ? "gui.tab.redstoneBehaviour.enableOn" : "gui.tab.redstoneBehaviour.emitRedstoneWhen";
    }

    protected void addPressureStatInfo(List<String> pressureStatText){
        TileEntityPneumaticBase pneumaticTile = (TileEntityPneumaticBase)te;
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(pneumaticTile.getPressure(ForgeDirection.UNKNOWN), 1) + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(pneumaticTile.currentAir + pneumaticTile.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(pneumaticTile.DEFAULT_VOLUME) + " mL.");
        float volumeLeft = pneumaticTile.volume - pneumaticTile.DEFAULT_VOLUME;
        if(volumeLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(volumeLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(pneumaticTile.volume) + " mL.");
        }
    }

    protected void addProblems(List<String> curInfo){
        if(te instanceof IRedstoneControlled && !te.redstoneAllows()) {
            IRedstoneControlled redstoneControlled = (IRedstoneControlled)te;
            curInfo.add("gui.tab.problems.redstoneDisallows");
            if(redstoneControlled.getRedstoneMode() == 1) {
                curInfo.add("gui.tab.problems.provideRedstone");
            } else {
                curInfo.add("gui.tab.problems.removeRedstone");
            }
        }
        if(te instanceof IMinWorkingPressure) {
            IMinWorkingPressure minWork = (IMinWorkingPressure)te;
            if(((TileEntityPneumaticBase)te).getPressure(ForgeDirection.UNKNOWN) < minWork.getMinWorkingPressure()) {
                curInfo.add("gui.tab.problems.notEnoughPressure");
                curInfo.add(I18n.format("gui.tab.problems.applyPressure", minWork.getMinWorkingPressure()));
            }
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        for(IGuiWidget widget : widgets) {
            if(widget.getBounds().contains(par1, par2)) widget.onMouseClicked(par1, par2, par3);
            else widget.onMouseClickedOutsideBounds(par1, par2, par3);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        if(widget instanceof IGuiAnimatedStat) {
            boolean leftSided = ((IGuiAnimatedStat)widget).isLeftSided();
            for(IGuiWidget w : widgets) {
                if(w instanceof IGuiAnimatedStat) {
                    IGuiAnimatedStat stat = (IGuiAnimatedStat)w;
                    if(widget != stat && stat.isLeftSided() == leftSided) {//when the stat is on the same side, close it.
                        stat.closeWindow();
                    }
                }
            }
        }
        sendPacketToServer(widget.getID());
    }

    protected void sendPacketToServer(int id){
        NetworkHandler.sendToServer(new PacketGuiButton(id));
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        for(IGuiWidget widget : widgets) {
            widget.handleMouseInput();
        }
    }

    @Override
    protected void keyTyped(char key, int keyCode){
        for(IGuiWidget widget : widgets) {
            if(widget.onKey(key, keyCode)) return;
        }
        super.keyTyped(key, keyCode);
    }

    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3){
        widgets.clear();
        super.setWorldAndResolution(par1Minecraft, par2, par3);
    }

    public void drawHoveringString(List<String> text, int x, int y, FontRenderer fontRenderer){
        drawHoveringText(text, x, y, fontRenderer);
    }

    public static void drawTexture(String texture, int x, int y){
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiUtils.getResourceLocation(texture));
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + 16, 0, 0.0, 1.0);
        tessellator.addVertexWithUV(x + 16, y + 16, 0, 1.0, 1.0);
        tessellator.addVertexWithUV(x + 16, y, 0, 1.0, 0.0);
        tessellator.addVertexWithUV(x, y, 0, 0.0, 0.0);
        tessellator.draw();
        // this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    public GuiButtonSpecial getButtonFromRectangle(int buttonID, Rectangle buttonSize, String buttonText){
        return new GuiButtonSpecial(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, buttonText);
    }

    public GuiButtonSpecial getInvisibleButtonFromRectangle(int buttonID, Rectangle buttonSize){
        return new GuiButtonSpecial(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, "");
    }

    public WidgetTextField getTextFieldFromRectangle(Rectangle textFieldSize){
        return new WidgetTextField(fontRendererObj, textFieldSize.x, textFieldSize.y, textFieldSize.width, textFieldSize.height);
    }

    public int getGuiLeft(){
        return guiLeft;
    }

    public int getGuiTop(){
        return guiTop;
    }

    //-----------NEI support

    @Override
    @Optional.Method(modid = ModIds.NEI)
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility){
        for(IGuiWidget w : widgets) {
            if(w instanceof IGuiAnimatedStat) {
                IGuiAnimatedStat stat = (IGuiAnimatedStat)w;
                if(stat.isLeftSided()) {
                    if(stat.getWidth() > 20) {
                        currentVisibility.showUtilityButtons = false;
                        currentVisibility.showStateButtons = false;
                    }
                } else {
                    if(stat.getAffectedY() < 10) {
                        currentVisibility.showWidgets = false;
                    }
                }
            }
        }
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item){
        return new ArrayList<Integer>();
    }

    /**
     * @return A list of TaggedInventoryAreas that will be used with the savestates.
     */
    @Override
    @Optional.Method(modid = ModIds.NEI)
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui){
        return null;
    }

    /**
     * Handles clicks while an itemstack has been dragged from the item panel. Use this to set configurable slots and the like. 
     * Changes made to the stackSize of the dragged stack will be kept
     * @param gui The current gui instance
     * @param mousex The x position of the mouse
     * @param mousey The y position of the mouse
     * @param draggedStack The stack being dragged from the item panel
     * @param button The button presed
     * @return True if the drag n drop was handled. False to resume processing through other routes. The held stack will be deleted if draggedStack.stackSize == 0
     */
    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button){
        return false;
    }

    /**
     * Used to prevent the item panel from drawing on top of other gui elements.
     * @param x The x coordinate of the rectangle bounding the slot
     * @param y The y coordinate of the rectangle bounding the slot
     * @param w The w coordinate of the rectangle bounding the slot
     * @param h The h coordinate of the rectangle bounding the slot
     * @return true if the item panel slot within the specified rectangle should not be rendered.
     */
    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h){
        for(IGuiWidget widget : widgets) {
            if(widget instanceof IGuiAnimatedStat) {
                IGuiAnimatedStat stat = (IGuiAnimatedStat)widget;
                if(stat.getBounds().intersects(new Rectangle(x, y, w, h))) return true;
            }
        }
        return false;
    }

    @Override
    public void onKeyTyped(IGuiWidget widget){

    }

    protected void refreshScreen(){
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        setWorldAndResolution(Minecraft.getMinecraft(), i, j);
        for(IGuiWidget widget : widgets) {
            if(widget instanceof GuiAnimatedStat) {
                widget.update();
            }
        }
    }
}
