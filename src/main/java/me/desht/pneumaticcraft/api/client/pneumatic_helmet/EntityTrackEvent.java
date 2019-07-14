package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when a helmet Block Tracker is about to track an entity. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 *
 * @author MineMaarten
 */
@Cancelable
public class EntityTrackEvent extends Event {
    public final Entity trackingEntity;

    public EntityTrackEvent(Entity trackingEntity) {
        this.trackingEntity = trackingEntity;
    }

}
