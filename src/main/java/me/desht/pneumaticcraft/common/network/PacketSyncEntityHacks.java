package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when player starts tracking an entity with any active hacks on it.
 */
public record PacketSyncEntityHacks(int entityId, List<ResourceLocation> hackIds) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("sync_entity_hacks");

    public static PacketSyncEntityHacks create(Entity target, List<ResourceLocation> ids) {
        return new PacketSyncEntityHacks(target.getId(), ids);
    }

    public static PacketSyncEntityHacks fromNetwork(FriendlyByteBuf buf) {
        return new PacketSyncEntityHacks(buf.readInt(), buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeCollection(hackIds, FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSyncEntityHacks message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            Entity e = ClientUtils.getClientLevel().getEntity(message.entityId());
            if (e != null) {
                HackManager.getActiveHacks(e).ifPresent(hacking -> {
                    for (ResourceLocation id : message.hackIds()) {
                        CommonArmorRegistry.getInstance().getHackableEntityForId(id).ifPresent(hacking::addHackable);
                    }
                });
            }
        });
    }
}
