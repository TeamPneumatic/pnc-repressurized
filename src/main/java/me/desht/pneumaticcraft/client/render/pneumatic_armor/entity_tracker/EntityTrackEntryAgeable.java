package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

// TODO this doesn't fully work since the client doesn't get the full age data of an entity
//  but is it worth going to the trouble of requesting extra server data?
public class EntityTrackEntryAgeable implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof AgeableMob;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        int growingAge = ((AgeableMob) entity).getAge();
        if (growingAge > 0) {
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.canBreedIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(growingAge, false)));
        } else if (growingAge < 0) {
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.growsUpIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(-growingAge, false)));
        } else {
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.canBreedNow"));
        }
    }
}
