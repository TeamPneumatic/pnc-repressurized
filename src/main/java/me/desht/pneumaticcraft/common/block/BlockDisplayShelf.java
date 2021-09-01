package me.desht.pneumaticcraft.common.block;

import net.minecraft.util.math.shapes.VoxelShape;

public class BlockDisplayShelf extends BlockDisplayTable {
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[16];

    @Override
    public double getTableHeight() {
        return 0.5d;
    }

    @Override
    protected VoxelShape[] getShapeCache() {
        return SHAPE_CACHE;
    }

    @Override
    protected boolean shelfLegs() {
        return true;
    }
}
