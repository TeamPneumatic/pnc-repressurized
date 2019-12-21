package me.desht.pneumaticcraft.common.inventory.handler;

import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.item.ItemStack;

public class ChargeableItemHandler extends BaseItemStackHandler {
    public ChargeableItemHandler(TileEntityChargingStation te) {
        super(te, UpgradableItemUtils.UPGRADE_INV_SIZE);

        if (!NBTUtil.hasTag(getChargingStack(), UpgradableItemUtils.NBT_UPGRADE_TAG)) {
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
        deserializeNBT(NBTUtil.getCompoundTag(getChargingStack(), UpgradableItemUtils.NBT_UPGRADE_TAG));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack itemStack) {
        if (itemStack.isEmpty()) return true;

        return getChargingStack().getItem() instanceof IUpgradeAcceptor
                && ((IUpgradeAcceptor) getChargingStack().getItem()).getApplicableUpgrades().contains(itemStack.getItem());
    }
}
