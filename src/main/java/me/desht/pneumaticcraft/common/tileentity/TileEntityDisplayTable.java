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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityDisplayTable extends TileEntityBase implements IComparatorSupport {
    private final DisplayItemHandler inventory = new DisplayItemHandler(this, 1);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);
    public ItemStack displayedStack = ItemStack.EMPTY;

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
    public CompoundNBT save(CompoundNBT tag) {
        tag.put("Items", inventory.serializeNBT());

        return super.save(tag);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        displayedStack = inventory.getStackInSlot(0);
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        tag.put("Item", displayedStack.save(new CompoundNBT()));
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        displayedStack = ItemStack.of(tag.getCompound("Item"));
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
                displayedStack = getStackInSlot(0);
                if (!level.isClientSide) sendDescriptionPacket();
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }
}
