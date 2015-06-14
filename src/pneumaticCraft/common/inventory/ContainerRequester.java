package pneumaticCraft.common.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.semiblock.SemiBlockRequester;

public class ContainerRequester extends ContainerPneumaticBase{
    public final SemiBlockRequester requester;
    private final boolean itemContainer;

    public ContainerRequester(InventoryPlayer inventoryPlayer, SemiBlockRequester requester){
        super(null);
        itemContainer = requester == null;
        if(itemContainer) {
            requester = getItemRequester(inventoryPlayer.player, inventoryPlayer.getCurrentItem());
        }
        this.requester = requester;
        IInventory requests = requester.getRequests();
        for(int y = 0; y < 5; y++) {
            for(int x = 0; x < 9; x++) {
                addSlotToContainer(new SlotPhantom(requests, y * 9 + x, x * 18 + 8, y * 18 + 18));
            }
        }

        // Add the player's inventory slots to the container
        for(int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for(int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 123 + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for(int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, 181));
        }
    }

    public static SemiBlockRequester getItemRequester(EntityPlayer player, ItemStack itemRequester){
        SemiBlockRequester requester = new SemiBlockRequester();
        requester.initialize(player.worldObj, new ChunkPosition(0, 0, 0));
        requester.onPlaced(player, itemRequester);
        return requester;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(EntityPlayer player){
        if(itemContainer) {
            List<ItemStack> drops = new ArrayList<ItemStack>();
            requester.addDrops(drops);
            NBTTagCompound settingTag = drops.get(0).getTagCompound();
            if(player.getCurrentEquippedItem().hasTagCompound()) {
                if(settingTag != null) {
                    player.getCurrentEquippedItem().getTagCompound().setTag("requests", settingTag.getTagList("requests", 10));
                } else {
                    player.getCurrentEquippedItem().getTagCompound().removeTag("requests");
                }
            } else {
                player.getCurrentEquippedItem().setTagCompound(settingTag);
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return !requester.isInvalid();
    }

}
