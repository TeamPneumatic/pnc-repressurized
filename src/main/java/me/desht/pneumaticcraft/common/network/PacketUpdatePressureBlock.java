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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent periodically from server to sync pressure level:
 * - For pressure tubes with an attached pressure gauge module
 * - For air grate modules, when the pressure changes enough to modify the range
 * - For machine air handlers which are currently leaking
 */
public record PacketUpdatePressureBlock(BlockPos pos, Direction handlerDir, Direction leakDir, int currentAir) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("update_pressure_block");

    public static PacketUpdatePressureBlock fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdatePressureBlock(
                buffer.readBlockPos(),
                buffer.readNullable(buf -> buf.readEnum(Direction.class)),
                buffer.readNullable(buf -> buf.readEnum(Direction.class)),
                buffer.readInt()
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNullable(handlerDir, FriendlyByteBuf::writeEnum);
        buf.writeNullable(leakDir, FriendlyByteBuf::writeEnum);
        buf.writeInt(currentAir);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketUpdatePressureBlock message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            BlockEntity blockEntity = ClientUtils.getBlockEntity(message.pos());
            if (blockEntity != null) {
                PNCCapabilities.getAirHandler(blockEntity, message.handlerDir()).ifPresent(handler -> {
                    handler.setSideLeaking(message.leakDir());
                    handler.addAir(message.currentAir() - handler.getAir());
                    if (message.handlerDir() != null && blockEntity instanceof AbstractAirHandlingBlockEntity aah) {
                        aah.initializeHullAirHandlerClient(message.handlerDir(), handler);
                    }
                });
            }
        });
    }
}
