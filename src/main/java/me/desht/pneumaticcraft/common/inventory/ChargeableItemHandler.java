package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.tileentity.FilteredItemStackHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.item.ItemStack;

public class ChargeableItemHandler extends FilteredItemStackHandler {
    private static final int INVENTORY_SIZE = 9;

    public static final String NBT_UPGRADE_TAG = "UpgradeInventory";

    private final ItemStack chargingItem;

    public ChargeableItemHandler(TileEntityChargingStation te) {
        super(te, INVENTORY_SIZE);

        this.chargingItem = te.getChargingItem();

        if (!hasInventory()) {
            createInventory();
        }
        loadInventory();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        saveInventory();
    }

    private boolean hasInventory() {
        return NBTUtil.hasTag(chargingItem, NBT_UPGRADE_TAG);
    }

    private void createInventory() {
        writeToNBT();
    }

    private void loadInventory() {
        readFromNBT();
    }

    public void saveInventory() {
        writeToNBT();
    }

    public void writeToNBT() {
        NBTUtil.setCompoundTag(chargingItem, NBT_UPGRADE_TAG, serializeNBT());
    }

    private void readFromNBT() {
        deserializeNBT(NBTUtil.getCompoundTag(chargingItem, NBT_UPGRADE_TAG));
    }

    @Override
    public boolean test(Integer integer, ItemStack itemStack) {
        if (itemStack.isEmpty()) return true;

        if (chargingItem.getItem() instanceof IUpgradeAcceptor) {
            return ((IUpgradeAcceptor) chargingItem.getItem()).getApplicableUpgrades().contains(itemStack.getItem());
        }
        return false;
    }
}
