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

package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.client.gui.ItemSearcherScreen;
import me.desht.pneumaticcraft.common.inventory.slot.UnstackablePhantomSlot;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ItemSearcherMenu extends AbstractContainerMenu {
    private static final int SEARCH_ROWS = 6;
    private static final int SEARCH_COLS = 8;

    // list of ALL items in this container, updated by GuiSearcher#updateCreativeSearch
    public final NonNullList<ItemStack> itemList = NonNullList.create();
    private ItemSearcherScreen gui;

    @SuppressWarnings("unused")
    public ItemSearcherMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
        super(ModMenuTypes.ITEM_SEARCHER.get(), windowId);
    }

    public void init(ItemSearcherScreen gui) {
        this.gui = gui;

        for (int i = 0; i < SEARCH_ROWS; ++i) {
            for (int j = 0; j < SEARCH_COLS; ++j) {
                addSlot(new UnstackablePhantomSlot(gui.getInventory(), i * SEARCH_COLS + j, SEARCH_COLS + j * 18, 52 + i * 18));
            }
        }

        addSlot(new UnstackablePhantomSlot(gui.getInventory(), 48, 148, 12));
        scrollTo(0.0F);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.PNEUMATIC_HELMET.get();
    }

    /**
     * Updates the gui slots ItemStack's based on scroll position.
     * @param scrollPos scroll position, the range 0.0 - 1.0
     */
    public void scrollTo(double scrollPos) {
        int i = itemList.size() / SEARCH_COLS - SEARCH_ROWS + 1;
        int j = Math.max(0, (int) (scrollPos * i + 0.5D));

        for (int k = 0; k < SEARCH_ROWS; ++k) {
            for (int l = 0; l < SEARCH_COLS; ++l) {
                int idx = l + (k + j) * SEARCH_COLS;
                ItemStack stack = idx >= 0 && idx < itemList.size() ? itemList.get(idx) : ItemStack.EMPTY;
                gui.getInventory().setStackInSlot(l + k * SEARCH_COLS, stack);
            }
        }
    }

    public boolean hasMoreThan1PageOfItemsInList() {
        return itemList.size() > SEARCH_COLS * SEARCH_ROWS;
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int par1, int state, ItemStack par2ItemStack) {
        // override this to do nothing, as NEI tries to place items in this container which makes it crash.
    }

}
