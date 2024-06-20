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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Implement this interface and register it with {@link IClientArmorRegistry#registerBlockTrackEntry(ResourceLocation, Supplier)}.
 * Your implementation must provide a no-parameter constructor.
 * <p>
 * These trackers are singleton objects, so any instance data is in effect shared amongst all block positions for which
 * the tracker is applicable (in most cases, instance data should not be necessary).
 */
public interface IBlockTrackEntry {
    /**
     * This method should return true if the block at the coordinate checked is one that should be tracked. This is
     * often as simple as just checking the block type, but could be more complex for some trackers, i.e. checking
     * if a certain capability exists on the block entity. This gets called a lot when the block tracker is active,
     * so keep it as simple as possible.
     *
     * @param world the world
     * @param pos   the blockpos
     * @param state blockstate at this blockpos
     * @param te    the block entity at this blockpos (may be null)
     * @return true if this block should be tracked by this BlockTrackEntry
     */
    boolean shouldTrackWithThisEntry(Level world, BlockPos pos, BlockState state, BlockEntity te);

    /**
     * This method controls whether to send server update requests, at 3 second intervals while the player is
     * looking at the block. This is specifically aimed at Block Entities, as the server will send an NBT
     * update packet in response. Return an empty list if no updates are needed, otherwise a (possibly immutable)
     * list of the block positions for which updates should be requested (in most cases, only the BE's own block pos,
     * but potentially others for multiblocks like the vanilla double chest)
     *
     * @param te the block entity at the currently checked location, may be null
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
     * This method is only called if {@link #shouldTrackWithThisEntry(Level, BlockPos, BlockState, BlockEntity)}
     * returned true, and the player is currently focused on the block.
     *
     * @param world    the world
     * @param pos      the blockpos
     * @param te       the block entity at this blockpos (may be null)
     * @param face     the block face the player is looking at (null if player is not looking directly at the block)
     * @param infoList list of text to add information to
     */
    void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList);

    /**
     * Return a unique identifier for this block track entry. This is also used for translation key and keybind naming
     * purposes; see {@link me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler#getStringKey(ResourceLocation)}
     *
     * @return the ID of this entry
     */
    @Nonnull
    ResourceLocation getEntryID();

    /**
     * Convenience method: check if the given capability provider provides the given capability on any block face.
     *
     * @param provider the capability provider
     * @param cap the capability
     * @return true if the provider provides the capability on any face, including the null "face"
     */
    static boolean hasCapabilityOnAnyFace(BlockEntity provider, BlockCapability<?,Direction> cap) {
        for (Direction face : Direction.values()) {
            if (provider.getLevel().getCapability(cap, provider.getBlockPos(), provider.getBlockState(), provider, face) != null) {
                return true;
            }
        }
        return provider.getLevel().getCapability(cap, provider.getBlockPos(), provider.getBlockState(), provider, null) != null;
    }
}
