package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ContainerAmadronAddTrade extends ContainerPneumaticBase {
//    private final InventoryBasic inv = new InventoryBasic("amadron", true, 2);
    private final ItemStackHandler inv = new ItemStackHandler(2);

    public ContainerAmadronAddTrade() {
        super(null);
        addSlotToContainer(new SlotUntouchable(inv, 0, 10, 90));
        addSlotToContainer(new SlotUntouchable(inv, 1, 99, 90));
    }

    public void setStack(int index, @Nonnull ItemStack stack) {
        inv.setStackInSlot(index, stack);
    }

    @Nonnull
    public ItemStack getStack(int index) {
        return inv.getStackInSlot(index);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getHeldItemMainhand().getItem() == Itemss.AMADRON_TABLET;
    }

    /**
     * args: slotID, itemStack to put in slot
     */
    @Override
    public void putStackInSlot(int slot, @Nonnull ItemStack stack) {
    }

//    /**
//     * places itemstacks in first x slots, x being aitemstack.lenght
//     */
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void putStacksInSlots(ItemStack[] p_75131_1_) {
//
//    }
}
