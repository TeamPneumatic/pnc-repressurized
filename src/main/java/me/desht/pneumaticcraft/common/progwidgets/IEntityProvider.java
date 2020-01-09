package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.List;

public interface IEntityProvider {
    List<Entity> getValidEntities(World world);

    boolean isEntityValid(Entity entity);

    /**
     * Most, but not all, widgets have the entity filter attached as the second piece (area as the first)
     *
     * @return the 0-based position of the entity filter Text widget
     */
    default int getEntityFilterPosition() {
        return 1;
    }
}
