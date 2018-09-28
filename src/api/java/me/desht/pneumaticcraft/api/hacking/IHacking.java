package me.desht.pneumaticcraft.api.hacking;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;

import java.util.List;

/**
 * Manages the list of "hacks" on an entity. Hacks are added via the Pneumatic Helmet
 * hacking feature.  You do not need to implement this class; retrieve an instance via
 * capabilities: {@code entity.getCapability(CapabilityHacking.HACKING_CAPABILITY, null)}
 */
public interface IHacking {
    /**
     * Called every tick on every entity which has been hacked (i.e. which has a non-empty list of hacks)
     *
     * @param entity the hacked entity
     */
    void update(Entity entity);

    /**
     * Add a new hack to the entity's list of hacks.
     * @param hackable a hack
     */
    void addHackable(IHackableEntity hackable);

    /**
     * Get a list of the hacks currently on the entity.
     *
     * @return a list of hacks
     */
    List<IHackableEntity> getCurrentHacks();
}
