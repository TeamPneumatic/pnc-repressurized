package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.client.render.pneumaticArmor.EntityTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.RenderTarget;
import pneumaticCraft.common.CommonHUDHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class PacketHackingEntityStart extends AbstractPacket<PacketHackingEntityStart>{
    private int entityId;

    public PacketHackingEntityStart(){}

    public PacketHackingEntityStart(Entity entity){
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
    public void handleClientSide(PacketHackingEntityStart message, EntityPlayer player){
        Entity entity = player.worldObj.getEntityByID(message.entityId);
        if(entity != null) {
            CommonHUDHandler.getHandlerForPlayer(player).setHackedEntity(entity);
            List<RenderTarget> targets = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargets();
            for(RenderTarget target : targets) {
                if(target.entity == entity) {
                    target.onHackConfirmServer();
                    break;
                }
            }
        }

    }

    @Override
    public void handleServerSide(PacketHackingEntityStart message, EntityPlayer player){
        Entity entity = player.worldObj.getEntityByID(message.entityId);
        if(entity != null) {
            CommonHUDHandler.getHandlerForPlayer(player).setHackedEntity(entity);
            NetworkHandler.sendToAllAround(message, new NetworkRegistry.TargetPoint(entity.worldObj.provider.dimensionId, entity.posX, entity.posY, entity.posZ, 64));
        }
    }

}
