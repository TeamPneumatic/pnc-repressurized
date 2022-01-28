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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implement this interface in tile entities which should store a camouflaged state.  The corresponding block should
 * be a subclass of {@link me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo}
 */
public interface CamouflageableBlockEntity {
    /**
     * Get the camouflage state; the blockstate which will be used to render this tile entity's block.
     *
     * @return the camouflage state, or null if the block should not be camouflaged
     */
    BlockState getCamouflage();

    /**
     * Set the camouflage for the tile entity.  The tile entity should sync this state to the client, and do
     * any necessary re-rendering of the block when the synced state changes.
     *
     * @param state the camo block state
     */
    void setCamouflage(BlockState state);

    /**
     * Convenience method: get the itemstack for the given block state.
     *
     * @param state the block state
     * @return an item for that block state
     */
    static ItemStack getStackForState(BlockState state) {
        return state == null ? ItemStack.EMPTY : new ItemStack(state.getBlock().asItem());
    }

    /**
     * Convenience method: sync camo state to client
     *
     * @param te the tile entity
     */
    static void syncToClient(TileEntityBase te) {
        if (te.getLevel() != null && !te.getLevel().isClientSide) {
            te.sendDescriptionPacket();
            te.setChanged();
        }
    }

    static BlockState readCamo(CompoundTag tag) {
        BlockState state = tag.contains("camoState", Tag.TAG_COMPOUND) ? NbtUtils.readBlockState(tag.getCompound("camoState")) : null;
        return state != null && state.getBlock() == Blocks.AIR ? null : state;
    }

    static void writeCamo(CompoundTag tag, BlockState state) {
        tag.put("camoState", NbtUtils.writeBlockState(state == null ? Blocks.AIR.defaultBlockState() : state));
    }
}
