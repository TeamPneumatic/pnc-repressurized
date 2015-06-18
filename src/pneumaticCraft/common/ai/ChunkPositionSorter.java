package pneumaticCraft.common.ai;

import java.util.Comparator;

import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class ChunkPositionSorter implements Comparator<ChunkPosition>{

    private final double x, y, z;

    public ChunkPositionSorter(IDroneBase entity){
        Vec3 vec = entity.getPosition();
        x = vec.xCoord;
        y = vec.yCoord;
        z = vec.zCoord;
    }

    public ChunkPositionSorter(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compare(ChunkPosition c1, ChunkPosition c2){
        return Double.compare(PneumaticCraftUtils.distBetween(c1.chunkPosX, c1.chunkPosY, c1.chunkPosZ, x, y, z), PneumaticCraftUtils.distBetween(c2.chunkPosX, c2.chunkPosY, c2.chunkPosZ, x, y, z));
    }
}
