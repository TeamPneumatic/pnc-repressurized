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

import me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker.TrackerBlacklistManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to give a clientside BE a copy of its NBT data
 */
public record PacketSendNBTPacket(BlockPos pos, CompoundTag tag) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("send_nbt_packet");

    public static PacketSendNBTPacket forBlockEntity(BlockEntity te) {
        return new PacketSendNBTPacket(te.getBlockPos(), te.saveWithFullMetadata());
    }

    public static PacketSendNBTPacket fromNetwork(FriendlyByteBuf buffer) {
        return new PacketSendNBTPacket(buffer.readBlockPos(), buffer.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(tag);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSendNBTPacket message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            BlockEntity te = ClientUtils.getBlockEntity(message.pos());
            if (te != null) {
                try {
                    te.load(message.tag());
                } catch (Throwable e) {
                    TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
                }
            }
        });
    }
}
