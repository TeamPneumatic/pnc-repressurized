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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Send when the GPS Tool GUI is closed, to update the held GPS tool settings
 */
public record PacketChangeGPSToolCoordinate(BlockPos pos, InteractionHand hand, String variable, int index) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("change_gps_tool_coord");

    public PacketChangeGPSToolCoordinate(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readEnum(InteractionHand.class), buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN), buf.readByte());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(hand);
        buf.writeUtf(variable);
        buf.writeByte(index);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketChangeGPSToolCoordinate message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            ItemStack stack = player.getItemInHand(message.hand);
            if (stack.getItem() instanceof IGPSToolSync sync) {
                sync.syncFromClient(player, stack, message.index, message.pos, message.variable);
            }
        }));
    }
}
