package me.desht.pneumaticcraft.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;

public class EntityDistanceComparator implements Comparator<Entity> {
    private final Vector3d origin;

    public EntityDistanceComparator(BlockPos pos) {
        origin = Vector3d.copyCentered(pos);
    }

    @Override
    public int compare(Entity e1, Entity e2) {
        return Double.compare(e1.getDistanceSq(origin), e2.getDistanceSq(origin));
    }
}
