package pneumaticCraft.client.render.pneumaticArmor.hacking.entity;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.lib.Log;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class HackableWitch implements IHackableEntity{
    private static Field attackTimer;

    @Override
    public String getId(){
        return "witch";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player){
        return true;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.result.disarm");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.finished.disarmed");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player){
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player){}

    @Override
    public boolean afterHackTick(Entity entity){
        if(attackTimer == null) attackTimer = ReflectionHelper.findField(EntityWitch.class, "field_82200_e", "witchAttackTimer");
        try {
            attackTimer.set(entity, 20);
            ((EntityWitch)entity).setAggressive(true);
            return true;
        } catch(Exception e) {
            Log.warning("Reflection failed on HackableWitch:");
            e.printStackTrace();
            return false;
        }
    }

}
