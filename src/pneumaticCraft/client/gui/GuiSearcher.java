package pneumaticCraft.client.gui;

import java.util.Iterator;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.pneumaticHelmet.GuiHelmetMainScreen;
import pneumaticCraft.common.inventory.ContainerSearcher;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSearcher extends InventoryEffectRenderer{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_ITEM_SEARCHER_LOCATION);
    private static final ResourceLocation scrollTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private final InventoryBasic inventory = new InventoryBasic("tmp", true, 49);
    private final GuiScreen parentScreen;

    /** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
    private float currentScroll;

    /** True if the scrollbar is being dragged */
    private boolean isScrolling;

    /**
     * True if the left mouse button was held down last time drawScreen was called.
     */
    private boolean wasClicking;
    private GuiTextField searchField;
    private boolean firstRun = true;

    /**
     * Used to back up the ContainerSearcher's inventory slots before filling it with the player's inventory slots for
     * the inventory tab.
     */
    //private List backupContainerSlots;
    private boolean field_74234_w;

    public GuiSearcher(EntityPlayer par1EntityPlayer){
        super(new ContainerSearcher());
        par1EntityPlayer.openContainer = inventorySlots;
        allowUserInput = true;
        ySize = 176;
        parentScreen = FMLClientHandler.instance().getClient().currentScreen;
        ((ContainerSearcher)inventorySlots).init(this);
    }

    public ItemStack getSearchStack(){
        return inventory.getStackInSlot(48);
    }

    public void setSearchStack(ItemStack stack){
        inventory.setInventorySlotContents(48, stack);
    }

    @Override
    protected void handleMouseClick(Slot par1Slot, int par2, int par3, int par4){
        if(par1Slot != null) {
            if(par1Slot.slotNumber == 48) {
                par1Slot.putStack(null);
            } else {
                inventory.setInventorySlotContents(48, par1Slot.getStack());
            }
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui(){
        super.initGui();
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        searchField = new GuiTextField(fontRendererObj, guiLeft + 20, guiTop + 36, 89, fontRendererObj.FONT_HEIGHT);
        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setVisible(true);
        searchField.setFocused(true);
        searchField.setTextColor(16777215);

        updateCreativeSearch();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen(){
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        if(parentScreen instanceof GuiHelmetMainScreen) {
            if(player.getCurrentArmor(3) == null || player.getCurrentArmor(3).getItem() != Itemss.pneumaticHelmet) {
                mc.displayGuiScreen(parentScreen);
                onGuiClosed();
            }
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2){
        if(field_74234_w) {
            field_74234_w = false;
            searchField.setText("");
        }

        if(par2 == 1)//esc
        {
            mc.displayGuiScreen(parentScreen);
            onGuiClosed();
        } else {
            if(searchField.textboxKeyTyped(par1, par2)) {
                updateCreativeSearch();
            } else {
                super.keyTyped(par1, par2);
            }
        }
    }

    private void updateCreativeSearch(){
        ContainerSearcher containerSearcher = (ContainerSearcher)inventorySlots;
        containerSearcher.itemList.clear();

        Iterator iterator = Item.itemRegistry.iterator();

        while(iterator.hasNext()) {
            Item item = (Item)iterator.next();

            if(item != null && item.getCreativeTab() != null) {
                item.getSubItems(item, (CreativeTabs)null, containerSearcher.itemList);
            }
        }

        Enchantment[] aenchantment = Enchantment.enchantmentsList;
        int j = aenchantment.length;

        for(int i = 0; i < j; ++i) {
            Enchantment enchantment = aenchantment[i];

            if(enchantment != null && enchantment.type != null) {
                Items.enchanted_book.func_92113_a(enchantment, containerSearcher.itemList);
            }
        }

        iterator = containerSearcher.itemList.iterator();
        String s = searchField.getText().toLowerCase();

        while(iterator.hasNext()) {
            ItemStack itemstack = (ItemStack)iterator.next();
            boolean flag = false;
            Iterator iterator1 = itemstack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips).iterator();

            while(true) {
                if(iterator1.hasNext()) {
                    String s1 = (String)iterator1.next();

                    if(!s1.toLowerCase().contains(s)) {
                        continue;
                    }

                    flag = true;
                }

                if(!flag) {
                    iterator.remove();
                }

                break;
            }
        }

        currentScroll = 0.0F;
        containerSearcher.scrollTo(0.0F);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2){
        fontRendererObj.drawString("Item Searcher", 23, 5, 4210752);
        fontRendererObj.drawString("Search Box", 23, 25, 4210752);
        fontRendererObj.drawString("Target", 113, 10, 4210752);
    }

    /**
     * Called when the mouse is clicked.
     */
    @Override
    protected void mouseClicked(int par1, int par2, int par3){

        super.mouseClicked(par1, par2, par3);
    }

    /**
     * Called when the mouse is moved or a mouse button is released.  Signature: (mouseX, mouseY, which) which==-1 is
     * mouseMove, which==0 or which==1 is mouseUp
     */
    @Override
    protected void mouseMovedOrUp(int par1, int par2, int par3){
        super.mouseMovedOrUp(par1, par2, par3);
    }

    /**
     * returns (if you are not on the inventoryTab) and (the flag isn't set) and( you have more than 1 page of items)
     */
    private boolean needsScrollBars(){
        return ((ContainerSearcher)inventorySlots).hasMoreThan1PageOfItemsInList();
    }

    /*
    private void setCurrentCreativeTab(CreativeTabs par1CreativeTabs){
        if(par1CreativeTabs == null) {
            return;
        }

        int i = selectedTabIndex;
        selectedTabIndex = par1CreativeTabs.getTabIndex();
        ContainerSearcher ContainerSearcher = (ContainerSearcher)inventorySlots;
        field_94077_p.clear();
        ContainerSearcher.itemList.clear();
        par1CreativeTabs.displayAllReleventItems(ContainerSearcher.itemList);

        if(par1CreativeTabs == CreativeTabs.tabInventory) {
            Container container = mc.thePlayer.inventoryContainer;

            if(backupContainerSlots == null) {
                backupContainerSlots = ContainerSearcher.inventorySlots;
            }

            ContainerSearcher.inventorySlots = new ArrayList();

            for(int j = 0; j < container.inventorySlots.size(); ++j) {
                SlotCreativeInventory slotcreativeinventory = new SlotCreativeInventory(this, (Slot)container.inventorySlots.get(j), j);
                ContainerSearcher.inventorySlots.add(slotcreativeinventory);
                int k;
                int l;
                int i1;

                if(j >= 5 && j < 9) {
                    k = j - 5;
                    l = k / 2;
                    i1 = k % 2;
                    slotcreativeinventory.xDisplayPosition = 9 + l * 54;
                    slotcreativeinventory.yDisplayPosition = 6 + i1 * 27;
                } else if(j >= 0 && j < 5) {
                    slotcreativeinventory.yDisplayPosition = -2000;
                    slotcreativeinventory.xDisplayPosition = -2000;
                } else if(j < container.inventorySlots.size()) {
                    k = j - 9;
                    l = k % 9;
                    i1 = k / 9;
                    slotcreativeinventory.xDisplayPosition = 9 + l * 18;

                    if(j >= 36) {
                        slotcreativeinventory.yDisplayPosition = 112;
                    } else {
                        slotcreativeinventory.yDisplayPosition = 54 + i1 * 18;
                    }
                }
            }

            field_74235_v = new Slot(inventory, 0, 173, 112);
            ContainerSearcher.inventorySlots.add(field_74235_v);
        } else if(i == CreativeTabs.tabInventory.getTabIndex()) {
            ContainerSearcher.inventorySlots = backupContainerSlots;
            backupContainerSlots = null;
        }

        if(searchField != null) {
            if(par1CreativeTabs == CreativeTabs.tabAllSearch) {
                searchField.setVisible(true);
                searchField.setCanLoseFocus(false);
                searchField.setFocused(true);
                searchField.setText("");
                updateCreativeSearch();
            } else {
                searchField.setVisible(false);
                searchField.setCanLoseFocus(true);
                searchField.setFocused(false);
            }
        }

        currentScroll = 0.0F;
        ContainerSearcher.scrollTo(0.0F);
    }
    */
    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if(i != 0 && needsScrollBars()) {
            int j = ((ContainerSearcher)inventorySlots).itemList.size() / 9 - 5 + 1;

            if(i > 0) {
                i = 1;
            }

            if(i < 0) {
                i = -1;
            }

            currentScroll = (float)(currentScroll - (double)i / (double)j);

            if(currentScroll < 0.0F) {
                currentScroll = 0.0F;
            }

            if(currentScroll > 1.0F) {
                currentScroll = 1.0F;
            }

            ((ContainerSearcher)inventorySlots).scrollTo(currentScroll);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3){
        boolean flag = Mouse.isButtonDown(0);
        int k = guiLeft;
        int l = guiTop;
        int i1 = k + 156;
        int j1 = l + 48;
        int k1 = i1 + 14;
        int l1 = j1 + 112;
        if(!wasClicking && flag && par1 >= i1 && par2 >= j1 && par1 < k1 && par2 < l1) {
            isScrolling = needsScrollBars();
        }

        if(!flag) {
            isScrolling = false;
        }

        wasClicking = flag;

        if(isScrolling) {
            currentScroll = (par2 - j1 - 7.5F) / (l1 - j1 - 15.0F);

            if(currentScroll < 0.0F) {
                currentScroll = 0.0F;
            }

            if(currentScroll > 1.0F) {
                currentScroll = 1.0F;
            }

            ((ContainerSearcher)inventorySlots).scrollTo(currentScroll);
        }
        if(firstRun) {
            firstRun = false;
            ((ContainerSearcher)inventorySlots).scrollTo(0);
        }

        super.drawScreen(par1, par2, par3);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3){
        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
        searchField.drawTextBox();

        int i1 = guiLeft + 156;
        int k = guiTop + 48;
        int l = k + 112;
        mc.getTextureManager().bindTexture(scrollTexture);
        drawTexturedModalRect(i1, k + (int)((l - k - 17) * currentScroll), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);

    }

    /**
     * Returns the creative inventory
     */
    public InventoryBasic getInventory(){
        return inventory;
    }
}
