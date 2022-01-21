/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Implement this class and register it with {@link IPneumaticHelmetRegistry#registerBlockTrackEntry(IBlockTrackEntry)}.
 * Your implementation must provide a no-parameter constructor. For every entity that's applicable for this definition,
 * an instance is created.
 */
public interface IBlockTrackEntry {
    /**
     * This method should return true if the coordinate checked is one that
     * should be tracked. Most entries will just return true when the blockID is
     * the one that they track.
     *
     * @param world The world that is examined.
     * @param pos   The position of the block examined.
     * @param state The block of the current coordinate. This will save you a
     *              call to World.getBlockState().
     * @param te    The TileEntity at this x,y,z  (may be null)
     * @return true if the coordinate should be tracked by this BlockTrackEntry.
     */
    boolean shouldTrackWithThisEntry(BlockGetter world, BlockPos pos, BlockState state, BlockEntity te);

    /**
     * This method controls whether the block should be updated by the server (at 5
     * second intervals). This is specifically aimed at Tile Entities, as the server will
     * send an NBT packet. Return an empty list if no updates are needed, otherwise a
     * list of the block positions for which updates should be sent (in most cases,
     * only the TE's own block pos, but potentially others for multiblocks like the
     * vanilla double chest)
     *
     * @param te the tile entity at the currently checked location, may be null
     * @return a list of the block positions for which update request packets should be sent
     */
    List<BlockPos> getServerUpdatePositions(@Nullable BlockEntity te);

    /**
     * The return of this method defines at how many tracked blocks of this type
     * the HUD should stop displaying text at the tracked blocks of this type.
     *
     * @return amount of blocks the HUD should stop displaying the block info.
     */
    int spamThreshold();

    /**
     * This method is called each client tick to retrieve the block's additional
     * information. The method behaves much the same as {@link net.minecraft.world.item.Item#appendHoverText(ItemStack, Level, List, TooltipFlag)}.
     * This method is only called if {@link #shouldTrackWithThisEntry(BlockGetter, BlockPos, BlockState, BlockEntity)}
     * returned true, and the player is curently focused on the block.
     *
     * @param world    The world the block is in.
     * @param pos      The position the block is at.
     * @param te       The TileEntity at the x,y,z (may be null)
     * @param face     The blockface the player is looking at (null if player is not looking directly at the block)
     * @param infoList The list of lines to display.
     */
    void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList);

    /**
     * Return a unique identifier for this block track entry. This is also used for translation key and keybind naming
     * purposes; see {@link me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler#getStringKey(ResourceLocation)}
     *
     * @return the ID of this entry
     */
    ResourceLocation getEntryID();

    /**
     * Convenience method: check if the given capability provider provides the given capability on any block face.
     *
     * @param provider the capability provider
     * @param cap the capability
     * @return true if the provider provides the capability on any face, including the null "face"
     */
    static boolean hasCapabilityOnAnyFace(ICapabilityProvider provider, Capability<?> cap) {
        for (Direction face : Direction.values()) {
            if (provider.getCapability(cap, face).isPresent()) return true;
        }
        return provider.getCapability(cap).isPresent();
    }
}
