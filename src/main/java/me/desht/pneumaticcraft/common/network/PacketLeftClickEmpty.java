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

import me.desht.pneumaticcraft.common.item.ILeftClickableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when certain items are left-clicked in the air (which is a client-only event)
 */
public class PacketLeftClickEmpty {
    public PacketLeftClickEmpty() {
    }

    @SuppressWarnings("EmptyMethod")
    public PacketLeftClickEmpty(@SuppressWarnings("unused") PacketBuffer buf) {
    }

    @SuppressWarnings("EmptyMethod")
    public void toBytes(@SuppressWarnings("unused") PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                ItemStack stack = ctx.get().getSender().getMainHandItem();
                if (stack.getItem() instanceof ILeftClickableItem) {
                    ((ILeftClickableItem) stack.getItem()).onLeftClickEmpty(ctx.get().getSender());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
