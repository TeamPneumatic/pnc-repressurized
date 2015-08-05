package pneumaticCraft.common.progwidgets;

import java.util.Set;

import net.minecraft.world.ChunkPosition;

public interface IAreaProvider{
    public void getArea(Set<ChunkPosition> area);
}
