package pneumaticCraft.common.util;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class WorldAndCoord{

    public final World world;
    public final int x, y, z;

    public WorldAndCoord(World world, int x, int y, int z){
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
        return x * 8976890 + y * 981131 + z * 11 + world.provider.dimensionId;
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
