package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

class SlotPneumaticArmor extends Slot {
    /**
     * The armor type that can be placed on that slot, it uses the same values
     * of armorType field on ItemArmor.
     */
    private final int armorType;  // 0 = boots, 1 = legs, 2 = chest, 3 = head
    private final EntityPlayer player;

    SlotPneumaticArmor(EntityPlayer player, InventoryPlayer inventoryPlayer, int index, int x, int y, int armorType) {
        super(inventoryPlayer, index, x, y);
        this.player = player;
        this.armorType = armorType;
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as
     * getInventoryStackLimit(), but 1 in the case of armor slots)
     */
    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for
     * the armor slots.
     */
    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        Item item = par1ItemStack.getItem();
        EntityEquipmentSlot eq = EntityEquipmentSlot.values()[armorType + 2];  // 0 & 1 are main & off hands
        return item.isValidArmor(par1ItemStack, eq, player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getSlotTexture() {
        return ItemArmor.EMPTY_SLOT_NAMES[armorType];
    }

}
