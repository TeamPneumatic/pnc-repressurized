package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;

public class ContainerSecurityStationHacking extends ContainerPneumaticBase<TileEntitySecurityStation>{

    public ContainerSecurityStationHacking(InventoryPlayer inventoryPlayer, TileEntitySecurityStation te){
        super(te);

        //add the network slots
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 5; j++) {
                addSlotToContainer(new SlotUntouchable(te, j + i * 5, 13 + j * 31, 18 + i * 31));
            }
        }
    }

    /**
     * @param itemStack
     *            ItemStack to merge into inventory
     * @param start
     *            minimum slot to attempt fill
     * @param end
     *            maximum slot to attempt fill
     * @param backwards
     *            go backwards
     * @return true if stacks merged successfully public boolean
     *         mergeItemStack(itemStack, start, end, backwards)
     */

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){
        return null;
    }

}
