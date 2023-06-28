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

package me.desht.pneumaticcraft.common.upgrades;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.upgrade.IUpgradeRegistry;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.UpgradeItem;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.mixin.accessors.BlockEntityTypeAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum ApplicableUpgradesDB implements IUpgradeRegistry {
    INSTANCE;

    private static final int MAX_UPGRADES_IN_TOOLTIP = 12;

    private final Table<BlockEntityType<?>, PNCUpgrade, Integer> TILE_ENTITIES = Tables.synchronizedTable(HashBasedTable.create());
    private final Table<EntityType<?>, PNCUpgrade, Integer> ENTITIES = Tables.synchronizedTable(HashBasedTable.create());
    private final Table<Item, PNCUpgrade, Integer> ITEMS = Tables.synchronizedTable(HashBasedTable.create());

    private final Map<ResourceLocation, PNCUpgrade> knownUpgrades = new ConcurrentHashMap<>();
    private final Map<PNCUpgrade,Set<Item>> acceptedUpgrades = new ConcurrentHashMap<>();

    public static ApplicableUpgradesDB getInstance() {
        return INSTANCE;
    }

    @Override
    public void addApplicableUpgrades(BlockEntityType<?> type, IUpgradeRegistry.Builder builder) {
        addUpgrades(TILE_ENTITIES, type, builder);
    }

    @Override
    public void addApplicableUpgrades(EntityType<?> type, IUpgradeRegistry.Builder builder) {
        addUpgrades(ENTITIES, type, builder);
    }

    @Override
    public void addApplicableUpgrades(Item item, IUpgradeRegistry.Builder builder) {
        addUpgrades(ITEMS, item, builder);
    }

    @Override
    public int getMaxUpgrades(BlockEntity te, PNCUpgrade upgrade) {
        if (te == null || upgrade == null) return 0;
        Integer max = TILE_ENTITIES.get(te.getType(), upgrade);
        return max == null ? 0 : max;
    }

    @Override
    public int getMaxUpgrades(Entity entity, PNCUpgrade upgrade) {
        if (entity == null || upgrade == null) return 0;
        Integer max = ENTITIES.get(entity.getType(), upgrade);
        return max == null ? 0 : max;
    }

    @Override
    public int getMaxUpgrades(Item item, PNCUpgrade upgrade) {
        if (item == null || upgrade == null) return 0;
        Integer max = ITEMS.get(item, upgrade);
        return max == null ? 0 : max;
    }

    @Override
    public void addUpgradeTooltip(PNCUpgrade upgrade, List<Component> tooltip) {
        Collection<Item> acceptors = ApplicableUpgradesDB.getInstance().getItemsWhichAccept(upgrade);
        if (!acceptors.isEmpty()) {
            List<Component> tempList = new ArrayList<>(acceptors.size());
            for (Item acceptor : acceptors) {
                tempList.add(Symbols.bullet().append(acceptor.getDescription().copy().withStyle(ChatFormatting.DARK_AQUA)));
            }
            tempList.sort(Comparator.comparing(Component::getString));
            if (tempList.size() > MAX_UPGRADES_IN_TOOLTIP) {
                int n = (int) ((ClientUtils.getClientLevel().getGameTime() / 8) % acceptors.size());
                List<Component> tempList2 = new ArrayList<>(MAX_UPGRADES_IN_TOOLTIP);
                for (int i = 0; i < MAX_UPGRADES_IN_TOOLTIP; i++) {
                    tempList2.add(tempList.get((n + i) % acceptors.size()));
                }
                tooltip.addAll(tempList2);
            } else {
                tooltip.addAll(tempList);
            }
        }
    }

    @Override
    public PNCUpgrade registerUpgrade(ResourceLocation id, int maxTier, String... depModIds) {
        PNCUpgrade upgrade = new PNCUpgradeImpl(id, maxTier, depModIds);
        if (knownUpgrades.put(upgrade.getId(), upgrade) != null) {
            throw new IllegalStateException("duplicate upgrade ID: " + id);
        }
        return upgrade;
    }

    @Override
    public PNCUpgrade getUpgradeById(ResourceLocation upgradeId) {
        return knownUpgrades.get(upgradeId);
    }

    @Override
    public Collection<PNCUpgrade> getKnownUpgrades() {
        return Collections.unmodifiableCollection(knownUpgrades.values());
    }

    @Override
    public Item makeUpgradeItem(PNCUpgrade upgrade, int tier) {
        return new UpgradeItem(upgrade, tier);
    }

    @Override
    public Item makeUpgradeItem(PNCUpgrade upgrade, int tier, Item.Properties properties) {
        return new UpgradeItem(upgrade, tier, properties);
    }

    @Override
    public int getUpgradeCount(ItemStack stack, PNCUpgrade upgrade) {
        return UpgradableItemUtils.getUpgradeCount(stack, upgrade);
    }

    @Override
    public Map<PNCUpgrade, Integer> getUpgradesInItem(ItemStack stack) {
        return UpgradableItemUtils.getUpgrades(stack);
    }

    @Override
    public ResourceLocation getItemRegistryName(PNCUpgrade upgrade, int tier) {
        return upgrade.getItemRegistryName(tier);
    }

    public Collection<Item> getItemsWhichAccept(PNCUpgrade upgrade) {
        return acceptedUpgrades.getOrDefault(upgrade, Collections.emptySet());
    }

    public Map<PNCUpgrade, Integer> getApplicableUpgrades(BlockEntity te) {
        return TILE_ENTITIES.row(te.getType());
    }

    public Map<PNCUpgrade, Integer> getApplicableUpgrades(Entity e) {
        return ENTITIES.row(e.getType());
    }

    public Map<PNCUpgrade, Integer> getApplicableUpgrades(Item item) {
        return ITEMS.row(item);
    }

    private <T> void addUpgrades(Table<T,PNCUpgrade,Integer> table, T entry, IUpgradeRegistry.Builder builder) {
        builder.getUpgrades().forEach((upgrade, max) -> {
            table.put(entry, upgrade, max);
            if (entry instanceof Item item) {
                addAccepted(upgrade, item);
            } else if (entry instanceof BlockEntityType<?> beType) {
                ((BlockEntityTypeAccess) beType).getValidBlocks().stream() // access transform
                        .map(Block::asItem)
                        .filter(item -> item != Items.AIR)
                        .forEach(item -> addAccepted(upgrade, item));
            }
        });
    }

    private void addAccepted(PNCUpgrade upgrade, Item item) {
        acceptedUpgrades.computeIfAbsent(upgrade, k -> ConcurrentHashMap.newKeySet()).add(item);
    }
}
