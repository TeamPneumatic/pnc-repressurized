package me.desht.pneumaticcraft.common.inventory.handler;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import net.minecraft.item.ItemStack;

public class ChargeableItemHandler extends BaseItemStackHandler {
    public ChargeableItemHandler(TileEntityChargingStation te) {
        super(te, UpgradableItemUtils.UPGRADE_INV_SIZE);

        if (!NBTUtils.hasTag(getChargingStack(), UpgradableItemUtils.NBT_UPGRADE_TAG)) {
            writeToNBT();
        }
        readFromNBT();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        writeToNBT();
    }

    private ItemStack getChargingStack() {
        return ((TileEntityChargingStation) te).getChargingStack();
    }

    public void writeToNBT() {
        UpgradableItemUtils.setUpgrades(getChargingStack(), this);
    }

    private void readFromNBT() {
        deserializeNBT(NBTUtils.getCompoundTag(getChargingStack(), UpgradableItemUtils.NBT_UPGRADE_TAG));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack itemStack) {
        return itemStack.isEmpty() || isApplicable(itemStack) && isUnique(slot, itemStack);
    }

    private boolean isUnique(int slot, ItemStack stack) {
        for (int i = 0; i < getSlots(); i++) {
            if (i != slot && EnumUpgrade.from(stack) == EnumUpgrade.from(getStackInSlot(i))) return false;
        }
        return true;
    }

    private boolean isApplicable(ItemStack stack) {
        return ApplicableUpgradesDB.getInstance().getMaxUpgrades(getChargingStack().getItem(), EnumUpgrade.from(stack)) > 0;
    }
}
