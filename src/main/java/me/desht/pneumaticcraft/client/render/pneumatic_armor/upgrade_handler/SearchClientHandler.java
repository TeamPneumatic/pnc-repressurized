package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.SearchOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderSearchItemBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.BlockTrackerHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.SearchHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SearchClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<SearchHandler> {
    private int totalSearchedItemCount;
    private int itemSearchCount;
    private int ticksExisted;
    private final Map<ItemEntity, Integer> searchedItems = new HashMap<>();
    private final Map<BlockPos, RenderSearchItemBlock> trackedInventories = new HashMap<>();
    private IGuiAnimatedStat searchInfo;
    private ItemStack searchedStack = ItemStack.EMPTY;

    public SearchClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().searchHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        ticksExisted++;

        if ((ticksExisted & 0xf) == 0) {
            // count up all items in tracked inventories, and cull any inventories with no matching items
            int blockSearchCount = trackInventoryCounts(armorHandler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.RANGE));

            searchedItems.entrySet().removeIf(e -> !e.getKey().isAlive());

            totalSearchedItemCount = itemSearchCount + blockSearchCount;
        }

        Item item = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
        List<ITextComponent> textList = new ArrayList<>();
        if (item == null || item == Items.AIR) {
            textList.add(xlate("pneumaticcraft.armor.search.configure", I18n.format(KeyHandler.getInstance().keybindOpenOptions.getTranslationKey())));
        } else {
            if (searchedStack.getItem() != item) searchedStack = new ItemStack(item);
            textList.add(searchedStack.getDisplayName().deepCopy().append(xlate("pneumaticcraft.armor.search.found", totalSearchedItemCount)));
        }
        searchInfo.setText(textList);
    }

    @Override
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        IVertexBuilder builder = buffer.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GLOW_RESOURCE, true));

        searchedItems.forEach((item, value) -> {
            float height = MathHelper.sin((item.getAge() + partialTicks) / 10.0F + item.hoverStart) * 0.1F + 0.2F;
            RenderSearchItemBlock.renderSearch(matrixStack, builder,
                    item.lastTickPosX + (item.getPosX() - item.lastTickPosX) * partialTicks,
                    item.lastTickPosY + (item.getPosY() - item.lastTickPosY) * partialTicks + height,
                    item.lastTickPosZ + (item.getPosZ() - item.lastTickPosZ) * partialTicks, value,
                    totalSearchedItemCount, partialTicks
            );
        });

        trackedInventories.values().forEach(entry -> entry.renderSearchBlock(matrixStack, builder, totalSearchedItemCount, partialTicks));
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
    }

    private int trackInventoryCounts(int rangeUpgrades) {
        int blockSearchCount = 0;
        int blockTrackRange = BlockTrackerHandler.BLOCK_TRACKING_RANGE
                + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int blockTrackRangeSq = blockTrackRange * blockTrackRange;

        PlayerEntity player = ClientUtils.getClientPlayer();
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
     */
    void trackItemEntities(PlayerEntity player, int rangeUpgrades) {
        searchedItems.clear();
        itemSearchCount = 0;

        Item searchedItem = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
        if (searchedItem == null || searchedItem == Items.AIR) return;

        List<ItemEntity> items = player.world.getEntitiesWithinAABB(ItemEntity.class, EntityTrackerClientHandler.getAABBFromRange(player, rangeUpgrades));
        for (ItemEntity itemEntity : items) {
            if (!itemEntity.getItem().isEmpty()) {
                if (itemEntity.getItem().getItem() == searchedItem) {
                    searchedItems.put(itemEntity, itemEntity.getItem().getCount());
                    itemSearchCount += itemEntity.getItem().getCount();
                } else {
                    List<ItemStack> inventoryItems = ItemRegistry.getInstance().getStacksInItem(itemEntity.getItem());
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
    public void reset() {
        trackedInventories.clear();
        searchedItems.clear();
        ticksExisted = 0;
        searchInfo = null;
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new SearchOptions(screen,this);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (searchInfo == null) {
            WidgetAnimatedStat.StatIcon icon = WidgetAnimatedStat.StatIcon.of(EnumUpgrade.SEARCH.getItemStack());
            searchInfo = new WidgetAnimatedStat(null, xlate("pneumaticcraft.armor.gui.search.searchingFor"), icon,
                    HUDHandler.getInstance().getStatOverlayColor(), null, ArmorHUDLayout.INSTANCE.itemSearchStat);
            searchInfo.setMinimumContractedDimensions(0, 0);
        }
        return searchInfo;
    }

    @Override
    public void onResolutionChanged() {
        searchInfo = null;
    }
}
