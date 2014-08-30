package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSetMobTarget extends AbstractPacket<PacketSetMobTarget>{
    private int mobId, targetId;

    public PacketSetMobTarget(){}

    public PacketSetMobTarget(EntityCreature mob, EntityLivingBase target){
        mobId = mob.getEntityId();
        targetId = target != null ? target.getEntityId() : -1;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(mobId);
        buffer.writeInt(targetId);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        mobId = buffer.readInt();
        targetId = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketSetMobTarget message, EntityPlayer player){
        Entity mob = player.worldObj.getEntityByID(message.mobId);
        Entity target = player.worldObj.getEntityByID(message.targetId);
        if(mob instanceof EntityCreature) {
            ((EntityCreature)mob).setAttackTarget(target instanceof EntityLivingBase ? (EntityLivingBase)target : null);
        }
    }

    @Override
    public void handleServerSide(PacketSetMobTarget message, EntityPlayer player){

    }
}
