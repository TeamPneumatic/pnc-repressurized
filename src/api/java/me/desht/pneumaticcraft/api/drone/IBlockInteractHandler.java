package me.desht.pneumaticcraft.api.drone;

/**
 * DON'T IMPLEMENT, just use
 */
public interface IBlockInteractHandler {

    /**
     * Returns a boolean[6] of all sides. when true, this side is accessible.  The sides are in order D,U,N,S,W,E,
     * same as for EnumFacing.  You can use {@link net.minecraft.util.EnumFacing#getFront(int)} with an index into this
     * array to get the facing direction.
     *
     * @return
     */
    boolean[] getSides();

    boolean useCount();

    void decreaseCount(int count);

    int getRemainingCount();

    /**
     * When invoked, the drone will abort searching the area. Could be used to abort early when full of RF energy for example, when importing RF.
     * (It's useless to search any further)
     */
    void abort();

}
