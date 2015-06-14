package pneumaticCraft.common.semiblock;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.tileentity.TileEntityBase;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.proxy.CommonProxy;

public class SemiBlockRequester extends SemiBlockLogistics implements ISpecificRequester{

    public static final String ID = "logisticsFrameRequester";
    private final IInventory requests = new InventoryBasic("requests", true, 45);

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        TileEntityBase.writeInventoryToNBT(tag, requests, "requests");
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        TileEntityBase.readInventoryFromNBT(tag, requests, "requests");
    }

    public IInventory getRequests(){
        return requests;
    }

    @Override
    public void addDrops(List<ItemStack> drops){
        super.addDrops(drops);
        for(int i = 0; i < requests.getSizeInventory(); i++) {
            if(requests.getStackInSlot(i) != null) {//Only set a tag when there are requests.
                ItemStack drop = drops.get(0);
                NBTTagCompound tag = new NBTTagCompound();
                TileEntityBase.writeInventoryToNBT(tag, requests, "requests");
                drop.setTagCompound(tag);
                break;
            }
        }
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack){
        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.hasKey("requests")) {
            TileEntityBase.readInventoryFromNBT(tag, requests, "requests");
        }
    }

    @Override
    public int getColor(){
        return 0xFF0000FF;
    }

    @Override
    public int amountRequested(ItemStack stack){
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if(totalRequestingAmount > 0) {
            IInventory inv = IOHelper.getInventoryForTE(getTileEntity());
            int count = 0;
            if(inv != null) {
                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack s = inv.getStackInSlot(i);
                    if(s != null && isItemEqual(s, stack)) {
                        count += s.stackSize;
                    }
                }
                for(ItemStack s : incomingStacks.keySet()) {
                    if(isItemEqual(s, stack)) {
                        count += s.stackSize;
                    }
                }
                int requested = Math.max(0, Math.min(stack.stackSize, totalRequestingAmount - count));
                return requested;
            }
        }
        return 0;
    }

    private int getTotalRequestedAmount(ItemStack stack){
        int requesting = 0;
        for(int i = 0; i < requests.getSizeInventory(); i++) {
            ItemStack requestingStack = requests.getStackInSlot(i);
            if(requestingStack != null && isItemEqual(stack, requestingStack)) {
                requesting += requestingStack.stackSize;
            }
        }
        return requesting;
    }

    private boolean isItemEqual(ItemStack s1, ItemStack s2){
        return s1.isItemEqual(s2);
    }

    @Override
    public int getPriority(){
        return 2;
    }

    @Override
    public boolean onRightClickWithConfigurator(EntityPlayer player){
        player.openGui(PneumaticCraft.instance, CommonProxy.GUI_ID_LOGISTICS_REQUESTER, world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        return true;
    }
}
