package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class HackableCreeper implements IHackableEntity {
    @Override
    public String getId() {
        return "creeper";
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.explode");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.exploded");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        ((CreeperEntity) entity).ignite();
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }

}
