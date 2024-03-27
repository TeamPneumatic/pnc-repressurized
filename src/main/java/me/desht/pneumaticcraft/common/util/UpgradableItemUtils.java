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

package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.upgrade.IUpgradeItem;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.common.upgrades.UpgradeCache;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Some helper methods to manage items which can store upgrades (Pneumatic Armor, Drones...)
 */
public class UpgradableItemUtils {
    public static final String NBT_CREATIVE = "CreativeUpgrade";
    public static final String NBT_UPGRADE_TAG = "UpgradeInventory";
    public static final int UPGRADE_INV_SIZE = 9;
    private static final String NBT_UPGRADE_CACHE_TAG = "UpgradeCache";

    /**
     * Add a standardized tooltip listing the installed upgrades in the given item.
     *
     * @param iStack the item
     * @param textList list of text to append tooltip too
     * @param flag tooltip flag
     */
    public static void addUpgradeInformation(ItemStack iStack, List<Component> textList, TooltipFlag flag) {
        Map<PNCUpgrade,Integer> upgrades = getUpgrades(iStack);
        if (upgrades.isEmpty()) {
            if (!ApplicableUpgradesDB.getInstance().getApplicableUpgrades(iStack.getItem()).isEmpty()) {
                textList.add(xlate("pneumaticcraft.gui.tooltip.upgrades.empty").withStyle(ChatFormatting.DARK_GREEN));
            }
        } else {
            textList.add(xlate("pneumaticcraft.gui.tooltip.upgrades.not_empty").withStyle(ChatFormatting.GREEN));
            List<ItemStack> stacks = new ArrayList<>();
            upgrades.forEach((upgrade, count) -> stacks.add(upgrade.getItemStack(count)));
            PneumaticCraftUtils.summariseItemStacks(textList, stacks, Component.literal(Symbols.BULLET + " ").withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    /**
     * Store a collection of upgrades into an item stack.  This should be only be used for items; don't use it
     * to manage saved upgrades on a dropped block which has serialized upgrade data.
     *
     * @param stack the stack
     * @param handler an ItemStackHandler holding upgrade items
     */
    public static void setUpgrades(ItemStack stack, ItemStackHandler handler) {
        stack.getOrCreateTag().put(NBT_UPGRADE_TAG, handler.serializeNBT());
        UpgradeCache cache = new UpgradeCache(() -> handler);
        Objects.requireNonNull(stack.getTag()).put(NBT_UPGRADE_CACHE_TAG, cache.toNBT());

        // in case volume upgrade count has changed...
        PNCCapabilities.getAirHandler(stack).ifPresent(h -> {
            if (h.getPressure() > h.maxPressure()) {
                int maxAir = (int)(h.getVolume() * h.maxPressure());
                h.addAir(maxAir - h.getAir());
            }
        });
    }

    /**
     * Retrieves the upgrades currently installed on the given itemstack. Upgrade tiers are taken into account, e.g.
     * a single tier 5 upgrade will have a count of 5.
     *
     * @param stack the itemstack to check
     * @return a map of upgrade->count
     */
    public static Map<PNCUpgrade,Integer> getUpgrades(ItemStack stack) {
        CompoundTag tag = getSerializedUpgrades(stack);
        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);
        ImmutableMap.Builder<PNCUpgrade,Integer> builder = ImmutableMap.builder();
        for (int i = 0; i < itemList.size(); i++) {
            ItemStack upgradeStack = ItemStack.of(itemList.getCompound(i));
            if (upgradeStack.getItem() instanceof IUpgradeItem upgradeItem) {
                builder.put(upgradeItem.getUpgradeType(), upgradeItem.getUpgradeTier() * upgradeStack.getCount());
            }
        }
        return builder.build();
    }

    /**
     * Get the installed count for the given upgrade. Tiered upgrades count as multiple, e.g. one Tier 5
     * upgrade will return a result of 5 if queried for.
     *
     * @param stack the itemstack to check
     * @param upgrade the upgrade to check for
     * @return number of upgrades installed
     */
    public static int getUpgradeCount(ItemStack stack, PNCUpgrade upgrade) {
        if (stack.getTag() != null) {
            validateUpgradeCache(stack);
            CompoundTag subTag = Objects.requireNonNull(stack.getTag()).getCompound(NBT_UPGRADE_CACHE_TAG);
            String key = PneumaticCraftUtils.modDefaultedString(upgrade.getId());
            return subTag.getInt(key);
        }
        return 0;
    }

    /**
     * Get the installed count for the given upgrades. Tiered upgrades count as multiple, e.g. one Tier 5
     * upgrade will return a result of 5 if queried for.
     *
     * @param stack the itemstack to check
     * @param upgradeList the upgrades to check for
     * @return number of upgrades installed, in the same order as the upgrades which were passed to the method
     */
    public static IntList getUpgradeList(ItemStack stack, PNCUpgrade... upgradeList) {
        IntList res = new IntArrayList();
        if (stack.getTag() != null) {
            validateUpgradeCache(stack);
            CompoundTag subTag = stack.getTag().getCompound(NBT_UPGRADE_CACHE_TAG);
            for (PNCUpgrade upgrade : upgradeList) {
                String key = PneumaticCraftUtils.modDefaultedString(upgrade.getId());
                res.add(subTag.getInt(key));
            }
        }
        return IntLists.unmodifiable(res);
    }

    public static boolean hasCreativeUpgrade(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean(UpgradableItemUtils.NBT_CREATIVE);
    }

    private static void validateUpgradeCache(ItemStack stack) {
        if (Objects.requireNonNull(stack.getTag()).contains(NBT_UPGRADE_TAG) && !stack.getTag().contains(NBT_UPGRADE_CACHE_TAG)) {
            // should not normally get here; the quick-access cache should already be built by setUpgrades()
            ItemStackHandler handler = new ItemStackHandler(UPGRADE_INV_SIZE);
            CompoundTag tag = getSerializedUpgrades(stack);
            if (!tag.isEmpty()) handler.deserializeNBT(tag);
            UpgradeCache cache = new UpgradeCache(() -> handler);
            Objects.requireNonNull(stack.getTag()).put(NBT_UPGRADE_CACHE_TAG, cache.toNBT());
        }
    }

    private static CompoundTag getSerializedUpgrades(ItemStack stack) {
        if (stack.getTag() == null) return new CompoundTag();
        if (stack.getTag().contains(NBTKeys.BLOCK_ENTITY_TAG)) {
            return Objects.requireNonNull(stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG)).getCompound(NBT_UPGRADE_TAG);
        } else {
            return stack.getTag().getCompound(NBT_UPGRADE_TAG);
        }
    }
}
