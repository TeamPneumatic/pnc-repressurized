package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class HackableWitch implements IHackableEntity {
    @Override
    public String getId() {
        return "witch";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.disarm");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.disarmed");
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
        if (entity instanceof EntityWitch) {
            Reflections.setWitchPotionUseTimer((EntityWitch) entity, 20);
            ((EntityWitch) entity).setDrinkingPotion(true);
            return true;
        } else {
            PneumaticCraftRepressurized.logger.error("something's wrong: found HackableWitch hack on " + entity);
            return false;
        }
    }

}
