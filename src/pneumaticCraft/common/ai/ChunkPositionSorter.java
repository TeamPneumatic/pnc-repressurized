package pneumaticCraft.common.ai;

import java.util.Comparator;

import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class ChunkPositionSorter implements Comparator{

    private final double x, y, z;

    public ChunkPositionSorter(IDroneBase entity){
        Vec3 vec = entity.getPosition();
        x = vec.xCoord;
        y = vec.yCoord;
        z = vec.zCoord;
    }

    @Override
    public int compare(Object arg0, Object arg1){
        ChunkPosition c1 = (ChunkPosition)arg0;
        ChunkPosition c2 = (ChunkPosition)arg1;
        return Double.compare(PneumaticCraftUtils.distBetween(c1.chunkPosX, c1.chunkPosY, c1.chunkPosZ, x, y, z), PneumaticCraftUtils.distBetween(c2.chunkPosX, c2.chunkPosY, c2.chunkPosZ, x, y, z));
    }
}
