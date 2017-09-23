package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class HackableEnderman implements IHackableEntity {

    @Override
    public String getId() {
        return "enderman";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return onEndermanTeleport(entity);
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.stopTeleport");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.stopTeleporting");
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
        return true;
    }

    /**
     * @param entity
     * @return false if enderman should be disallowed from teleporting
     */
    public static boolean onEndermanTeleport(Entity entity) {
        List<IHackableEntity> hacks = PneumaticRegistry.getInstance().getHelmetRegistry().getCurrentEntityHacks(entity);
        for (IHackableEntity hack : hacks) {
            if (hack instanceof HackableEnderman) {
                return false;
            }
        }
        return true;
    }

}
