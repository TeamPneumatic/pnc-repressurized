package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

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
     * @param te    The TileEntity at this x,y,z.
     * @return true if the coordinate should be tracked by this BlockTrackEntry.
     */
    boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te);

    /**
     * This method defines if the block should be updated by the server (each 5
     * seconds). This is specifically aimed at Tile Entities, as the server will
     * send an NBT packet. This method returns true at for instance Chests and
     * Mob Spawners, to get the inventory at the client side and the time to the
     * next spawn respectively.
     *
     * @param te The TileEntity at the currently checked location.
     * @return true if the Tile Entity should be updated, or false when it
     * doesn't have to.
     */
    boolean shouldBeUpdatedFromServer(TileEntity te);

    /**
     * The return of this method defines at how many tracked blocks of this type
     * the HUD should stop displaying text at the tracked blocks of this type.
     *
     * @return amount of blocks the HUD should stop displaying the block info.
     */
    int spamThreshold();

    /**
     * This method is called each render tick to retrieve the blocks additional
     * information. The method behaves the same as the addInformation method in
     * the Item class. This method only will be called if
     * shouldTrackWithThisEntry() returned true and the player hovers over the
     * coordinate.
     *
     * @param world    The world the block is in.
     * @param pos      The position the block is at.
     * @param te       The TileEntity at the x,y,z.
     * @param infoList The list of lines to display.
     */
    void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList);

    /**
     * This method is called when displaying the currently tracked blocks.
     * Will be tried to be mapped to the localization file first.
     *
     * @return the name of the group of this entry.
     */
    String getEntryName();
}
