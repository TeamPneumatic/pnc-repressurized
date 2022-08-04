/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.StatPanelLayout;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.SearchOptions;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderSearchItemBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.BlockTrackerHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.SearchHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SearchClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<SearchHandler> {
    private static final StatPanelLayout DEFAULT_STAT_LAYOUT = new StatPanelLayout(0.005f, 0.1f, false);

    private int totalSearchedItemCount;
    private int itemSearchCount;
    private int ticksExisted;
    private final Map<ItemEntity, Integer> searchedItems = new HashMap<>();
    private final Map<BlockPos, RenderSearchItemBlock> trackedInventories = new HashMap<>();
    private IGuiAnimatedStat searchInfo;
    private ItemStack searchedStack = ItemStack.EMPTY;

    public SearchClientHandler() {
        super(CommonUpgradeHandlers.searchHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        ticksExisted++;

        if ((ticksExisted & 0xf) == 0) {
            // count up all items in tracked inventories, and cull any inventories with no matching items
            int blockSearchCount = trackInventoryCounts(armorHandler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.RANGE.get()));

            searchedItems.entrySet().removeIf(e -> !e.getKey().isAlive());

            totalSearchedItemCount = itemSearchCount + blockSearchCount;
        }

        Item item = PneumaticArmorItem.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlot.HEAD));
        List<Component> textList = new ArrayList<>();
        if (item == null || item == Items.AIR) {
            textList.add(xlate("pneumaticcraft.armor.search.configure", KeyHandler.getInstance().keybindOpenOptions.getTranslatedKeyMessage()));
        } else {
            if (searchedStack.getItem() != item) searchedStack = new ItemStack(item);
            textList.add(searchedStack.getHoverName().copy().append(xlate("pneumaticcraft.armor.search.found", totalSearchedItemCount)));
        }
        searchInfo.setText(textList);
    }

    @Override
    public void render3D(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        VertexConsumer builder = buffer.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GLOW_RESOURCE, true));

        searchedItems.forEach((item, value) -> {
            float height = Mth.sin((item.getAge() + partialTicks) / 10.0F + item.bobOffs) * 0.1F + 0.2F;
            RenderSearchItemBlock.renderSearch(matrixStack, builder,
                    item.xOld + (item.getX() - item.xOld) * partialTicks,
                    item.yOld + (item.getY() - item.yOld) * partialTicks + height,
                    item.zOld + (item.getZ() - item.zOld) * partialTicks,
                    value, totalSearchedItemCount, partialTicks
            );
        });

        trackedInventories.values().forEach(entry -> entry.renderSearchBlock(matrixStack, builder, totalSearchedItemCount, partialTicks));
    }

    @Override
    public void render2D(PoseStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
    }

    private int trackInventoryCounts(int rangeUpgrades) {
        int blockSearchCount = 0;
        int blockTrackRange = BlockTrackerHandler.BLOCK_TRACKING_RANGE
                + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int blockTrackRangeSq = blockTrackRange * blockTrackRange;

        Player player = ClientUtils.getClientPlayer();
        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos,RenderSearchItemBlock> entry : trackedInventories.entrySet()) {
            int nItems = entry.getKey().distSqr(player.blockPosition()) < blockTrackRangeSq ?
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
    void trackItemEntities(Player player, int rangeUpgrades) {
        searchedItems.clear();
        itemSearchCount = 0;

        Item searchedItem = PneumaticArmorItem.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlot.HEAD));
        if (searchedItem == null || searchedItem == Items.AIR) return;

        List<ItemEntity> items = player.level.getEntitiesOfClass(ItemEntity.class, EntityTrackerClientHandler.getAABBFromRange(player, rangeUpgrades));
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
     * @param te TileEntity the block entity, which is already known to support the item handler capability
     * @param handlerEnabled true if the search handler is actually enabled, false otherwise
     */
    void checkInventoryForItems(BlockEntity te, Direction face, boolean handlerEnabled) {
        if (!handlerEnabled) {
            trackedInventories.clear();
        } else {
            Item searchedItem = PneumaticArmorItem.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlot.HEAD));
            if (searchedItem != null) {
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).ifPresent(handler -> {
                    if (checkForItems(handler, searchedItem)) {
                        trackedInventories.put(te.getBlockPos(), new RenderSearchItemBlock(te.getLevel(), te.getBlockPos()));
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
            ItemStack icon = ModUpgrades.SEARCH.get().getItemStack();
            searchInfo = PneumaticHelmetRegistry.getInstance().makeHUDStatPanel(xlate("pneumaticcraft.armor.gui.search.searchingFor"), icon, this);
            searchInfo.setMinimumContractedDimensions(0, 0);
        }
        return searchInfo;
    }

    @Override
    public StatPanelLayout getDefaultStatLayout() {
        return DEFAULT_STAT_LAYOUT;
    }

    @Override
    public void onResolutionChanged() {
        searchInfo = null;
    }
}
