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

import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to highlight a block the drone can't interact with (blacklisted)
 */
public record PacketShowWireframe(BlockPos pos, int entityId) implements CustomPacketPayload {
    public static final Type<PacketShowWireframe> TYPE = new Type<>(RL("show_wireframe"));

    public static final StreamCodec<FriendlyByteBuf, PacketShowWireframe> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketShowWireframe::pos,
            ByteBufCodecs.INT, PacketShowWireframe::entityId,
            PacketShowWireframe::new
    );

    public static PacketShowWireframe forDrone(DroneEntity entity, BlockPos pos) {
        return new PacketShowWireframe(pos, entity.getId());
    }

    public static PacketShowWireframe fromNetwork(FriendlyByteBuf buffer) {
        return new PacketShowWireframe(buffer.readBlockPos(), buffer.readInt());
    }

    @Override
    public Type<PacketShowWireframe> type() {
        return TYPE;
    }

    public static void handle(PacketShowWireframe message, IPayloadContext ctx) {
        if (ctx.player().level().getEntity(message.entityId()) instanceof DroneEntity drone) {
            EntityTrackerClientHandler.addDroneToHudHandler(drone, message.pos());
        }
    }
}
