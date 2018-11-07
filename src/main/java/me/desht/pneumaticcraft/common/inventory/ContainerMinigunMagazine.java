package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.ItemMinigun;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerMinigunMagazine extends ContainerPneumaticBase {

    private final ItemMinigun.MagazineHandler gunInv;

    public ContainerMinigunMagazine(EntityPlayer player) {
        super(null);

        gunInv = ItemMinigun.getMagazine(player.getHeldItemMainhand());

        for (int i = 0; i < ItemMinigun.MAGAZINE_SIZE; i++) {
            addSlotToContainer(new SlotInventoryLimiting(gunInv, i, 26 + (i % 2) * 18, 26 + (i / 2) * 18));
        }

        addPlayerSlots(player.inventory, 84);
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
