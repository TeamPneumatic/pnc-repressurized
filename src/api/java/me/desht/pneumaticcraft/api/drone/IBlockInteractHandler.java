package me.desht.pneumaticcraft.api.drone;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * This represents the drone's AI interaction handler. DON'T IMPLEMENT THIS, just use it when passed to
 * {@link ICustomBlockInteract#doInteract(BlockPos, IDrone, IBlockInteractHandler, boolean)}.
 */
public interface IBlockInteractHandler {

    /**
     * Returns a boolean[6] of all sides. When true, this side is accessible.  The sides are in order D,U,N,S,W,E;
     * you can use {@link Direction#byIndex(int)} with an index into this array to get the facing direction.
     * <p>
     * See also {@link #isSideAccessible(Direction)}
     *
     * @return an array of booleans indexed by the Direction index
     */
    boolean[] getSides();

    /**
     * This is controlled by players setting the "Use Count" checkbox in the widget GUI.  When true, the drone is trying
     * to transfer a specific quantity of the resource in question (items/fluid/energy...).
     *
     * @return true if a specific quantity should be imported/exported
     */
    boolean useCount();

    /**
     * When {@link #useCount()} returns true, this should be called to notify the amount actually transferred.
     *
     * @param count the amount to notify
     */
    void decreaseCount(int count);

    /**
     * When {@link #useCount()} returns true, transfer only up to this much of the resource in question.
     *
     * @return the maximum amount to attempt to transfer
     */
    int getRemainingCount();

    /**
     * When invoked, the drone will abort searching the area. Could be used to abort early when full of RF energy for
     * example, when importing RF.  (It's useless to search any further)
     */
    void abort();

    /**
     * Convenience method to check if a given face is accessible.
     *
     * @param face the face to check
     * @return true if the face is accessible
     */
    default boolean isSideAccessible(Direction face) {
        return getSides()[face.get3DDataValue()];
    }
}
