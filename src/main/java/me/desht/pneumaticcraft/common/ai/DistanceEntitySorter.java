package me.desht.pneumaticcraft.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;

public class DistanceEntitySorter implements Comparator<Entity> {
    private final IDroneBase drone;

    DistanceEntitySorter(IDroneBase drone) {
        this.drone = drone;
    }

    private int compare_internal(Entity entity1, Entity entity2) {
        Vector3d vec = drone.getDronePos();
        double d0 = vec.distanceToSqr(entity1.position());
        double d1 = vec.distanceToSqr(entity2.position());
        return Double.compare(d0, d1);
    }

    @Override
    public int compare(Entity p_compare_1_, Entity p_compare_2_) {
        return this.compare_internal(p_compare_1_, p_compare_2_);
    }
}
