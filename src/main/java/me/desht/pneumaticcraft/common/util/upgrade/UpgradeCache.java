package me.desht.pneumaticcraft.common.util.upgrade;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UpgradeCache {
    private final int[] upgradeCount = new int[EnumUpgrade.values().length];
    private final Map<String,Integer> customUpgradeCount = new HashMap<>();
    private final IUpgradeHolder holder;
    private boolean isValid = false;
    private Direction ejectDirection;

    public UpgradeCache(IUpgradeHolder holder) {
        this.holder = holder;
    }

    /**
     * Mark the upgrade cache as invalid.  The next query for an upgrade will force a cache rebuild from the upgrade
     * inventory.
     */
    public void invalidate() {
        isValid = false;
    }

    public int getUpgrades(EnumUpgrade type) {
        validate();
        return upgradeCount[type.ordinal()];
    }

    public int getUpgrades(ItemStack stack) {
        validate();
        return customUpgradeCount.getOrDefault(makeUpgradeKey(stack), 0);
    }

    public Direction getEjectDirection() {
        return ejectDirection;
    }

    public void validate() {
        if (isValid) return;

        IItemHandler handler = holder.getUpgradeHandler();

        Arrays.fill(upgradeCount, 0);
        customUpgradeCount.clear();
        ejectDirection = null;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() instanceof ItemMachineUpgrade) {
                // native upgrade
                EnumUpgrade type = ((ItemMachineUpgrade) stack.getItem()).getUpgradeType();
                upgradeCount[type.ordinal()] += handler.getStackInSlot(i).getCount();
                if (type == EnumUpgrade.DISPENSER && stack.hasTag()) {
                    ejectDirection = Direction.byName(NBTUtil.getString(stack, ItemMachineUpgrade.NBT_DIRECTION));
                }
            } else if (!handler.getStackInSlot(i).isEmpty()) {
                // custom upgrade, maybe from another mod
                String key = makeUpgradeKey(stack);
                customUpgradeCount.put(key, customUpgradeCount.getOrDefault(key, 0) + stack.getCount());
            }
        }
        isValid = true;
        holder.onUpgradesChanged();
    }

    public IntArrayNBT toNBT() {
        validate();
        return new IntArrayNBT(upgradeCount);
    }

    private static String makeUpgradeKey(ItemStack stack) {
        return stack.getItem().getRegistryName().toString();
    }
}
