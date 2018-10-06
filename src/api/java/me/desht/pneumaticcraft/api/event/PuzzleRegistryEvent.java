package me.desht.pneumaticcraft.api.event;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired just before the finalised puzzle piece list is compiled.  If you need to register custom puzzle pieces, do it
 * here, via {@link PuzzleRegistryEvent#getDroneRegistry()} and
 * {@link IDroneRegistry#registerCustomBlockInteractor(ICustomBlockInteract)}
 */
public class PuzzleRegistryEvent extends Event {
    private final IDroneRegistry droneRegistry;

    public PuzzleRegistryEvent() {
        droneRegistry = PneumaticRegistry.getInstance().getDroneRegistry();
    }

    public IDroneRegistry getDroneRegistry() {
        return droneRegistry;
    }
}
