package me.desht.pneumaticcraft.common.util.upgrade;

import net.minecraftforge.items.IItemHandler;

@FunctionalInterface
public interface IUpgradeHolder {
    IItemHandler getUpgradeHandler();

    default void onUpgradesChanged() {}
}
