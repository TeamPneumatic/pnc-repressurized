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

package me.desht.pneumaticcraft.common.block.entity.utility;

import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IComparatorSupport;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class DisplayTableBlockEntity extends AbstractPneumaticCraftBlockEntity implements IComparatorSupport {
    private final DisplayItemHandler inventory = new DisplayItemHandler(this, 1);
    public ItemStack displayedStack = ItemStack.EMPTY;

    public DisplayTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.DISPLAY_TABLE.get(), pos, state);
    }

    DisplayTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return inventory;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        displayedStack = inventory.getStackInSlot(0);
    }

    @Override
    public void writeToPacket(CompoundTag tag) {
        super.writeToPacket(tag);

        tag.put("Item", displayedStack.save(new CompoundTag()));
    }

    @Override
    public void readFromPacket(CompoundTag tag) {
        super.readFromPacket(tag);

        displayedStack = ItemStack.of(tag.getCompound("Item"));
    }

    @Override
    public int getComparatorValue() {
        return inventory.getStackInSlot(0).isEmpty() ? 0 : 15;
    }

    class DisplayItemHandler extends BaseItemStackHandler {
        DisplayItemHandler(DisplayTableBlockEntity te, int size) {
            super(te, size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (slot == 0) {
                displayedStack = getStackInSlot(0);
                if (!nonNullLevel().isClientSide) sendDescriptionPacket();
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }
}
