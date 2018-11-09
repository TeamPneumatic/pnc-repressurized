package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.ItemMinigun;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerMinigunMagazine extends ContainerPneumaticBase {

    private final ItemMinigun.MagazineHandler gunInv;

    public ContainerMinigunMagazine(EntityPlayer player) {
        super(null);

        gunInv = ItemMinigun.getMagazine(player.getHeldItemMainhand());

        for (int i = 0; i < gunInv.getSlots(); i++) {
            addSlotToContainer(new SlotInventoryLimiting(gunInv, i, 26 + (i % 2) * 18, 26 + (i / 2) * 18));
        }

        addPlayerSlots(player.inventory, 84);
    }

    public void updateMagazine(EntityPlayer player) {
        // called when the ammo in the magazine is externally modified
        ItemMinigun.MagazineHandler handler = ItemMinigun.getMagazine(player.getHeldItemMainhand());
        for (int i = 0; i < handler.getSlots(); i++) {
            if (ItemStack.areItemsEqualIgnoreDurability(gunInv.getStackInSlot(i), handler.getStackInSlot(i))) {
                gunInv.setStackInSlot(i, handler.getStackInSlot(i));
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        gunInv.save();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
