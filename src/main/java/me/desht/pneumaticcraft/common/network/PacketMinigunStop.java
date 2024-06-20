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

import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when player switches away from a held minigun which is active (i.e. spinning)
 */
public record PacketMinigunStop(ItemStack stack) implements CustomPacketPayload {
    public static final Type<PacketMinigunStop> TYPE = new Type<>(RL("packetminigunstop"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketMinigunStop> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, PacketMinigunStop::stack,
            PacketMinigunStop::new
    );

    @Override
    public Type<PacketMinigunStop> type() {
        return TYPE;
    }

    public static void handle(PacketMinigunStop message, IPayloadContext ctx) {
        ItemStack stack = message.stack();
        if (stack.getItem() instanceof MinigunItem mgItem) {
            Minigun minigun = mgItem.getMinigun(stack, ctx.player());
            minigun.setMinigunSpeed(0);
            minigun.setMinigunActivated(false);
            minigun.setMinigunTriggerTimeOut(0);
            ctx.player().playSound(ModSounds.MINIGUN_STOP.get(), 1f, 1f);
        }
    }
}
