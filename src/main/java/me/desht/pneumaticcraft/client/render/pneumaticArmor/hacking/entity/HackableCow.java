package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;

import java.util.List;

public class HackableCow implements IHackableEntity {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return entity.getClass() == EntityCow.class;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.fungiInfuse");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.fungiInfusion");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 100;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        if (!entity.world.isRemote) {
            entity.setDead();
            EntityMooshroom entitycow = new EntityMooshroom(entity.world);
            entitycow.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            entitycow.setHealth(((EntityCow) entity).getHealth());
            entitycow.renderYawOffset = ((EntityCow) entity).renderYawOffset;
            entity.world.spawnEntity(entitycow);
            entity.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, entity.posX, entity.posY + entity.height / 2.0F, entity.posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
