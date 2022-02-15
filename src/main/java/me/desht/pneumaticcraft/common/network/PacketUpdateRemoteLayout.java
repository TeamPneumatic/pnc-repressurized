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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update the layout of a Remote item from the Remote GUI
 */
public class PacketUpdateRemoteLayout {
    private final CompoundTag layout;
    private final InteractionHand hand;

    public PacketUpdateRemoteLayout(CompoundTag layout, InteractionHand hand) {
        this.layout = layout;
        this.hand = hand;
    }

    public PacketUpdateRemoteLayout(FriendlyByteBuf buffer) {
        this.layout = buffer.readNbt();
        this.hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(layout);
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack remote = ctx.get().getSender().getItemInHand(hand);
            if (remote.getItem() instanceof RemoteItem) {
                CompoundTag tag = remote.getTag();
                if (tag == null) {
                    tag = new CompoundTag();
                    remote.setTag(tag);
                }
                tag.put("actionWidgets", layout.getList("actionWidgets", Tag.TAG_COMPOUND));
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
