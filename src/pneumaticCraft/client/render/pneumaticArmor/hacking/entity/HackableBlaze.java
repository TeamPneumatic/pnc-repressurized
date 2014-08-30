package pneumaticCraft.client.render.pneumaticArmor.hacking.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;

public class HackableBlaze implements IHackableEntity{

    @Override
    public String getId(){
        return "blaze";
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
        ((EntityLivingBase)entity).attackTime = 20;
        return true;
    }

}
