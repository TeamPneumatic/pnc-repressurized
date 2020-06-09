package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HackableCow implements IHackableEntity {
    @Override
    public ResourceLocation getHackableId() {
        return RL("cow");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        // mooshrooms are also a type of CowEntity
        return entity.getType() == EntityType.COW;
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.result.fungiInfuse");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.finished.fungiInfusion");
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
            entitycow.setLocationAndAngles(entity.getPosX(), entity.getPosY(), entity.getPosZ(), entity.rotationYaw, entity.rotationPitch);
            entitycow.setHealth(((CowEntity) entity).getHealth());
            entitycow.renderYawOffset = ((CowEntity) entity).renderYawOffset;
            entity.world.addEntity(entitycow);
            entity.world.addParticle(ParticleTypes.EXPLOSION, entity.getPosX(), entity.getPosY() + entity.getHeight() / 2.0F, entity.getPosZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
