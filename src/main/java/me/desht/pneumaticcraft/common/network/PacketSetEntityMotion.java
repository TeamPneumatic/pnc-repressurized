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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when an immediate update is needed to a client-side entity's motion
 */
public record PacketSetEntityMotion(Vector3f vec, int entityId) implements CustomPacketPayload {
    public static final Type<PacketSetEntityMotion> TYPE = new Type<>(RL("set_entity_motion"));

    public static final StreamCodec<FriendlyByteBuf, PacketSetEntityMotion> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, PacketSetEntityMotion::vec,
            ByteBufCodecs.INT, PacketSetEntityMotion::entityId,
            PacketSetEntityMotion::new
    );

    public static PacketSetEntityMotion create(Entity entity, Vec3 motion) {
        return new PacketSetEntityMotion(motion.toVector3f(), entity.getId());
    }

    @Override
    public Type<PacketSetEntityMotion> type() {
        return TYPE;
    }

    public static void handle(PacketSetEntityMotion message, IPayloadContext ctx) {
        Entity entity = ctx.player().level().getEntity(message.entityId());
        if (entity != null) {
            entity.setDeltaMovement(new Vec3(message.vec()));
            entity.setOnGround(false);
            entity.horizontalCollision = false;
            entity.verticalCollision = false;
            if (entity instanceof LivingEntity l) {
                l.setJumping(true);
            }
        }
    }
}
