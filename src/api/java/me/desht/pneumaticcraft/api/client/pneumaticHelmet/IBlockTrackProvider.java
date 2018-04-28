package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import java.util.List;

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
     * A object used to track a certain block pos.
     * @param world
     * @param pos
     * @param te
     * @return
     */
    IBlockTrackProviderInstance provideInstance(World world, BlockPos pos, TileEntity te);

    
    public static interface IBlockTrackProviderInstance {
        void toBytes(ByteBuf buf);
        
        void fromBytes(ByteBuf buf);
        
        void addInformation(List<String> infoList);
    }
}
