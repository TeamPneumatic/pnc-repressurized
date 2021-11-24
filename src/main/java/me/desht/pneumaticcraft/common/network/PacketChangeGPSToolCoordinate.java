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

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Send when the GPS Tool GUI is closed, to update the held GPS tool settings
 */
public class PacketChangeGPSToolCoordinate extends LocationIntPacket {
    private final Hand hand;
    private final String variable;
    private final int index;

    public PacketChangeGPSToolCoordinate(BlockPos pos, Hand hand, String variable, int index) {
        super(pos);
        this.hand = hand;
        this.variable = variable;
        this.index = index;
    }

    public PacketChangeGPSToolCoordinate(PacketBuffer buf) {
        super(buf);
        variable = buf.readUtf(32767);
        index = buf.readByte();
        hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeUtf(variable);
        buf.writeByte(index);
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            ItemStack playerStack = player.getItemInHand(hand);
            if (playerStack.getItem() == ModItems.GPS_TOOL.get()) {
                ItemGPSTool.setVariable(playerStack, variable);
                if (pos.getY() >= 0) {
                    ItemGPSTool.setGPSLocation(playerStack, pos);
                }
            } else if (playerStack.getItem() == ModItems.GPS_AREA_TOOL.get()) {
                ItemGPSAreaTool.setVariable(playerStack, variable, index);
                if (pos.getY() >= 0) {
                    ItemGPSAreaTool.setGPSPosAndNotify(player, pos, hand, index);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
