package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.client.gui.GuiSearcher;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSearcher extends Container {
    private static final int SEARCH_ROWS = 6;
    private static final int SEARCH_COLS = 8;

    // list of ALL items in this container, updated by GuiSearcher#updateCreativeSearch
    public final NonNullList<ItemStack> itemList = NonNullList.create();
    private GuiSearcher gui;

    public void init(GuiSearcher gui) {
        this.gui = gui;
        for (int i = 0; i < SEARCH_ROWS; ++i) {
            for (int j = 0; j < SEARCH_COLS; ++j) {
                addSlotToContainer(new SlotItemHandler(gui.getInventory(), i * SEARCH_COLS + j, SEARCH_COLS + j * 18, 52 + i * 18));
            }
        }

        addSlotToContainer(new SlotItemHandler(gui.getInventory(), 48, 124, 25));
        scrollTo(0.0F);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Itemss.PNEUMATIC_HELMET;
    }

    /**
     * Updates the gui slots ItemStack's based on scroll position.
     * @param scrollPos scroll position, the range 0.0 - 1.0
     */
    public void scrollTo(float scrollPos) {
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
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

    @Override
    public void putStackInSlot(int par1, ItemStack par2ItemStack) {
        // override this to do nothing, as NEI tries to place items in this container which makes it crash.
    }

}
