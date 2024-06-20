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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Some helper methods to manage items which can store upgrades (Pneumatic Armor, Drones...)
 */
public class UpgradableItemUtils {
    public static final int UPGRADE_INV_SIZE = 9;

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
    public static void setUpgrades(ItemStack stack, IItemHandler handler) {
        stack.set(ModDataComponents.ITEM_UPGRADES, SavedUpgrades.fromItemHandler(handler));

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
        return stack.getOrDefault(ModDataComponents.ITEM_UPGRADES, SavedUpgrades.EMPTY).getUpgradeMap();
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
        return stack.getOrDefault(ModDataComponents.ITEM_UPGRADES, SavedUpgrades.EMPTY).getUpgradeCount(upgrade);
    }

    /**
     * Get the installed count for the given upgrades. Tiered upgrades count as multiple, e.g. one Tier 5
     * upgrade will return a result of 5 if queried for.
     *
     * @param stack the itemstack to check
     * @param upgradeList the upgrades to check for
     * @return a list of the upgrades installed with their count, in the same order as the upgrades which were passed to the method
     */
    public static IntList getUpgradeList(ItemStack stack, PNCUpgrade... upgradeList) {
        IntList res = new IntArrayList();
        var map = getUpgrades(stack);
        for (PNCUpgrade upgrade : upgradeList) {
            res.add((int) map.getOrDefault(upgrade, 0));
        }
        return IntLists.unmodifiable(res);
    }

    public static boolean hasCreativeUpgrade(ItemStack stack) {
        return getUpgradeCount(stack, ModUpgrades.CREATIVE.get()) > 0;
    }
}
