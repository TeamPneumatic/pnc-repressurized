package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerMinigunMagazine extends ContainerPneumaticBase<TileEntityBase> {
    private final ItemMinigun.MagazineHandler gunInv;

    public ContainerMinigunMagazine(int i, PlayerInventory playerInventory, @SuppressWarnings("unused") PacketBuffer buffer) {
        this(i, playerInventory);
    }

    public ContainerMinigunMagazine(int windowId, PlayerInventory playerInventory) {
        super(ModContainerTypes.MINIGUN_MAGAZINE, windowId, playerInventory);

        gunInv = ItemMinigun.getMagazine(playerInventory.player.getHeldItemMainhand());

        for (int i = 0; i < gunInv.getSlots(); i++) {
            addSlot(new SlotItemHandler(gunInv, i, 26 + (i % 2) * 18, 26 + (i / 2) * 18));
        }

        addPlayerSlots(playerInventory, 84);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);

        gunInv.save();
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
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
