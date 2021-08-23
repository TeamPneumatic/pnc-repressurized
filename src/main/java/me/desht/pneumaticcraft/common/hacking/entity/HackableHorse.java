package me.desht.pneumaticcraft.common.hacking.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

/**
 * Horses, although tameable, don't extend EntityTameable.  Yay.
 */
public class HackableHorse extends HackableTameable {
    @Override
    public ResourceLocation getHackableId() {
        return RL("horse");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return !player.getUUID().equals(((HorseEntity) entity).getOwnerUUID());
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (entity.level.isClientSide) {
            entity.handleEntityEvent((byte) 7);
        } else {
            HorseEntity horse = (HorseEntity) entity;
            horse.getNavigation().stop();
            horse.setTarget(null);
            horse.setHealth(20.0F);
            horse.setOwnerUUID(player.getUUID());
            horse.level.broadcastEntityEvent(entity, (byte) 7);
            horse.setTamed(true);
        }
    }

}
