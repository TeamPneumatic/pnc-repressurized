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
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to highlight a block the drone can't interact with (blacklisted)
 */
public record PacketShowWireframe(BlockPos pos, int entityId) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("show_wireframe");

    public static PacketShowWireframe create(DroneEntity entity, BlockPos pos) {
        return new PacketShowWireframe(pos, entity.getId());
    }

    public static PacketShowWireframe fromNetwork(FriendlyByteBuf buffer) {
        return new PacketShowWireframe(buffer.readBlockPos(), buffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(entityId);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketShowWireframe message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (ClientUtils.getClientLevel().getEntity(message.entityId()) instanceof DroneEntity drone) {
                ClientUtils.addDroneToHudHandler(drone, message.pos());
            }
        });
    }
}
