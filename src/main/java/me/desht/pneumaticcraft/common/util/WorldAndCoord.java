package me.desht.pneumaticcraft.common.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WorldAndCoord {

    public final IBlockAccess world;
    public final BlockPos pos;

    public WorldAndCoord(IBlockAccess world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public Block getBlock() {
        return world.getBlockState(pos).getBlock();
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WorldAndCoord) {
            WorldAndCoord wac = (WorldAndCoord) o;
            return wac.world == world && wac.pos.equals(pos);
        } else {
            return false;
        }
    }
}
