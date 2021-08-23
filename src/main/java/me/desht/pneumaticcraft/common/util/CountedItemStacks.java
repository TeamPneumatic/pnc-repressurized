package me.desht.pneumaticcraft.common.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class CountedItemStacks extends Object2IntOpenCustomHashMap<ItemStack> {
    private final boolean canCoalesce;

    private static class ItemStackHashingStrategy implements Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack object) {
            return 31 * Item.getId(object.getItem()) + object.getDamageValue();
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && o1.getItem() == o2.getItem()
                    && o1.getDamageValue() == o2.getDamageValue();
            // ignore NBT for these purposes
        }
    }

    public CountedItemStacks() {
        super(new ItemStackHashingStrategy());
        canCoalesce = false;
    }

    public CountedItemStacks(IItemHandler handler) {
        super(handler.getSlots(), new ItemStackHashingStrategy());

        boolean canCoalesce = false;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int seenAlready = getInt(stack);
                if (seenAlready > 0 && seenAlready + stack.getCount() <= stack.getMaxStackSize()) {
                    canCoalesce = true;
                }
                put(stack, seenAlready + stack.getCount());
            }
        }
        this.canCoalesce = canCoalesce;
    }

    /**
     * Can the item handler that this collection of stacks came from be coalesced into fewer stacks?
     * @return true if it can be coalesced, false otherwise
     */
    public boolean canCoalesce() {
        return canCoalesce;
    }

    public NonNullList<ItemStack> coalesce() {
        NonNullList<ItemStack> coalesced = NonNullList.create();
        forEach((stack, amount) -> {
            while (amount > 0) {
                int toTake = Math.min(amount, stack.getMaxStackSize());
                amount -= toTake;
                coalesced.add(ItemHandlerHelper.copyStackWithSize(stack, toTake));
            }
        });
        return coalesced;
    }
}
