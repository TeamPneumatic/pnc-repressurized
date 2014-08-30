package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.lib.ModIds;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = ModIds.NEI)
public class GuiPneumaticContainerBase extends GuiContainer implements INEIGuiHandler{
    /**
     * Any GuiAnimatedStat added to this list will be tracked for mouseclicks, tooltip renders, rendering,updating (resolution and expansion).
     */
    protected final List<IGuiAnimatedStat> animatedStatList = new ArrayList<IGuiAnimatedStat>();
    protected List<GuiCheckBox> checkBoxList = new ArrayList<GuiCheckBox>();
    private IGuiAnimatedStat lastLeftStat, lastRightStat;

    public GuiPneumaticContainerBase(Container par1Container){
        super(par1Container);
    }

    protected GuiAnimatedStat addAnimatedStat(String title, ItemStack icon, int color, boolean leftSided){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        GuiAnimatedStat stat = new GuiAnimatedStat(this, title, icon, xStart + (leftSided ? 0 : xSize), leftSided && lastLeftStat != null || !leftSided && lastRightStat != null ? 3 : yStart + 5, color, leftSided ? lastLeftStat : lastRightStat, leftSided);
        animatedStatList.add(stat);
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
        animatedStatList.add(stat);
        if(leftSided) {
            lastLeftStat = stat;
        } else {
            lastRightStat = stat;
        }
        return stat;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int i, int j){
        for(IGuiAnimatedStat stat : animatedStatList) {
            stat.render(fontRendererObj, zLevel, partialTicks);
        }
    }

    @Override
    public void drawScreen(int x, int y, float unknown){
        super.drawScreen(x, y, unknown);
        for(IGuiAnimatedStat stat : animatedStatList) {
            stat.onMouseHovering(fontRendererObj, x, y);
        }
        for(GuiCheckBox checkBox : checkBoxList) {
            checkBox.render(x, y);
        }
        List<String> tooltip = new ArrayList<String>();
        for(Object obj : buttonList) {
            if(obj instanceof GuiButtonSpecial) {
                GuiButtonSpecial button = (GuiButtonSpecial)obj;
                if(button.xPosition < x && button.xPosition + button.getWidth() > x && button.yPosition < y && button.yPosition + button.getHeight() > y) {
                    button.getTooltip(tooltip);
                }
            }
        }
        if(tooltip.size() > 0) {
            drawHoveringString(tooltip, x, y, fontRendererObj);
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        for(IGuiAnimatedStat stat : animatedStatList) {
            if(stat.mouseClicked(par1, par2, par3)) {
                for(IGuiAnimatedStat closingStat : animatedStatList) {
                    if(stat != closingStat && stat.isLeftSided() == closingStat.isLeftSided()) {//when the stat is on the same side, close it.
                        closingStat.closeWindow();
                    }
                }
            }
        }
        for(GuiCheckBox checkBox : checkBoxList) {
            checkBox.render(par1, par2);
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3){
        animatedStatList.clear();
        checkBoxList.clear();
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

    public GuiButton getButtonFromRectangle(int buttonID, Rectangle buttonSize, String buttonText){
        return new GuiButton(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, buttonText);
    }

    public GuiButtonSpecial getInvisibleButtonFromRectangle(int buttonID, Rectangle buttonSize){
        return new GuiButtonSpecial(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, "");
    }

    public GuiTextField getTextFieldFromRectangle(Rectangle textFieldSize){
        return new GuiTextField(fontRendererObj, textFieldSize.x, textFieldSize.y, textFieldSize.width, textFieldSize.height);
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
        for(IGuiAnimatedStat stat : animatedStatList) {
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
        return currentVisibility;
    }

    /**
     * NEI will give the specified item to the InventoryRange returned if the player's inventory is full.
     * return null for no range
     */
    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item){
        return null;
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
        for(IGuiAnimatedStat stat : animatedStatList) {
            if(stat.getDimensions().intersects(new Rectangle(x, y, w, h))) return true;
        }
        return false;
    }

}
