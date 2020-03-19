package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityDisplayTable extends TileEntityBase implements IComparatorSupport {
    private final DisplayItemHandler inventory = new DisplayItemHandler(this, 1);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);
    public int itemId;

    public TileEntityDisplayTable() {
        super(ModTileEntities.DISPLAY_TABLE.get());
    }

    TileEntityDisplayTable(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("Items", inventory.serializeNBT());

        return super.write(tag);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        itemId = Item.getIdFromItem(inventory.getStackInSlot(0).getItem());
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        tag.putInt("ItemId", itemId);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        itemId = tag.getInt("ItemId");
    }

    @Override
    public int getComparatorValue() {
        return inventory.getStackInSlot(0).isEmpty() ? 0 : 15;
    }

    class DisplayItemHandler extends BaseItemStackHandler {
        DisplayItemHandler(TileEntityDisplayTable te, int size) {
            super(te, size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (slot == 0) {
                itemId = Item.getIdFromItem(getStackInSlot(0).getItem());
                if (!world.isRemote) sendDescriptionPacket();
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }
}
