package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiSearchUpgradeOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderSearchItemBlock;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUpgradeHandler implements IUpgradeRenderHandler {
    private int totalSearchedItemCount;
    private int itemSearchCount;
    private int ticksExisted;
    private final Map<EntityItem, Integer> searchedItems = new HashMap<>();
    private final Map<BlockPos, RenderSearchItemBlock> trackedInventories = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private GuiAnimatedStat searchInfo;

    @Override
    @SideOnly(Side.CLIENT)
    public String getUpgradeName() {
        return "itemSearcher";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades) {
        ticksExisted++;

        if ((ticksExisted & 0xf) == 0) {
            // count up all items in tracked inventories, and cull any inventories with no matching items
            int blockSearchCount = trackInventoryCounts(rangeUpgrades);

            searchedItems.entrySet().removeIf(e -> !e.getKey().isEntityAlive());

            totalSearchedItemCount = itemSearchCount + blockSearchCount;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(Textures.GLOW_RESOURCE);

        searchedItems.forEach((item, value) -> {
            float height = MathHelper.sin((item.getAge() + partialTicks) / 10.0F + item.hoverStart) * 0.1F + 0.2F;
            RenderSearchItemBlock.renderSearch(
                    item.lastTickPosX + (item.posX - item.lastTickPosX) * partialTicks,
                    item.lastTickPosY + (item.posY - item.lastTickPosY) * partialTicks + height,
                    item.lastTickPosZ + (item.posZ - item.lastTickPosZ) * partialTicks, value,
                    totalSearchedItemCount, partialTicks
            );
        });

        trackedInventories.values().forEach(entry -> entry.renderSearchBlock(totalSearchedItemCount, partialTicks));

        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableRescaleNormal();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render2D(float partialTicks, boolean helmetEnabled) {
        ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        List<String> textList = new ArrayList<>();
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

    private int trackInventoryCounts(int rangeUpgrades) {
        int blockSearchCount = 0;
        int blockTrackRange = BlockTrackUpgradeHandler.BLOCK_TRACKING_RANGE
                + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int blockTrackRangeSq = blockTrackRange * blockTrackRange;

        EntityPlayer player = Minecraft.getMinecraft().player;
        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos,RenderSearchItemBlock> entry : trackedInventories.entrySet()) {
            int nItems = entry.getKey().distanceSq(player.posX, player.posY, player.posZ) < blockTrackRangeSq ?
                    entry.getValue().getSearchedItemCount() : 0;

            if (nItems == 0) {
                toRemove.add(entry.getKey());
            }
            blockSearchCount += nItems;
        }
        toRemove.forEach(trackedInventories::remove);

        return blockSearchCount;
    }

    /**
     * Called by the EntityTrackerUpgradeHandler every 16 ticks to find items in item entities on the ground.
     *  @param player the player
     * @param rangeUpgrades number of range upgrades installed in the helmet
     * @param handlerEnabled true if the search handler is actually enabled, false otherwise
     */
    void trackItemEntities(EntityPlayer player, int rangeUpgrades, boolean handlerEnabled) {
        searchedItems.clear();
        itemSearchCount = 0;

        if (!handlerEnabled) return;

        ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
        List<EntityItem> items = player.world.getEntitiesWithinAABB(EntityItem.class, EntityTrackUpgradeHandler.getAABBFromRange(player, rangeUpgrades));

        for (EntityItem item : items) {
            if (!item.getItem().isEmpty() && !searchStack.isEmpty()) {
                if (item.getItem().isItemEqual(searchStack)) {
                    searchedItems.put(item, item.getItem().getCount());
                    itemSearchCount += item.getItem().getCount();
                } else {
                    List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(item.getItem());
                    int itemCount = 0;
                    for (ItemStack inventoryItem : inventoryItems) {
                        if (inventoryItem.isItemEqual(searchStack)) {
                            itemCount += inventoryItem.getCount();
                        }
                    }
                    if (itemCount > 0) {
                        searchedItems.put(item, itemCount);
                        itemSearchCount += itemCount;
                    }
                }
            }
        }
    }

    /**
     * Called by the BlockTrackUpgradeHandler when it finds inventories while scanning blocks.  If
     * the inventory contains any of the searched item, its position is added to a track list.
     *
     * @param te TileEntity the tile entity, which is already known to support the item handler capability
     * @param handlerEnabled true if the search handler is actually enabled, false otherwise
     */
    void checkInventoryForItems(TileEntity te, EnumFacing face, boolean handlerEnabled) {
        if (!handlerEnabled) {
            trackedInventories.clear();
        } else {
            ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
            if (!searchStack.isEmpty()) {
                if (checkForItems(handler, searchStack)) {
                    trackedInventories.put(te.getPos(), new RenderSearchItemBlock(te.getWorld(), te.getPos()));
                }
            }
        }
    }

    private boolean checkForItems(IItemHandler handler, ItemStack searchStack) {
        for (int l = 0; l < handler.getSlots(); l++) {
            if (!handler.getStackInSlot(l).isEmpty()) {
                int items = RenderSearchItemBlock.getSearchedItemCount(handler.getStackInSlot(l), searchStack);
                if (items > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset() {
        trackedInventories.clear();
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
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.HEAD;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiAnimatedStat getAnimatedStat() {
        if (searchInfo == null) {
            GuiAnimatedStat.StatIcon icon = GuiAnimatedStat.StatIcon.of(CraftingRegistrator.getUpgrade(EnumUpgrade.SEARCH));
            searchInfo = new GuiAnimatedStat(null, "Currently searching for:", icon,
                    0x3000AA00, null, ArmorHUDLayout.INSTANCE.itemSearchStat);
            searchInfo.setMinDimensionsAndReset(0, 0);
        }
        return searchInfo;
    }

    @Override
    public void onResolutionChanged() {
        searchInfo = null;
    }
}
