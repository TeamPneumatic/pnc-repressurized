package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryItemFrame implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof ItemFrame;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        ItemFrame frame = (ItemFrame) entity;
        ItemStack stack = frame.getItem();

        if (!stack.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.itemframe.item", stack.getHoverName().getString()));
            if (frame.getRotation() != 0) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.itemframe.rotation", frame.getRotation() * 45));
            }
        }
    }
}
