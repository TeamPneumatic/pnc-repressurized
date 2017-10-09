package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;

public class UpgradeCache {
    private final int upgradeCount[] = new int[IItemRegistry.EnumUpgrade.values().length];
    private final TileEntityBase te;

    public UpgradeCache(TileEntityBase te) {
        this.te = te;
    }

    public void cacheUpgrades() {
        Arrays.fill(upgradeCount, 0);
        IItemHandler inv = te.getUpgradesInventory();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).getItem() instanceof ItemMachineUpgrade) {
                int idx = ((ItemMachineUpgrade) inv.getStackInSlot(i).getItem()).getUpgradeType().ordinal();
                upgradeCount[idx] += inv.getStackInSlot(i).getCount();
            }
        }
    }

    public int getUpgrades(IItemRegistry.EnumUpgrade type) {
        return upgradeCount[type.ordinal()];
    }
}
