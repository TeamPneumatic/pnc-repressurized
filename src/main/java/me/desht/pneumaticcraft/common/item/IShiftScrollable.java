package me.desht.pneumaticcraft.common.item;

import net.minecraft.entity.player.PlayerEntity;

public interface IShiftScrollable {
    /**
     * Called both client- and server-side when a player shift-scrolls the mouse wheel, while holding an item
     * (in main hand) which implements this interface.
     *
     * @param player player doing the shift-scrolling
     * @param forward true if the mouse wheel was rotated up, false if rotated down
     */
    void onShiftScrolled(PlayerEntity player, boolean forward);
}
