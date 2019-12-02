package me.desht.pneumaticcraft.common.inventory.handler;

import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class RenderedItemStackHandler extends BaseItemStackHandler {
    private ItemEntity renderItem = null;

    public RenderedItemStackHandler(TileEntity te) {
        super(te,1);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        if (te.getWorld().isRemote) {
            ItemStack stack = getStackInSlot(0);
            if (stack.isEmpty()) {
                renderItem = null;
            } else {
                renderItem = new ItemEntity(EntityType.ITEM, te.getWorld());
                renderItem.setItem(stack);
                renderItem.makeFakeItem();
            }
        }
    }

    private ItemEntity getRenderItem() {
        return renderItem;
    }

    public static ItemEntity getItemToRender(TileEntityBase te) {
        if (te.getPrimaryInventory() instanceof RenderedItemStackHandler) {
            return ((RenderedItemStackHandler) te.getPrimaryInventory()).getRenderItem();
        } else {
            return null;
        }
    }
}
