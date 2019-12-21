package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;

import java.util.List;

public class HackableCow implements IHackableEntity {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity.getClass() == CowEntity.class;
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.fungiInfuse");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.fungiInfusion");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 100;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.world.isRemote) {
            entity.remove();
            MooshroomEntity entitycow = new MooshroomEntity(EntityType.MOOSHROOM, entity.world);
            entitycow.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            entitycow.setHealth(((CowEntity) entity).getHealth());
            entitycow.renderYawOffset = ((CowEntity) entity).renderYawOffset;
            entity.world.addEntity(entitycow);
            entity.world.addParticle(ParticleTypes.EXPLOSION, entity.posX, entity.posY + entity.getHeight() / 2.0F, entity.posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
