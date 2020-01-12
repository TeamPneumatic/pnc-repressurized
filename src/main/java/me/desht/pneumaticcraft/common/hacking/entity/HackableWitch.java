package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class HackableWitch implements IHackableEntity {
    @Override
    public String getId() {
        return "witch";
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return true;
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.disarm");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.disarmed");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        if (entity instanceof WitchEntity) {
            ((WitchEntity) entity).potionUseTimer = 20;
            ((WitchEntity) entity).setDrinkingPotion(true);
            return true;
        } else {
            Log.error("something's wrong: found HackableWitch hack on " + entity);
            return false;
        }
    }

}
