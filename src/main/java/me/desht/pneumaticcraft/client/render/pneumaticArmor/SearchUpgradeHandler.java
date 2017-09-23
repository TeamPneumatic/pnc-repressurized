package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumaticHelmet.GuiSearchUpgradeOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUpgradeHandler implements IUpgradeRenderHandler {
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
    public String getUpgradeName() {
        return "itemSearcher";
    }

    @Override
    public void initConfig() {
        statX = ConfigHandler.helmetOptions.itemSearchX;
        statY = ConfigHandler.helmetOptions.itemSearchY;
        statLeftSided = ConfigHandler.helmetOptions.itemSearchLeft;
//        statX = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat X", -1).getInt();
//        statY = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat Y", 17).getInt();
//        statLeftSided = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat leftsided", true).getBoolean(true);
    }

    @Override
    public void saveToConfig() {
//        Configuration config = ConfigHandler.config;
//        config.load();
        ConfigHandler.helmetOptions.itemSearchX = statX = searchInfo.getBaseX();
        ConfigHandler.helmetOptions.itemSearchY = statY = searchInfo.getBaseY();
        ConfigHandler.helmetOptions.itemSearchLeft = statLeftSided = searchInfo.isLeftSided();
        ConfigHandler.sync();
//        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat X", -1).set(searchInfo.getBaseX());
//        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat Y", 17).set(searchInfo.getBaseY());
//        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Item_Search", "stat leftsided", true).set(searchInfo.isLeftSided());
//        config.save();
//        statX = searchInfo.getBaseX();
//        statY = searchInfo.getBaseY();
//        statLeftSided = searchInfo.isLeftSided();
    }

    public void addToSearchedItemCounter(int amount) {
        searchedItemCounter += amount;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades) {
        ticksExisted++;
        ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));

        if (ticksExisted % 20 == 0) {
            List<EntityItem> items = player.world.getEntitiesWithinAABB(EntityItem.class, EntityTrackUpgradeHandler.getAABBFromRange(player, rangeUpgrades));
            searchedItems.clear();
            for (EntityItem item : items) {
                if (!item.getItem().isEmpty() && !searchStack.isEmpty()) {
                    if (item.getItem().isItemEqual(searchStack))
                        searchedItems.put(item, item.getItem().getCount());
                    else {
                        List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(item.getItem());
                        int itemCount = 0;
                        for (ItemStack inventoryItem : inventoryItems) {
                            if (inventoryItem.isItemEqual(searchStack)) {
                                itemCount += inventoryItem.getCount();
                            }
                        }
                        if (itemCount > 0) searchedItems.put(item, itemCount);
                    }
                }
            }

            totalSearchedItemCount = searchedItemCounter;
            searchedItemCounter = 0;
            for (Integer itemCount : searchedItems.values()) {
                searchedItemCounter += itemCount;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks) {
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(Textures.GLOW_RESOURCE);
        //  mc.func_110434_K().func_110577_a(Textures.GLOW_RESOURCE);
        for (Map.Entry<EntityItem, Integer> entry : searchedItems.entrySet()) {
            EntityItem item = entry.getKey();
            float height = MathHelper.sin((item.getAge() + partialTicks) / 10.0F + item.hoverStart) * 0.1F + 0.2F;
            RenderSearchItemBlock.renderSearch(item.lastTickPosX + (item.posX - item.lastTickPosX) * partialTicks, item.lastTickPosY + (item.posY - item.lastTickPosY) * partialTicks + height, item.lastTickPosZ + (item.posZ - item.lastTickPosZ) * partialTicks, entry.getValue(), totalSearchedItemCount);
        }
        for (int i = 0; i < searchedBlocks.size(); i++) {
            if (!searchedBlocks.get(i).renderSearchBlock(totalSearchedItemCount)) {
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
    public void render2D(float partialTicks, boolean helmetEnabled) {
        ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(FMLClientHandler.instance().getClient().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        List<String> textList = new ArrayList<String>();
        if (searchStack.isEmpty()) {
            textList.add("press '" + Keyboard.getKeyName(KeyHandler.getInstance().keybindOpenOptions.getKeyCode()) + "' to configure");
        } else {
            textList.add(searchStack.getDisplayName() + " (" + totalSearchedItemCount + " found)");
        }
        searchInfo.setText(textList);
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{Itemss.upgrades.get(EnumUpgrade.SEARCH)};
    }

    /**
     * This method will be called by the BlockTrackUpgradeHandler when it finds inventories while scanning blocks.
     *
     * @param te TileEntity the tile entity, which is already known to support the item handler capability
     */
    public void checkInventoryForItems(TileEntity te) {
        try {
            ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(FMLClientHandler.instance().getClient().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            assert handler != null;
            boolean hasFoundItem = false;
            if (!searchStack.isEmpty()) {
                for (int l = 0; l < handler.getSlots(); l++) {
                    if (!handler.getStackInSlot(l).isEmpty()) {
                        int items = RenderSearchItemBlock.getSearchedItemCount(handler.getStackInSlot(l), searchStack);
                        if (items > 0) {
                            hasFoundItem = true;
                            searchedItemCounter += items;
                        }
                    }
                }
            }
            if (hasFoundItem) {
                boolean inList = false;
                for (RenderSearchItemBlock trackedBlock : searchedBlocks) {
                    if (trackedBlock.isAlreadyTrackingCoord(te.getPos())) {
                        inList = true;
                        break;
                    }
                }
                if (!inList) searchedBlocks.add(new RenderSearchItemBlock(te.getWorld(), te.getPos()));
            }
        } catch (Throwable ignored) {

        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset() {
        searchedBlocks.clear();
        searchedItemCounter = 0;
        searchedItems.clear();
        ticksExisted = 0;
        searchInfo = null;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player) {
        return PneumaticValues.USAGE_ITEM_SEARCHER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiSearchUpgradeOptions(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiAnimatedStat getAnimatedStat() {
        if (searchInfo == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft);
            searchInfo = new GuiAnimatedStat(null, "Currently searching for:", CraftingRegistrator.getUpgrade(EnumUpgrade.SEARCH), statX != -1 ? statX : sr.getScaledWidth() - 2, statY, 0x3000AA00, null, statLeftSided);
            searchInfo.setMinDimensionsAndReset(0, 0);
        }
        return searchInfo;
    }

}
