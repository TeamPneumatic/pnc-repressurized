package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntrySlime implements IEntityTrackEntry {
    private static final String[] MESSAGES = new String[]{
            "pneumaticcraft.entityTracker.info.slimeOther",
            "pneumaticcraft.entityTracker.info.slimeTiny",
            "pneumaticcraft.entityTracker.info.slimeSmall",
            "pneumaticcraft.entityTracker.info.slimeLarge"
    };

    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof Slime;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        int size = ((Slime) entity).getSize();
        if (size >= 1 && size <= 3) {
            curInfo.add(xlate(MESSAGES[size]));
        } else {
            curInfo.add(xlate(MESSAGES[0], size));
        }
    }
}
