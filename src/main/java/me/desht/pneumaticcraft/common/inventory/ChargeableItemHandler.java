package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ChargeableItemHandler extends ItemStackHandler {
    private static final int INVENTORY_SIZE = 9;

    private final ItemStack armorStack;
    private final TileEntityChargingStation te;

    public ChargeableItemHandler(TileEntityChargingStation te) {
        super(INVENTORY_SIZE);

        this.armorStack = te.getChargingItem();
        this.te = te;

        if (!hasInventory()) {
            createInventory();
        }
        loadInventory();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        saveInventory();
        te.markDirty();
    }

    private boolean hasInventory() {
        return NBTUtil.hasTag(armorStack, "UpgradeInventory");
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
//        NBTTagCompound inv = new NBTTagCompound();
//        inv.setTag("Items", serializeNBT());
        NBTUtil.setCompoundTag(armorStack, "UpgradeInventory", serializeNBT());
    }

    private void readFromNBT() {
        deserializeNBT(NBTUtil.getCompoundTag(armorStack, "UpgradeInventory"));
    }

}
