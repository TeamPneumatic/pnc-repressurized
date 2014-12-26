package pneumaticCraft.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.inventory.ContainerInventorySearcher;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInventorySearcher extends GuiContainer{
    private final InventoryBasic inventory = new InventoryBasic("tmp", true, 1);
    private final GuiScreen parentScreen;

    public GuiInventorySearcher(EntityPlayer par1EntityPlayer){
        super(new ContainerInventorySearcher(par1EntityPlayer.inventory));
        par1EntityPlayer.openContainer = inventorySlots;
        allowUserInput = true;
        ySize = 176;//TODO change
        parentScreen = FMLClientHandler.instance().getClient().currentScreen;
        ((ContainerInventorySearcher)inventorySlots).init(inventory);
    }

    public ItemStack getSearchStack(){
        return inventory.getStackInSlot(0);
    }

    public void setSearchStack(ItemStack stack){
        inventory.setInventorySlotContents(0, stack);
    }

    @Override
    protected void handleMouseClick(Slot par1Slot, int par2, int par3, int par4){
        if(par1Slot != null) {
            if(par1Slot.slotNumber == 36) {
                par1Slot.putStack(null);
            } else {
                ItemStack stack = par1Slot.getStack();
                if(stack != null) {
                    stack = stack.copy();
                    stack.stackSize = 1;
                }
                inventory.setInventorySlotContents(0, stack);
            }
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2){
        if(par2 == 1)//esc
        {
            mc.displayGuiScreen(parentScreen);
            onGuiClosed();
        } else {
            super.keyTyped(par1, par2);
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2){
        fontRendererObj.drawString("Inventory", 7, 5, 4210752);
        fontRendererObj.drawString("Searcher", 7, 15, 4210752);
        fontRendererObj.drawString("Target", 71, 8, 4210752);
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3){
        super.drawScreen(par1, par2, par3);
        /*
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(GL11.GL_LIGHTING);*/
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3){
        mc.getTextureManager().bindTexture(Textures.GUI_INVENTORY_SEARCHER);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
    }

}
