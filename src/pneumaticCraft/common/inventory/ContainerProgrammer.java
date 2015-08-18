package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.common.network.PacketSendNBTPacket;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;

public class ContainerProgrammer extends ContainerPneumaticBase<TileEntityProgrammer>{

    public ContainerProgrammer(InventoryPlayer inventoryPlayer, TileEntityProgrammer te){
        super(te);
        // add the upgrade slots
        addSlotToContainer(new Slot(te, 0, 326, 15){
            @Override
            public boolean isItemValid(ItemStack stack){
                return isProgrammableItem(stack);
            }
        });

        // Add the player's inventory slots to the container
        for(int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
            for(int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 95 + inventoryColumnIndex * 18, 174 + inventoryRowIndex * 18));
            }
        }

        // Add the player's action bar slots to the container
        for(int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
            addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 95 + actionBarSlotIndex * 18, 232));
        }
    }

    private static boolean isProgrammableItem(ItemStack stack){
        if(stack == null) return false;
        return stack.getItem() instanceof IProgrammable && ((IProgrammable)stack.getItem()).canProgram(stack);
    }

    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();
        if(te.getWorldObj().getTotalWorldTime() % 20 == 0) {
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity neighbor = te.getWorldObj().getTileEntity(te.xCoord + d.offsetX, te.yCoord + d.offsetY, te.zCoord + d.offsetZ);
                if(neighbor instanceof IInventory) {
                    sendToCrafters(new PacketSendNBTPacket(neighbor));
                }
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){

        ItemStack var3 = null;
        Slot var4 = (Slot)inventorySlots.get(par2);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(par2 == 0) {
                if(!mergeItemStack(var5, 1, 36, false)) return null;
                var4.onSlotChange(var5, var3);
            } else if(isProgrammableItem(var3)) {
                if(!mergeItemStack(var5, 0, 1, false)) return null;
                var4.onSlotChange(var5, var3);
            }
            if(var5.stackSize == 0) {
                var4.putStack((ItemStack)null);
            } else {
                var4.onSlotChanged();
            }

            if(var5.stackSize == var3.stackSize) return null;

            var4.onPickupFromSlot(par1EntityPlayer, var5);
        }

        return var3;
    }

}
