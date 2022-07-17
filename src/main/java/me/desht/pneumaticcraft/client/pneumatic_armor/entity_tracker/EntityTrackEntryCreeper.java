package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryCreeper implements IEntityTrackEntry {
    private int creeperInFuseTime;

    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof Creeper;
    }

    @Override
    public void tick(Entity entity) {
        if (entity instanceof Creeper creeper) {
            if (creeper.getSwellDir() == 1) {
                creeperInFuseTime++;
                if (creeperInFuseTime > 30) creeperInFuseTime = 30;
            } else {
                creeperInFuseTime--;
                if (creeperInFuseTime < 0) creeperInFuseTime = 0;
            }
        }
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        if (creeperInFuseTime > 0) {
            if (((Creeper) entity).getSwellDir() == 1) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.creeper.fuse", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !").withStyle(ChatFormatting.RED));
            } else {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.creeper.coolDown", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !").withStyle(ChatFormatting.DARK_GREEN));
            }
        }
    }
}
