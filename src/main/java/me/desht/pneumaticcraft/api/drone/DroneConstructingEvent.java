package me.desht.pneumaticcraft.api.drone;


import net.minecraftforge.eventbus.api.Event;

public class DroneConstructingEvent extends Event {
    public final IDrone drone;

    public DroneConstructingEvent(IDrone drone) {
        this.drone = drone;
    }
}
