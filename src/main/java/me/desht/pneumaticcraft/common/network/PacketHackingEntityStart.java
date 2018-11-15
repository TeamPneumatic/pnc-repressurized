package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.RenderTarget;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

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
            HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargetsStream()
                    .filter(target -> target.entity == entity)
                    .findFirst()
                    .ifPresent(RenderTarget::onHackConfirmServer);
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
