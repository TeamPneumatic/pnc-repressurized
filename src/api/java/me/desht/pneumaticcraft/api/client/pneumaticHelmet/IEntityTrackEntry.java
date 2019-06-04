package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.entity.Entity;

import java.util.List;

/**
 * Implement this class and register it with {@link IPneumaticHelmetRegistry#registerEntityTrackEntry(Class)}.
 * Your implementation must provide a no-parameter constructor. For every entity that's applicable for this definition,
 * an instance is created.
 */
public interface IEntityTrackEntry {
    /**
     * Return true if you want to add a tooltip for the given entity.
     *
     * @param entity the candidate entity
     * @return true if this tracker is applicable to the given entity
     */
    boolean isApplicable(Entity entity);

    /**
     * Add info to the tab. This is only called when isApplicable returned true.
     *
     * @param entity the tracked entity
     * @param curInfo list of String to append information to
     * @param isLookingAtTarget true if the player is focused on the tracked entity
     */
    void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget);

    /**
     * Update is called every (client) tick, and can be used to update something like a timer (e.g. used for the Creeper
     * explosion countdown).
     *
     * @param entity the tracked entity
     */
    void update(Entity entity);

    /**
     * Called every render tick, this method can be used to render additional info. Used for Drone AI visualisation.
     *
     * @param entity the tracked entity
     * @param partialTicks partial ticks since last full ticks
     */
    void render(Entity entity, float partialTicks);

    /**
     * Just a basic implementation class that can be used if an update and render method isn't needed.
     */
    abstract class EntityTrackEntry implements IEntityTrackEntry {
        @Override
        public void update(Entity entity) {
        }

        @Override
        public void render(Entity entity, float partialTicks) {
        }
    }
}
