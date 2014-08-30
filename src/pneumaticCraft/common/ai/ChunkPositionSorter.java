package pneumaticCraft.common.ai;

import java.util.Comparator;

import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.PneumaticCraftUtils;

public class ChunkPositionSorter implements Comparator{

    private final Entity entity;

    public ChunkPositionSorter(Entity entity){
        this.entity = entity;
    }

    @Override
    public int compare(Object arg0, Object arg1){
        ChunkPosition c1 = (ChunkPosition)arg0;
        ChunkPosition c2 = (ChunkPosition)arg1;
        return Double.compare(PneumaticCraftUtils.distBetween(c1.chunkPosX, c1.chunkPosY, c1.chunkPosZ, entity.posX, entity.posY, entity.posZ), PneumaticCraftUtils.distBetween(c2.chunkPosX, c2.chunkPosY, c2.chunkPosZ, entity.posX, entity.posY, entity.posZ));
    }
}
