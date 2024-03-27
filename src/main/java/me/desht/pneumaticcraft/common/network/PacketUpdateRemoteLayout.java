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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to update the layout of a Remote item from the Remote GUI
 */
public record PacketUpdateRemoteLayout(CompoundTag layout, InteractionHand hand) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("update_remote_layout");

    public static PacketUpdateRemoteLayout fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdateRemoteLayout(buffer.readNbt(), buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(layout);
        buf.writeEnum(hand);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketUpdateRemoteLayout message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            ItemStack remote = player.getItemInHand(message.hand());
            if (remote.getItem() instanceof RemoteItem) {
                CompoundTag tag = remote.getTag();
                if (tag == null) {
                    tag = new CompoundTag();
                    remote.setTag(tag);
                }
                tag.put("actionWidgets", message.layout().getList("actionWidgets", Tag.TAG_COMPOUND));
            }
        }));
    }

}
