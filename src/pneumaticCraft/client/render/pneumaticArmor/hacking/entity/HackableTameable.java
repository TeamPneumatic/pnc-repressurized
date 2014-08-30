package pneumaticCraft.client.render.pneumaticArmor.hacking.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;

public class HackableTameable implements IHackableEntity{

    @Override
    public String getId(){
        return null;
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player){
        return ((EntityTameable)entity).getOwner() != player;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.result.tame");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player){
        curInfo.add("pneumaticHelmet.hacking.finished.tamed");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player){
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player){
        EntityTameable tameable = (EntityTameable)entity;
        if(entity.worldObj.isRemote) {
            tameable.handleHealthUpdate((byte)7);
        } else {
            tameable.setPathToEntity((PathEntity)null);
            tameable.setAttackTarget((EntityLivingBase)null);
            tameable.setHealth(20.0F);
            tameable.func_152115_b(player.getUniqueID().toString());//setOwner
            entity.worldObj.setEntityState(tameable, (byte)7);
            tameable.setTamed(true);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity){
        return false;
    }

}
