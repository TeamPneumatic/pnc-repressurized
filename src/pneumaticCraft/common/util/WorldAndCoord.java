package pneumaticCraft.common.util;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class WorldAndCoord{

    public final IBlockAccess world;
    public final int x, y, z;

    public WorldAndCoord(IBlockAccess world, int x, int y, int z){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block getBlock(){
        return world.getBlock(x, y, z);
    }

    @Override
    public int hashCode(){
        return x * 8976890 + y * 981131 + z * 11;//this will cause a few hashcode collisions due to not including the IBlockAccess, but we can live with that.
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof WorldAndCoord) {
            WorldAndCoord wac = (WorldAndCoord)o;
            return wac.world == world && wac.x == x && wac.y == y && wac.z == z;
        } else {
            return false;
        }
    }
}
