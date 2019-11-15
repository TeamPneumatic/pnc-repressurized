package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiSearchUpgradeOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderSearchItemBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUpgradeHandler implements IUpgradeRenderHandler {
    private int totalSearchedItemCount;
    private int itemSearchCount;
    private int ticksExisted;
    private final Map<ItemEntity, Integer> searchedItems = new HashMap<>();
    private final Map<BlockPos, RenderSearchItemBlock> trackedInventories = new HashMap<>();
    @OnlyIn(Dist.CLIENT)
    private GuiAnimatedStat searchInfo;
    private ItemStack searchedStack = ItemStack.EMPTY;

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getUpgradeName() {
        return "itemSearcher";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void update(PlayerEntity player, int rangeUpgrades) {
        ticksExisted++;

        if ((ticksExisted & 0xf) == 0) {
            // count up all items in tracked inventories, and cull any inventories with no matching items
            int blockSearchCount = trackInventoryCounts(rangeUpgrades);

            searchedItems.entrySet().removeIf(e -> !e.getKey().isAlive());

            totalSearchedItemCount = itemSearchCount + blockSearchCount;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render3D(float partialTicks) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepthTest();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getInstance().getTextureManager().bindTexture(Textures.GLOW_RESOURCE);

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
        GlStateManager.enableDepthTest();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableRescaleNormal();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render2D(float partialTicks, boolean helmetEnabled) {
        Item item = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
        List<String> textList = new ArrayList<>();
        if (item == null) {
            textList.add("press '" + KeyHandler.getInstance().keybindOpenOptions.getKeyDescription() + "' to configure");
        } else {
            if (searchedStack.getItem() != item) searchedStack = new ItemStack(item);
            textList.add(searchedStack.getDisplayName() + " (" + totalSearchedItemCount + " found)");
        }
        searchInfo.setText(textList);
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{ EnumUpgrade.SEARCH.getItem() };
    }

    private int trackInventoryCounts(int rangeUpgrades) {
        int blockSearchCount = 0;
        int blockTrackRange = BlockTrackUpgradeHandler.BLOCK_TRACKING_RANGE
                + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int blockTrackRangeSq = blockTrackRange * blockTrackRange;

        PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos,RenderSearchItemBlock> entry : trackedInventories.entrySet()) {
            int nItems = entry.getKey().distanceSq(player.getPosition()) < blockTrackRangeSq ?
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
     * @param player the player
     * @param rangeUpgrades number of range upgrades installed in the helmet
     * @param handlerEnabled true if the search handler is actually enabled, false otherwise
     */
    void trackItemEntities(PlayerEntity player, int rangeUpgrades, boolean handlerEnabled) {
        searchedItems.clear();
        itemSearchCount = 0;

        if (!handlerEnabled) return;

        Item searchedItem = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
        List<ItemEntity> items = player.world.getEntitiesWithinAABB(ItemEntity.class, EntityTrackUpgradeHandler.getAABBFromRange(player, rangeUpgrades));

        for (ItemEntity itemEntity : items) {
            if (!itemEntity.getItem().isEmpty() && searchedItem != null) {
                if (itemEntity.getItem().getItem() == searchedItem) {
                    searchedItems.put(itemEntity, itemEntity.getItem().getCount());
                    itemSearchCount += itemEntity.getItem().getCount();
                } else {
                    List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(itemEntity.getItem());
                    int itemCount = 0;
                    for (ItemStack inventoryItem : inventoryItems) {
                        if (inventoryItem.getItem() == searchedItem) {
                            itemCount += inventoryItem.getCount();
                        }
                    }
                    if (itemCount > 0) {
                        searchedItems.put(itemEntity, itemCount);
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
    void checkInventoryForItems(TileEntity te, Direction face, boolean handlerEnabled) {
        if (!handlerEnabled) {
            trackedInventories.clear();
        } else {
            Item searchedItem = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
            if (searchedItem != null) {
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).ifPresent(handler -> {
                    if (checkForItems(handler, searchedItem)) {
                        trackedInventories.put(te.getPos(), new RenderSearchItemBlock(te.getWorld(), te.getPos()));
                    }
                });
            }
        }
    }

    private boolean checkForItems(IItemHandler handler, Item item) {
        for (int l = 0; l < handler.getSlots(); l++) {
            if (!handler.getStackInSlot(l).isEmpty()) {
                int items = RenderSearchItemBlock.getSearchedItemCount(handler.getStackInSlot(l), item);
                if (items > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void reset() {
        trackedInventories.clear();
        searchedItems.clear();
        ticksExisted = 0;
        searchInfo = null;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
        return PneumaticValues.USAGE_ITEM_SEARCHER;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiSearchUpgradeOptions(this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiAnimatedStat getAnimatedStat() {
        if (searchInfo == null) {
            GuiAnimatedStat.StatIcon icon = GuiAnimatedStat.StatIcon.of(EnumUpgrade.SEARCH.getItem());
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
