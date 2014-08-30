package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public interface IEntityProvider{
    public List<EntityLivingBase> getValidEntities(World world);
}
