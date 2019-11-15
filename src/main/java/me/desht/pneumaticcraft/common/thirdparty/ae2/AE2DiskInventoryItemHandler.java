package me.desht.pneumaticcraft.common.thirdparty.ae2;

import me.desht.pneumaticcraft.api.item.IInventoryItem;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AE2DiskInventoryItemHandler implements IInventoryItem{
	// TODO 1.14
//	private final ICellRegistry cellRegistry = AEApi.instance().registries().cell();
//	private final IItemStorageChannel itemChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
//
	@Override
	public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks){
//		IMEInventoryHandler<IAEItemStack> cellInventoryHandler = cellRegistry.getCellInventory(stack, null,	itemChannel);
//		if (cellInventoryHandler != null) {
//			IItemList<IAEItemStack> cellItemList = itemChannel.createList();
//			cellInventoryHandler.getAvailableItems(cellItemList);
//			for (IAEItemStack aeStack : cellItemList) {
//				ItemStack st = aeStack.createItemStack();
//				st.setCount((int) aeStack.getStackSize());
//				curStacks.add(st);
//			}
//		}
	}
}
