package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Implement on a Container/Tile Entity/EntitySemiblock to allow it to receive messages from the client when a GUI button is
 * clicked.
 */
public interface IGUIButtonSensitive {
    String REDSTONE_TAG = "redstone";

    void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player);
}