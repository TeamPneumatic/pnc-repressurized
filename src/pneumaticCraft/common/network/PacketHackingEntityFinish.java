package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.CommonHUDHandler;

public class PacketHackingEntityFinish extends AbstractPacket<PacketHackingEntityFinish>{
    private int entityId;

    public PacketHackingEntityFinish(){}

    public PacketHackingEntityFinish(Entity entity){
        entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf){
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(entityId);
    }

    @Override
    public void handleClientSide(PacketHackingEntityFinish message, EntityPlayer player){
        Entity entity = player.worldObj.getEntityByID(message.entityId);
        if(entity != null) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(entity, player);
            if(hackableEntity != null) {
                hackableEntity.onHackFinished(entity, player);
                PneumaticCraft.proxy.getHackTickHandler().trackEntity(entity, hackableEntity);
                CommonHUDHandler.getHandlerForPlayer(player).setHackedEntity(null);
                player.worldObj.playSound(entity.posX, entity.posY, entity.posZ, "PneumaticCraft:helmetHackFinish", 1.0F, 1.0F, false);
            }
        }

    }

    @Override
    public void handleServerSide(PacketHackingEntityFinish message, EntityPlayer player){}

}
