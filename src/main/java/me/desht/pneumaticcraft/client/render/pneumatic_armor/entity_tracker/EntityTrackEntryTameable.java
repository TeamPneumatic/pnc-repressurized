package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryTameable implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof TamableAnimal;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        LivingEntity owner = ((TamableAnimal) entity).getOwner();
        if (owner != null) {
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.tamed", owner.getDisplayName().getString()));
        } else {
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.canTame"));
        }
    }
}
