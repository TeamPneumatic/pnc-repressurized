package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class HackableShulker implements IHackableEntity {
    @Override
    public String getId() {
        return "shulker";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        EntityAITasks tasks = ((EntityLiving) entity).tasks;
        for (EntityAITasks.EntityAITaskEntry task : tasks.taskEntries) {
            if (Reflections.shulker_aiAttack.isAssignableFrom(task.action.getClass())) {
                tasks.removeTask(task.action);
                break;
            }
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        if (entity instanceof EntityShulker && entity.getEntityWorld().rand.nextInt(5) < 4) {
            ((EntityShulker) entity).updateArmorModifier(100);
        }
        return false;
    }
}
