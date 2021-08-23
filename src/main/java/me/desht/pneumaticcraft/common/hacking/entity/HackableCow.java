package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.fungiInfuse"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.fungiInfusion"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 100;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (!entity.level.isClientSide) {
            entity.remove();
            MooshroomEntity entitycow = new MooshroomEntity(EntityType.MOOSHROOM, entity.level);
            entitycow.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
            entitycow.setHealth(((CowEntity) entity).getHealth());
            entitycow.yBodyRot = ((CowEntity) entity).yBodyRot;
            entity.level.addFreshEntity(entitycow);
            entity.level.addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0F, entity.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
