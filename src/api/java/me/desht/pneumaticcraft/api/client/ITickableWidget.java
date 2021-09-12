package me.desht.pneumaticcraft.api.client;

import net.minecraft.client.gui.screen.Screen;

/**
 * A widget which will be ticked by PneumaticCraft GUI's.
 */
public interface ITickableWidget {
    /**
     * Called each tick by the {@link Screen#tick()} method.
     */
    void tickWidget();
}
