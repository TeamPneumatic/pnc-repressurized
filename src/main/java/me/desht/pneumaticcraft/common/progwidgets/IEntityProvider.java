package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.List;

public interface IEntityProvider {
    List<Entity> getValidEntities(World world);

    boolean isEntityValid(Entity entity);
}
