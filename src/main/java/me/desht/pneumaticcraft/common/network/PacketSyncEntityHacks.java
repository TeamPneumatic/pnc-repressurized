package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when player starts tracking an entity with any active hacks on it.
 */
public class PacketSyncEntityHacks {
    private final int entityId;
    private final List<ResourceLocation> ids;

    public PacketSyncEntityHacks(Entity target, List<ResourceLocation> ids) {
        this.entityId = target.getId();
        this.ids = ids;
    }

    public PacketSyncEntityHacks(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        int size = buf.readVarInt();
        ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readResourceLocation());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeVarInt(ids.size());
        ids.forEach(buf::writeResourceLocation);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity e = ClientUtils.getClientLevel().getEntity(entityId);
            if (e != null) {
                e.getCapability(PNCCapabilities.HACKING_CAPABILITY).ifPresent(hacking -> {
                    for (ResourceLocation id : ids) {
                        CommonArmorRegistry.getInstance().getHackableEntityForId(id).ifPresent(hacking::addHackable);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
