package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class HackableCreeper implements IHackableEntity {
    @Override
    public String getId() {
        return "creeper";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.explode");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.exploded");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        ((EntityCreeper) entity).setCreeperState(1);
        return true;
    }

}
