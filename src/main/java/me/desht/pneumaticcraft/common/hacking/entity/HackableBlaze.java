package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class HackableBlaze implements IHackableEntity {

    @Override
    public String getId() {
        return "blaze";
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
            if (Reflections.blaze_aiFireballAttack.isAssignableFrom(task.action.getClass())) {
                tasks.removeTask(task.action);
                break;
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
