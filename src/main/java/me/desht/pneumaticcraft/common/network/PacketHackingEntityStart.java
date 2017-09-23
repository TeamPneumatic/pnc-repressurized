package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.RenderTarget;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;

public class PacketHackingEntityStart extends AbstractPacket<PacketHackingEntityStart> {
    private int entityId;

    public PacketHackingEntityStart() {
    }

    public PacketHackingEntityStart(Entity entity) {
        entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public void handleClientSide(PacketHackingEntityStart message, EntityPlayer player) {
        Entity entity = player.world.getEntityByID(message.entityId);
        if (entity != null) {
            CommonHUDHandler.getHandlerForPlayer(player).setHackedEntity(entity);
            List<RenderTarget> targets = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargets();
            for (RenderTarget target : targets) {
                if (target.entity == entity) {
                    target.onHackConfirmServer();
                    break;
                }
            }
        }

    }

    @Override
    public void handleServerSide(PacketHackingEntityStart message, EntityPlayer player) {
        Entity entity = player.world.getEntityByID(message.entityId);
        if (entity != null) {
            CommonHUDHandler.getHandlerForPlayer(player).setHackedEntity(entity);
            NetworkHandler.sendToAllAround(message, new NetworkRegistry.TargetPoint(entity.world.provider.getDimension(), entity.posX, entity.posY, entity.posZ, 64));
        }
    }

}
