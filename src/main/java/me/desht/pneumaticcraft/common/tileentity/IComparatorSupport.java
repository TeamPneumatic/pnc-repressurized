package me.desht.pneumaticcraft.common.tileentity;

/**
 * Implement on tile entities whose block will supply a signal level to adjacent Comparators
 */
public interface IComparatorSupport {
    int getComparatorValue();
}
