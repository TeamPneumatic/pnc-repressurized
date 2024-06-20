/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.RingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import java.util.Objects;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to get the client to spawn a new client-side ring entity
 */
public record PacketSpawnRing(Vector3f vec, int targetEntityId, int color) implements CustomPacketPayload {
    public static final Type<PacketSpawnRing> TYPE = new Type<>(RL("spawn_ring"));

    public static final StreamCodec<FriendlyByteBuf, PacketSpawnRing> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, PacketSpawnRing::vec,
            ByteBufCodecs.INT, PacketSpawnRing::targetEntityId,
            ByteBufCodecs.INT, PacketSpawnRing::color,
            PacketSpawnRing::new
    );

    public static PacketSpawnRing create(BlockPos pos, Entity targetEntity, Integer color) {
        return new PacketSpawnRing(Vec3.atCenterOf(pos).toVector3f(), targetEntity.getId(), Objects.requireNonNullElse(color, 0xFFFFFFFF));
    }

    @Override
    public Type<PacketSpawnRing> type() {
        return TYPE;
    }

    public static void handle(PacketSpawnRing message, IPayloadContext ctx) {
        Level level = ctx.player().level();
        Entity entity = level.getEntity(message.targetEntityId());
        if (entity != null) {
            Vector3f vec = message.vec();
            ClientUtils.spawnEntityClientside(new RingEntity(level, vec.x, vec.y, vec.z, entity, message.color()));
        }
    }
}
