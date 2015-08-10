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
import pneumaticCraft.common.item.ItemLogisticsFrame;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.common.tileentity.IGUIButtonSensitive;

public class ContainerLogistics extends ContainerPneumaticBase{
    public final SemiBlockLogistics logistics;
    private final boolean itemContainer;

    public ContainerLogistics(InventoryPlayer inventoryPlayer, SemiBlockLogistics logistics){
        super(null);
        itemContainer = logistics == null;
        if(itemContainer) {
            logistics = getLogistics(inventoryPlayer.player, inventoryPlayer.getCurrentItem());
        }
        this.logistics = logistics;
        if(logistics != null) {
            addSyncedFields(logistics);
            IInventory requests = logistics.getFilters();
            for(int y = 0; y < 3; y++) {
                for(int x = 0; x < 9; x++) {
                    addSlotToContainer(logistics.canFilterStack() ? new SlotPhantom(requests, y * 9 + x, x * 18 + 8, y * 18 + 29) : new SlotPhantomUnstackable(requests, y * 9 + x, x * 18 + 8, y * 18 + 29));
                }
            }

            // Add the player's inventory slots to the container
            for(int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
                for(int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                    addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 134 + inventoryRowIndex * 18));
                }
            }

            // Add the player's action bar slots to the container
            for(int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
                addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, 192));
            }
        }
    }

    public static SemiBlockLogistics getLogistics(EntityPlayer player, ItemStack itemRequester){
        if(itemRequester != null && itemRequester.getItem() instanceof ItemLogisticsFrame) {
            SemiBlockLogistics logistics = (SemiBlockLogistics)SemiBlockManager.getSemiBlockForKey(((ItemLogisticsFrame)itemRequester.getItem()).semiBlockId);
            logistics.initialize(player.worldObj, new ChunkPosition(0, 0, 0));
            logistics.onPlaced(player, itemRequester);
            return logistics;
        } else {
            return null;
        }
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(EntityPlayer player){
        if(itemContainer && logistics != null) {
            List<ItemStack> drops = new ArrayList<ItemStack>();
            logistics.addDrops(drops);
            NBTTagCompound settingTag = drops.get(0).getTagCompound();
            if(player.getCurrentEquippedItem().hasTagCompound()) {
                NBTTagCompound itemTag = player.getCurrentEquippedItem().getTagCompound();
                if(settingTag != null) {
                    itemTag.setTag("filters", settingTag.getTagList("filters", 10));
                    itemTag.setTag("fluidFilters", settingTag.getTagList("fluidFilters", 10));
                    itemTag.setBoolean("invisible", settingTag.getBoolean("invisible"));
                } else {
                    itemTag.removeTag("filters");
                    itemTag.removeTag("fluidFilters");
                    itemTag.removeTag("invisible");

                }
            } else {
                player.getCurrentEquippedItem().setTagCompound(settingTag);
            }
        }
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){
        super.handleGUIButtonPress(guiID, player);
        if(logistics instanceof IGUIButtonSensitive) {
            ((IGUIButtonSensitive)logistics).handleGUIButtonPress(guiID, player);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return logistics != null && !logistics.isInvalid();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){

        Slot var4 = (Slot)inventorySlots.get(par2);

        if(par2 >= 27 && var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            for(int i = 0; i < 27; i++) {
                Slot slot = (Slot)inventorySlots.get(i);
                if(!slot.getHasStack()) {
                    slot.putStack(var5.copy());
                    slot.getStack().stackSize = slot.getSlotStackLimit();
                    break;
                }
            }
        }

        return null;
    }

}
