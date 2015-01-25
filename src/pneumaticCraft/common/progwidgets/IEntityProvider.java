package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface IEntityProvider{
    public List<Entity> getValidEntities(World world);

    public boolean isEntityValid(Entity entity);
}
