package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class HackableCaveSpider implements IHackableEntity {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 50;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        entity.setDead();
        EntitySpider spider = new EntitySpider(entity.world);
        spider.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        spider.setHealth(((EntitySpider) entity).getHealth());
        spider.renderYawOffset = ((EntitySpider) entity).renderYawOffset;
        entity.world.spawnEntity(spider);
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
