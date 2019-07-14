package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerInventorySearcher extends Container {
    public ContainerInventorySearcher(int windowId, PlayerInventory inv, PacketBuffer data) {
        super(ModContainerTypes.INVENTORY_SEARCHER, windowId);

        // Add the player's inventory slots to the container
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 48 + row * 18));
            }
        }

        // Add the player's action bar slots to the container
        for (int slot = 0; slot < 9; ++slot) {
            addSlot(new Slot(inv, slot, 8 + slot * 18, 106));
        }
    }

    public void init(IItemHandler inv) {
        addSlot(new SlotItemHandler(inv, 0, 80, 23));
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

    @Override
    public void putStackInSlot(int par1, @Nonnull ItemStack par2ItemStack) {
        //override this to do nothing, as NEI tries to place items in this container which makes it crash.
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity entityplayer) {
        return true;
    }

}
