package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class HackableCaveSpider implements IHackableEntity {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 50;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.world.isRemote) {
            entity.remove();
            SpiderEntity spider = new SpiderEntity(EntityType.SPIDER, entity.world);
            spider.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            spider.setHealth(((SpiderEntity) entity).getHealth());
            spider.renderYawOffset = ((SpiderEntity) entity).renderYawOffset;
            entity.world.addEntity(spider);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
