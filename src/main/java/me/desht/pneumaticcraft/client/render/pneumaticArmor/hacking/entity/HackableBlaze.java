package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
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
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        /*EntityAITasks tasks = ((EntityLiving)entity).tasks;
        for(EntityAITasks.EntityAITaskEntry task : tasks.taskEntries){
            if(task.action instanceof EntityBlaze.AIFireballAttack){
                tasks.removeTask(task.action);
                break;
            }
        }
        for(int i = 0; i < )
        ((EntityLivingBase)entity).attackTime = 20;*///TODO 1.8 fix
        return true;
    }
}
