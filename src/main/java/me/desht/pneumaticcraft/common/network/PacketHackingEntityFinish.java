package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when an entity hack completes
 */
public class PacketHackingEntityFinish {
    private int entityId;

    public PacketHackingEntityFinish() {
    }

    public PacketHackingEntityFinish(Entity entity) {
        entityId = entity.getEntityId();
    }

    public PacketHackingEntityFinish(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
            Entity entity = player.world.getEntityByID(entityId);
            if (entity != null) {
                IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(entity, player);
                if (hackableEntity != null) {
                    hackableEntity.onHackFinished(entity, player);
                    HackTickHandler.instance().trackEntity(entity, hackableEntity);
                    CommonArmorHandler.getHandlerForPlayer(player).setHackedEntity(null);
                    player.playSound(ModSounds.HELMET_HACK_FINISH.get(), 1.0F, 1.0F);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
