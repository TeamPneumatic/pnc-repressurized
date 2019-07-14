package me.desht.pneumaticcraft.api.drone;

import net.minecraft.entity.Entity;

public interface IPathNavigator {
    boolean moveToXYZ(double x, double y, double z);

    boolean moveToEntity(Entity entity);

    boolean hasNoPath();

    boolean isGoingToTeleport();
}
