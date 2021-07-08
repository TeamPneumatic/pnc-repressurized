package me.desht.pneumaticcraft.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;

/**
 * To keep crafting widgets happy
 */
public class DummyContainer extends Container {
    public DummyContainer() {
        super(null, -1);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return false;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        // do nothing; default behaviour is to call detectAndSendChanges() which is unnecessary for drone
        // crafting purposes, and just wastes CPU cycles
    }
}
