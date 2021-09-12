package me.desht.pneumaticcraft.api.semiblock;

import net.minecraft.util.Direction;

/**
 * Represents a semiblock which sits on the side of an actual block, rather than occupying the same space.
 * E.g. Transfer Gadgets and Logistics Frames are directional, but Crop Supports and Heat Frames are not.
 */
public interface IDirectionalSemiblock {
    Direction getSide();

    void setSide(Direction direction);
}
