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
        return !player.getUniqueID().equals(((HorseEntity) entity).getOwnerUniqueId());
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (entity.world.isRemote) {
            entity.handleStatusUpdate((byte) 7);
        } else {
            HorseEntity horse = (HorseEntity) entity;
            horse.getNavigator().clearPath();
            horse.setAttackTarget(null);
            horse.setHealth(20.0F);
            horse.setOwnerUniqueId(player.getUniqueID());
            horse.world.setEntityState(entity, (byte) 7);
            horse.setHorseTamed(true);
        }
    }

}
