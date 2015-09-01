package pneumaticCraft.client.render.pneumaticArmor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.KeyHandler;
import pneumaticCraft.client.gui.pneumaticHelmet.GuiSearchUpgradeOptions;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SearchUpgradeHandler implements IUpgradeRenderHandler{
    private int totalSearchedItemCount;
    public int searchedItemCounter;
    private int ticksExisted;
    private final Map<EntityItem, Integer> searchedItems = new HashMap<EntityItem, Integer>();
    private final List<RenderSearchItemBlock> searchedBlocks = new ArrayList<RenderSearchItemBlock>();
    @SideOnly(Side.CLIENT)
    private GuiAnimatedStat searchInfo;
    private int statX;
    private int statY;
    private boolean statLeftSided;

    @Override
    @SideOnly(Side.CLIENT)
    public String getUpgradeName(){
        return "itemSearcher";
    }

    @Override
    public void initConfig(Configuration config){
        statX = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat X", -1).getInt();
        statY = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat Y", 17).getInt();
        statLeftSided = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat leftsided", true).getBoolean(true);
    }

    @Override
    public void saveToConfig(){
        Configuration config = Config.config;
        config.load();
        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat X", -1).set(searchInfo.getBaseX());
        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat Y", 17).set(searchInfo.getBaseY());
        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat leftsided", true).set(searchInfo.isLeftSided());
        config.save();
        statX = searchInfo.getBaseX();
        statY = searchInfo.getBaseY();
        statLeftSided = searchInfo.isLeftSided();
    }

    public void addToSearchedItemCounter(int amount){
        searchedItemCounter += amount;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades){
        ticksExisted++;
        ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(player.getCurrentArmor(3));

        if(ticksExisted % 20 == 0) {
            List<EntityItem> items = player.worldObj.getEntitiesWithinAABB(EntityItem.class, EntityTrackUpgradeHandler.getAABBFromRange(player, rangeUpgrades));
            searchedItems.clear();
            for(EntityItem item : items) {
                if(item.getEntityItem() != null && searchStack != null) {
                    if(item.getEntityItem().isItemEqual(searchStack)) searchedItems.put(item, item.getEntityItem().stackSize);
                    else {
                        List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(item.getEntityItem());
                        int itemCount = 0;
                        for(ItemStack inventoryItem : inventoryItems) {
                            if(inventoryItem.isItemEqual(searchStack)) {
                                itemCount += inventoryItem.stackSize;
                            }
                        }
                        if(itemCount > 0) searchedItems.put(item, itemCount);
                    }
                }
            }

            totalSearchedItemCount = searchedItemCounter;
            searchedItemCounter = 0;
            for(Integer itemCount : searchedItems.values()) {
                searchedItemCounter += itemCount;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks){
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderManager.instance.renderEngine.bindTexture(Textures.GLOW_RESOURCE);
        //  mc.func_110434_K().func_110577_a(Textures.GLOW_RESOURCE);
        for(Map.Entry<EntityItem, Integer> entry : searchedItems.entrySet()) {
            EntityItem item = entry.getKey();
            float height = MathHelper.sin((item.age + partialTicks) / 10.0F + item.hoverStart) * 0.1F + 0.2F;
            RenderSearchItemBlock.renderSearch(item.lastTickPosX + (item.posX - item.lastTickPosX) * partialTicks, item.lastTickPosY + (item.posY - item.lastTickPosY) * partialTicks + height, item.lastTickPosZ + (item.posZ - item.lastTickPosZ) * partialTicks, entry.getValue(), totalSearchedItemCount);
        }
        for(int i = 0; i < searchedBlocks.size(); i++) {
            if(!searchedBlocks.get(i).renderSearchBlock(totalSearchedItemCount)) {
                searchedBlocks.remove(i);
                i--;
            }
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render2D(float partialTicks, boolean helmetEnabled){
        ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(FMLClientHandler.instance().getClient().thePlayer.getCurrentArmor(3));
        List<String> textList = new ArrayList<String>();
        if(searchStack == null) {
            textList.add("press '" + Keyboard.getKeyName(KeyHandler.getInstance().keybindOpenOptions.getKeyCode()) + "' to configure");
        } else {
            textList.add(searchStack.getDisplayName() + " (" + totalSearchedItemCount + " found)");
        }
        searchInfo.setText(textList);
    }

    @Override
    public boolean isEnabled(ItemStack[] upgradeStacks){
        for(ItemStack stack : upgradeStacks) {
            if(stack != null && stack.getItem() == Itemss.machineUpgrade && stack.getItemDamage() == ItemMachineUpgrade.UPGRADE_SEARCH_DAMAGE) return true;
        }
        return false;
    }

    /**
     * This method will be called by the BlockTrackUpgradeHandler when it finds inventories while scanning blocks.
     * @param te TileEntity that already has been checked on if it implements IInventory, so it's save to cast it to IInventory.
     */
    public void checkInventoryForItems(TileEntity te){
        try {
            ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(FMLClientHandler.instance().getClient().thePlayer.getCurrentArmor(3));
            IInventory inventory = (IInventory)te;
            boolean hasFoundItem = false;
            if(searchStack != null) {
                for(int l = 0; l < inventory.getSizeInventory(); l++) {
                    if(inventory.getStackInSlot(l) != null) {
                        int items = RenderSearchItemBlock.getSearchedItemCount(inventory.getStackInSlot(l), searchStack);
                        if(items > 0) {
                            hasFoundItem = true;
                            searchedItemCounter += items;
                        }
                    }
                }
            }
            if(hasFoundItem) {
                boolean inList = false;
                for(RenderSearchItemBlock trackedBlock : searchedBlocks) {
                    if(trackedBlock.isAlreadyTrackingCoord(te.xCoord, te.yCoord, te.zCoord)) {
                        inList = true;
                        break;
                    }
                }
                if(!inList) searchedBlocks.add(new RenderSearchItemBlock(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord));
            }
        } catch(Throwable e) {

        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset(){
        searchedBlocks.clear();
        searchedItemCounter = 0;
        searchedItems.clear();
        ticksExisted = 0;
        searchInfo = null;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player){
        return PneumaticValues.USAGE_ITEM_SEARCHER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage(){
        return new GuiSearchUpgradeOptions(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiAnimatedStat getAnimatedStat(){
        if(searchInfo == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
            searchInfo = new GuiAnimatedStat(null, "Currently searching for:", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SEARCH_DAMAGE), statX != -1 ? statX : sr.getScaledWidth() - 2, statY, 0x3000AA00, null, statLeftSided);
            searchInfo.setMinDimensionsAndReset(0, 0);
        }
        return searchInfo;
    }

}
