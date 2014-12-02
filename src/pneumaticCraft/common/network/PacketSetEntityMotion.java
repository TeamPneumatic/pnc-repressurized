package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSetEntityMotion extends LocationDoublePacket<PacketSetEntityMotion>{

    private int entityId;

    public PacketSetEntityMotion(){}

    public PacketSetEntityMotion(Entity entity, double dx, double dy, double dz){
        super(dx, dy, dz);
        entityId = entity.getEntityId();
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        buf.writeInt(entityId);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        entityId = buf.readInt();
    }

    @Override
    public void handleClientSide(PacketSetEntityMotion message, EntityPlayer player){
        Entity entity = player.worldObj.getEntityByID(message.entityId);
        if(entity != null) {
            entity.motionX = message.x;
            entity.motionY = message.y;
            entity.motionZ = message.z;

            entity.onGround = false;
            entity.isCollided = false;
            entity.isCollidedHorizontally = false;
            entity.isCollidedVertically = false;
            if(entity instanceof EntityLivingBase) ((EntityLivingBase)entity).setJumping(true);
        }
    }

    @Override
    public void handleServerSide(PacketSetEntityMotion message, EntityPlayer player){}

}
