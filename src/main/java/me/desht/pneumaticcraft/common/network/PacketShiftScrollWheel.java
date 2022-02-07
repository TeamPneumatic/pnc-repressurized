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

import me.desht.pneumaticcraft.common.item.IShiftScrollable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent by client when player shift-scrolls the mouse wheel while holding an item implementing the
 * {@link me.desht.pneumaticcraft.common.item.IShiftScrollable} interface.
 */
public class PacketShiftScrollWheel {
    private final boolean forward;
    private final boolean mainHand;

    public PacketShiftScrollWheel(boolean forward, InteractionHand mainHand) {
        this.forward = forward;
        this.mainHand = mainHand == InteractionHand.MAIN_HAND;
    }

    public PacketShiftScrollWheel(FriendlyByteBuf buf) {
        this.forward = buf.readBoolean();
        this.mainHand = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(forward);
        buf.writeBoolean(mainHand);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof IShiftScrollable) {
                    ((IShiftScrollable) stack.getItem()).onShiftScrolled(player, forward, mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
