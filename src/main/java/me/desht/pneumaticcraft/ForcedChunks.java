package me.desht.pneumaticcraft;

import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public enum ForcedChunks {
    INSTANCE;

    private TicketController pcController;

    public void registerTicketController(RegisterTicketControllersEvent event) {
        pcController = new TicketController(RL("programmable_controller"));
        event.register(pcController);
    }

    public TicketController getPcController() {
        return pcController;
    }
}
