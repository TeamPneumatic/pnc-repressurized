package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

import java.util.List;

public class EntityTrackEntryMinecart implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof AbstractMinecart;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        // TODO 1.17 implement an entity syncing protocol (probably as part of a general pneumatic helmet tracker overhaul)
    }
}
