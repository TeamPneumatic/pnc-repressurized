package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Horses, although tameable, don't extend EntityTameable.  Yay.
 */
public class HackableHorse extends HackableTameable {
    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return !player.getUniqueID().equals(((EntityHorse) entity).getOwnerUniqueId());
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        EntityHorse horse = (EntityHorse) entity;
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
