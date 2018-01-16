package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.util.ITickable;

/**
 * Ticking tile entities should either extend this class, or implement ITickable themselves.
 * Note that the superclass, TileEntityBase, contains an implementation of update() which
 * will be used by default.
 */
public class TileEntityTickableBase extends TileEntityBase implements ITickable {
    public TileEntityTickableBase() {
        this(0);
    }

    public TileEntityTickableBase(int upgradeSize) {
        super(upgradeSize);
    }
}
