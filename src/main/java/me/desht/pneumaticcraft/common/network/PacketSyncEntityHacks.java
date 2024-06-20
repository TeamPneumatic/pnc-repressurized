package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when player starts tracking an entity with any active hacks on it.
 */
public record PacketSyncEntityHacks(int entityId, List<ResourceLocation> hackIds) implements CustomPacketPayload {
    public static final Type<PacketSyncEntityHacks> TYPE = new Type<>(RL("sync_entity_hacks"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncEntityHacks> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketSyncEntityHacks::entityId,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketSyncEntityHacks::hackIds,
            PacketSyncEntityHacks::new
    );

    public static PacketSyncEntityHacks create(Entity target, List<ResourceLocation> ids) {
        return new PacketSyncEntityHacks(target.getId(), ids);
    }

    @Override
    public Type<PacketSyncEntityHacks> type() {
        return TYPE;
    }

    public static void handle(PacketSyncEntityHacks message, IPayloadContext ctx) {
        Entity e = ctx.player().level().getEntity(message.entityId());
        if (e != null) {
            HackManager.getActiveHacks(e).ifPresent(hacking -> {
                for (ResourceLocation id : message.hackIds()) {
                    CommonArmorRegistry.getInstance().getHackableEntityForId(id).ifPresent(hacking::addHackable);
                }
            });
        }
    }
}
