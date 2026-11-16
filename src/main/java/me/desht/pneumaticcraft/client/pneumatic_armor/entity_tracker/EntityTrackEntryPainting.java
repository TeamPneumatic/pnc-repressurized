package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.painting.Painting;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryPainting implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof Painting;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        if (entity instanceof Painting painting) {
            painting.getVariant().unwrapKey().ifPresent(key ->
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.painting.art", key.identifier().toString())));
        }
    }
}
