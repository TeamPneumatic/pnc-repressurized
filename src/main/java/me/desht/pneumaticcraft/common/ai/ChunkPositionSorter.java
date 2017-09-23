package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class ChunkPositionSorter implements Comparator<BlockPos> {

    private final double x, y, z;

    public ChunkPositionSorter(IDroneBase entity) {
        Vec3d vec = entity.getDronePos();
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    public ChunkPositionSorter(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compare(BlockPos c1, BlockPos c2) {
        return Double.compare(PneumaticCraftUtils.distBetweenSq(c1.getX(), c1.getY(), c1.getZ(), x, y, z), PneumaticCraftUtils.distBetweenSq(c2.getX(), c2.getY(), c2.getZ(), x, y, z));
    }
}
