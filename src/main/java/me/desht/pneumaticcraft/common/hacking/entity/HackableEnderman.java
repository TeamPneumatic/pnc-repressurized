package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

import java.util.List;

public class HackableEnderman implements IHackableEntity {

    @Override
    public String getId() {
        return "enderman";
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return onEndermanTeleport(entity);
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.stopTeleport");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.stopTeleporting");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        // enderman teleport suppression is handled in EventHandlerPneumaticCraft#onEnderTeleport
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return true;
    }

    /**
     * See {@link me.desht.pneumaticcraft.common.event.EventHandlerPneumaticCraft#onEnderTeleport(EnderTeleportEvent)}
     * @param entity the enderman
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
