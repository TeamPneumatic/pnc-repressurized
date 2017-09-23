package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.entity.Entity;

import java.util.List;

/**
 * Implement this class and register it by adding it to the entityTrackEntries class.
 * There needs to be a parameterless constructor. For every entity that's applicable for this definition, an instance is created.
 */
public interface IEntityTrackEntry {
    /**
     * Return true if you want to add a tooltip for the given entity.
     *
     * @param entity
     * @return
     */
    boolean isApplicable(Entity entity);

    /**
     * Add info to the tab. This is only called when isApplicable returned true.
     *
     * @param entity
     * @param curInfo
     */
    void addInfo(Entity entity, List<String> curInfo);

    /**
     * Update is called every (client) tick, and can be used to update something like a timer (used for the Creeper countdown).
     *
     * @param entity
     */
    void update(Entity entity);

    /**
     * Called every render tick, this method can be used to render additional info. Used for Drone AI visualisation.
     *
     * @param entity
     * @param partialTicks
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
