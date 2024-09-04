package me.desht.pneumaticcraft.common.util.chunkloading;

import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public enum ForcedChunks {
    INSTANCE;

    private TicketController pcController;
    private TicketController droneController;

    public void registerTicketController(RegisterTicketControllersEvent event) {
        pcController = new TicketController(RL("programmable_controller"));
        droneController = new TicketController(RL("drone_controller"));
        event.register(pcController);
        event.register(droneController);
    }

    public TicketController getPcController() {
        return pcController;
    }

    public TicketController getDroneController() {
        return droneController;
    }
}
