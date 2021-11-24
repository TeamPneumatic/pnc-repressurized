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

import me.desht.pneumaticcraft.common.item.ItemRemote;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update the layout of a Remote item from the Remote GUI
 */
public class PacketUpdateRemoteLayout {
    private final CompoundNBT layout;
    private final Hand hand;

    public PacketUpdateRemoteLayout(CompoundNBT layout, Hand hand) {
        this.layout = layout;
        this.hand = hand;
    }

    public PacketUpdateRemoteLayout(PacketBuffer buffer) {
        this.layout = buffer.readNbt();
        this.hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeNbt(layout);
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack remote = ctx.get().getSender().getItemInHand(hand);
            if (remote.getItem() instanceof ItemRemote) {
                CompoundNBT tag = remote.getTag();
                if (tag == null) {
                    tag = new CompoundNBT();
                    remote.setTag(tag);
                }
                tag.put("actionWidgets", layout.getList("actionWidgets", Constants.NBT.TAG_COMPOUND));
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
