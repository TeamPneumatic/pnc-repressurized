package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HackableEnderman implements IHackableEntity {

    @Override
    public ResourceLocation getHackableId() {
        return RL("enderman");
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return onEndermanTeleport(entity);
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.result.stopTeleport");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.finished.stopTeleporting");
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
        return entity.isAlive();
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
