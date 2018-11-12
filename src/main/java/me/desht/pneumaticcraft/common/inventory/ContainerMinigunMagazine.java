package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

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

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        gunInv.save();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (clickType == ClickType.CLONE && dragType == 2) {
            // middle-click to lock a slot
            ItemStack gunStack = player.getHeldItemMainhand();
            if (gunStack.getItem() instanceof ItemMinigun) {
                int slot = NBTUtil.hasTag(gunStack, ItemMinigun.NBT_LOCKED_SLOT) ? NBTUtil.getInteger(gunStack, ItemMinigun.NBT_LOCKED_SLOT) : -1;
                if (slot == slotId) {
                    NBTUtil.removeTag(gunStack, ItemMinigun.NBT_LOCKED_SLOT);
                } else {
                    NBTUtil.setInteger(gunStack, ItemMinigun.NBT_LOCKED_SLOT, slotId);
                }
                if (player.world.isRemote) {
                    player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1.0f);
                }
            }
            return ItemStack.EMPTY;
        } else {
            return super.slotClick(slotId, dragType, clickType, player);
        }
    }
}
