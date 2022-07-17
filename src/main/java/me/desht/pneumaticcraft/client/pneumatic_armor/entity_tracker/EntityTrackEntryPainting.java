package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryPainting implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof Painting;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        if (entity instanceof Painting painting && painting.getVariant().isBound()) {
            ResourceLocation rl = ForgeRegistries.PAINTING_VARIANTS.getKey(painting.getVariant().get());
            if (rl != null) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.painting.art", rl.toString()));
            }
        }
    }
}
