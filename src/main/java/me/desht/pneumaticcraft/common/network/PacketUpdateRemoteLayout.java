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

import me.desht.pneumaticcraft.common.item.RemoteItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.rmi.Remote;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to update the layout of a Remote item from the Remote GUI
 */
public record PacketUpdateRemoteLayout(CompoundTag layout, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<PacketUpdateRemoteLayout> TYPE = new Type<>(RL("update_remote_layout"));

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateRemoteLayout> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, PacketUpdateRemoteLayout::layout,
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), PacketUpdateRemoteLayout::hand,
            PacketUpdateRemoteLayout::new
    );

    @Override
    public Type<PacketUpdateRemoteLayout> type() {
        return TYPE;
    }

    public static void handle(PacketUpdateRemoteLayout message, IPayloadContext ctx) {
        ItemStack remote = ctx.player().getItemInHand(message.hand);
        if (remote.getItem() instanceof RemoteItem) {
            RemoteItem.setSavedLayout(remote, message.layout);
        }
    }
}
