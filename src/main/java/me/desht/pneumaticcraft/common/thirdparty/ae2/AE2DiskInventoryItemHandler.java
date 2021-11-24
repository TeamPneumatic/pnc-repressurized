/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.ae2;

import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import me.desht.pneumaticcraft.api.item.IInventoryItem;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AE2DiskInventoryItemHandler implements IInventoryItem {
	private final ICellRegistry cellRegistry = AE2PNCAddon.api.registries().cell();
	private final IItemStorageChannel itemChannel = AE2PNCAddon.api.storage().getStorageChannel(IItemStorageChannel.class);

	@Override
	public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks){
		IMEInventoryHandler<IAEItemStack> cellInventoryHandler = cellRegistry.getCellInventory(stack, null, itemChannel);
		if (cellInventoryHandler != null) {
			IItemList<IAEItemStack> cellItemList = itemChannel.createList();
			cellInventoryHandler.getAvailableItems(cellItemList);
			for (IAEItemStack aeStack : cellItemList) {
				ItemStack st = aeStack.createItemStack();
				st.setCount((int) aeStack.getStackSize());
				curStacks.add(st);
			}
		}
	}
}
