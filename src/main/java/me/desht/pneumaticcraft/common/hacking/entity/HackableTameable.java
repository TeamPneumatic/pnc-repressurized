package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class HackableTameable implements IHackableEntity {

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return ((TameableEntity) entity).getOwner() != player;
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.tame");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.tamed");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        TameableEntity tameable = (TameableEntity) entity;
        if (entity.world.isRemote) {
            tameable.handleStatusUpdate((byte) 7);
        } else {
            tameable.getNavigator().clearPath();
            tameable.setAttackTarget(null);
            tameable.setHealth(20.0F);
            tameable.setOwnerId(player.getUniqueID());
            entity.world.setEntityState(tameable, (byte) 7);
            tameable.setTamed(true);

            // TODO: code smell
            // Would be better to have a HackableOcelot subclass, but HackableHandler.getHackableForEntity() isn't
            // set up to prioritise getting an ocelot over a generic tameable.
//            if (entity instanceof OcelotEntity) {
//                ((OcelotEntity) entity).setTameSkin(1 + entity.getEntityWorld().rand.nextInt(3));
//            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
