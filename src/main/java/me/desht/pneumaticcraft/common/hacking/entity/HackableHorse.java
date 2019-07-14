package me.desht.pneumaticcraft.common.hacking.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Horses, although tameable, don't extend EntityTameable.  Yay.
 */
public class HackableHorse extends HackableTameable {
    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return !player.getUniqueID().equals(((HorseEntity) entity).getOwnerUniqueId());
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        HorseEntity horse = (HorseEntity) entity;
        if (entity.world.isRemote) {
            horse.handleStatusUpdate((byte) 7);
        } else {
            horse.getNavigator().clearPath();
            horse.setAttackTarget(null);
            horse.setHealth(20.0F);
            horse.setOwnerUniqueId(player.getUniqueID());
            entity.world.setEntityState(horse, (byte) 7);
            horse.setHorseTamed(true);
        }
    }
}
