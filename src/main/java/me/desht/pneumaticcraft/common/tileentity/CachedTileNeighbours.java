package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;
import java.util.BitSet;
import java.util.EnumMap;

class CachedTileNeighbours {
    private final BitSet known = new BitSet(6);
    private final EnumMap<Direction,WeakReference<TileEntity>> neighbours = new EnumMap<>(Direction.class);
    private final TileEntity owner;

    public CachedTileNeighbours(TileEntity owner) {
        this.owner = owner;
        for (Direction d : DirectionUtil.VALUES) {
            neighbours.put(d, new WeakReference<>(null));
        }
    }

    public TileEntity getCachedNeighbour(Direction dir) {
        if (owner.getLevel() == null) return null;
        TileEntity res = known.get(dir.get3DDataValue()) ? neighbours.get(dir).get() : findNeighbour(dir);
        if (res != null && res.isRemoved()) {
            // shouldn't happen, but let's be defensive
            res = findNeighbour(dir);
        }
        return res;
    }

    private TileEntity findNeighbour(Direction dir) {
        BlockPos pos2 = owner.getBlockPos().relative(dir);
        TileEntity te = owner.getLevel().isAreaLoaded(pos2, 0) ? owner.getLevel().getBlockEntity(pos2) : null;
        neighbours.put(dir, new WeakReference<>(te));
        known.set(dir.get3DDataValue());
        return te;
    }

    public void purge() {
        known.clear();
    }
}
