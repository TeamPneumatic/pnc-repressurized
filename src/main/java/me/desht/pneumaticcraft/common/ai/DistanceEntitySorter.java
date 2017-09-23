package me.desht.pneumaticcraft.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class DistanceEntitySorter implements Comparator<Entity> {
    private final IDroneBase drone;

    public DistanceEntitySorter(IDroneBase drone) {
        this.drone = drone;
    }

    public int compare_internal(Entity entity1, Entity entity2) {
        Vec3d vec = drone.getDronePos();
        double d0 = vec.squareDistanceTo(entity1.getPositionVector());
        double d1 = vec.squareDistanceTo(entity2.getPositionVector());
        return d0 < d1 ? -1 : d0 > d1 ? 1 : 0;
    }

    @Override
    public int compare(Entity p_compare_1_, Entity p_compare_2_) {
        return this.compare_internal(p_compare_1_, p_compare_2_);
    }
}
