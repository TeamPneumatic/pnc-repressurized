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

import me.desht.pneumaticcraft.common.item.IGPSToolSync;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Send when the GPS Tool GUI is closed, to update the held GPS tool settings
 */
public class PacketChangeGPSToolCoordinate extends LocationIntPacket {
    private final InteractionHand hand;
    private final String variable;
    private final int index;

    public PacketChangeGPSToolCoordinate(BlockPos pos, InteractionHand hand, String variable, int index) {
        super(pos);
        this.hand = hand;
        this.variable = variable;
        this.index = index;
    }

    public PacketChangeGPSToolCoordinate(FriendlyByteBuf buf) {
        super(buf);
        variable = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
        index = buf.readByte();
        hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeUtf(variable);
        buf.writeByte(index);
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof IGPSToolSync sync) {
                    sync.syncFromClient(player, stack, index, pos, variable);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
