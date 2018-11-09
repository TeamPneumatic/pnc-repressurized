package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.tileentity.FilteredItemStackHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.item.ItemStack;

public class ChargeableItemHandler extends FilteredItemStackHandler {
    private static final int INVENTORY_SIZE = 9;
    public static final String NBT_UPGRADE_TAG = "UpgradeInventory";

    public ChargeableItemHandler(TileEntityChargingStation te) {
        super(te, INVENTORY_SIZE);

        if (!NBTUtil.hasTag(getChargingStack(), NBT_UPGRADE_TAG)) {
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
        return ((TileEntityChargingStation) te).getChargingItem();
    }

    public void writeToNBT() {
        NBTUtil.setCompoundTag(getChargingStack(), NBT_UPGRADE_TAG, serializeNBT());
    }

    private void readFromNBT() {
        deserializeNBT(NBTUtil.getCompoundTag(getChargingStack(), NBT_UPGRADE_TAG));
    }

    @Override
    public boolean test(Integer integer, ItemStack itemStack) {
        if (itemStack.isEmpty()) return true;

        if (getChargingStack().getItem() instanceof IUpgradeAcceptor) {
            return ((IUpgradeAcceptor) getChargingStack().getItem()).getApplicableUpgrades().contains(itemStack.getItem());
        }
        return false;
    }
}
