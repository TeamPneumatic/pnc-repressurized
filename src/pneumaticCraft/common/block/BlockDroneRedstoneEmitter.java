package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.tileentity.TileEntityDroneRedstoneEmitter;

public class BlockDroneRedstoneEmitter extends BlockAir implements ITileEntityProvider{

    @Override
    public boolean canProvidePower(){
        return true;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
        return 0;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side){
        if(blockAccess instanceof World) {
            World world = (World)blockAccess;
            List<EntityDrone> drones = world.getEntitiesWithinAABB(EntityDrone.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
            int signal = 0;
            for(EntityDrone drone : drones) {
                signal = Math.max(signal, drone.getEmittingRedstone(ForgeDirection.getOrientation(side).getOpposite()));
            }
            return signal;

        } else {
            return 0;
        }
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_){
        return new TileEntityDroneRedstoneEmitter();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        super.breakBlock(world, x, y, z, block, meta);
        world.removeTileEntity(x, y, z);
    }
}
