package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class ChunkPositionSorter implements Comparator<BlockPos> {
    private static final double EPSILON = 0.01;

    private final double x, y, z;

    ChunkPositionSorter(IDroneBase entity) {
        Vec3d vec = entity.getDronePos();

        // work from middle of the block the drone is in (try to minimize inconsistency)
        x = Math.floor(vec.x) + 0.5;
        y = Math.floor(vec.y) + 0.5;
        z = Math.floor(vec.z) + 0.5;
    }

    public ChunkPositionSorter(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compare(BlockPos c1, BlockPos c2) {
        double d = PneumaticCraftUtils.distBetweenSq(c1.getX(), c1.getY(), c1.getZ(), x, y, z) - PneumaticCraftUtils.distBetweenSq(c2.getX(), c2.getY(), c2.getZ(), x, y, z);
        if (Math.abs(d) < EPSILON) {
            return c1.compareTo(c2);
        } else {
            return d < 0 ? -1 : 1;
        }
    }
}
