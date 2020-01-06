package me.desht.pneumaticcraft.common.util.upgrade;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;

public class UpgradeCache {
    private final int[] upgradeCount = new int[EnumUpgrade.values().length];
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

    public Direction getEjectDirection() {
        return ejectDirection;
    }

    public void validate() {
        if (isValid) return;

        IItemHandler handler = holder.getUpgradeHandler();

        Arrays.fill(upgradeCount, 0);
        ejectDirection = null;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() instanceof ItemMachineUpgrade) {
                ItemMachineUpgrade upgrade = ItemMachineUpgrade.of(stack);
                EnumUpgrade type = upgrade.getUpgradeType();
                if (upgradeCount[type.ordinal()] != 0) {
                    Log.warning("found upgrade " + type + " in multiple slots! Ignoring.");
                    continue;
                }
                upgradeCount[type.ordinal()] = stack.getCount() * upgrade.getTier();
                handleExtraData(stack, type);
            } else if (!stack.isEmpty()) {
                throw new IllegalStateException("found non-upgrade item in an upgrade handler! " + stack);
            }
        }
        isValid = true;
        holder.onUpgradesChanged();
    }

    private void handleExtraData(ItemStack stack, EnumUpgrade type) {
        if (type == EnumUpgrade.DISPENSER && stack.hasTag()) {
            ejectDirection = Direction.byName(NBTUtil.getString(stack, ItemMachineUpgrade.NBT_DIRECTION));
        }
    }

    public IntArrayNBT toNBT() {
        validate();
        return new IntArrayNBT(upgradeCount);
    }
}
