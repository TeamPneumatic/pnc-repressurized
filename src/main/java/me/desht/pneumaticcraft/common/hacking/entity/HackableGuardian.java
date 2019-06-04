package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;

import java.util.List;

public class HackableGuardian implements IHackableEntity {
    @Override
    public String getId() {
        return "guardian";
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
        EntityAITasks tasks = ((EntityLiving) entity).tasks;
        for (EntityAITasks.EntityAITaskEntry task : tasks.taskEntries) {
            if (Reflections.guardian_aiGuardianAttack.isAssignableFrom(task.action.getClass())) {
                tasks.removeTask(task.action);
                break;
            }
        }
        if (entity instanceof EntityElderGuardian) {
            player.removeActivePotionEffect(MobEffects.MINING_FATIGUE);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
