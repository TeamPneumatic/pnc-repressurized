package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import java.util.List;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;

/**
 * To be implemented for custom handling of {@link IBlockTrackEntry}.
 */
public interface IBlockTrackProvider {
    
    /**
     * Return true for the types of block tracker this provider is applicable.
     * @param blockTracker
     * @return
     */
    boolean canHandle(IBlockTrackEntry blockTracker);
    
    /**
     * An object used to track a certain block pos.
     * @param world
     * @param pos
     * @param te
     * @param blockTrackers the block trackers that are tracked by this provider for the given block. This possibly affects what
     * needs to be synced, and what to append in addInformation.
     * @return
     */
    IBlockTrackHandler provideHandler(World world, BlockPos pos, TileEntity te, Set<IBlockTrackEntry> blockTrackers);

    
    public static interface IBlockTrackHandler {
        /**
         * Called from the server-side netty network thread.
         * @param buf
         */
        void toBytes(ByteBuf buf);
        
        /**
         * Called from the client thread (not netty's network thread)
         * @param buf
         */
        void fromBytes(ByteBuf buf);
        
        void addInformation(List<String> infoList, Set<IBlockTrackEntry> blockTrackers);
    }
}
