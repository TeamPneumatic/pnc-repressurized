package me.desht.pneumaticcraft.common.util;

import net.minecraft.core.Vec3i;

/**
 * A data class which stores all the data relevant to a block entities bounding blocks
 */
public class BoundingBlockEntityData {
    public Vec3i offsetFromMain = new Vec3i(0, 0, 0);
    public boolean boundingPlaced = false;
    public boolean boundingRemoved = false;
    public boolean mainBlockRemovalLock = false;
}
