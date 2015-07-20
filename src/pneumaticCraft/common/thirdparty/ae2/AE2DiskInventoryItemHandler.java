package pneumaticCraft.common.thirdparty.ae2;

import java.util.List;

import net.minecraft.item.ItemStack;
import pneumaticCraft.api.item.IInventoryItem;
import appeng.api.AEApi;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class AE2DiskInventoryItemHandler implements IInventoryItem{
    private final ICellRegistry cellRegistry = AEApi.instance().registries().cell();
    private final IStorageHelper storageHelper = AEApi.instance().storage();

    @Override
    public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks){
        IMEInventoryHandler<IAEItemStack> cellInventoryHandler = cellRegistry.getCellInventory(stack, null, StorageChannel.ITEMS);
        if(cellInventoryHandler != null) {
            IItemList<IAEItemStack> cellItemList = storageHelper.createItemList();
            cellInventoryHandler.getAvailableItems(cellItemList);
            for(IAEItemStack aeStack : cellItemList) {
                ItemStack st = aeStack.getItemStack();
                st.stackSize = (int)aeStack.getStackSize();//Do another getStacksize, as above retrieval caps to 64.
                curStacks.add(st);
            }
        }
    }
}
