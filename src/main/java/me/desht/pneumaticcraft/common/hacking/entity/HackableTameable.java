package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableTameable implements IHackableEntity {

    @Override
    public ResourceLocation getHackableId() {
        return RL("tameable");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity instanceof TameableEntity && ((TameableEntity) entity).getOwner() != player;
    }

    @Override
    public void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.tame"));
    }

    @Override
    public void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.tamed"));
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        if (entity.world.isRemote) {
            entity.handleStatusUpdate((byte) 7);
        } else {
            TameableEntity tameable = (TameableEntity) entity;
            tameable.getNavigator().clearPath();
            tameable.setAttackTarget(null);
            tameable.setHealth(20.0F);
            tameable.setOwnerId(player.getUniqueID());
            tameable.world.setEntityState(entity, (byte) 7);
            tameable.setTamed(true);

            // TODO: code smell
            // Would be better to have a HackableCat subclass, but HackableHandler.getHackableForEntity() isn't
            // set up to prioritise getting a cat over a generic tameable.
            if (entity instanceof CatEntity) {
                ((CatEntity) entity).setCatType(-1);  // < 0 means "use a random type"
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
