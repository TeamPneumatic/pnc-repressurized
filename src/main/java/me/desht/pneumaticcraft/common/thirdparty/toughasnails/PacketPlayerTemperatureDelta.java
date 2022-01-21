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

package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.AirConClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when air conditioning level changes so client can update the HUD gauge
 */
public class PacketPlayerTemperatureDelta {
    private int deltaTemp;

    public PacketPlayerTemperatureDelta() {
        // empty
    }

    PacketPlayerTemperatureDelta(int deltaTemp) {
        this.deltaTemp = deltaTemp;
    }

    PacketPlayerTemperatureDelta(FriendlyByteBuf buffer) {
        deltaTemp = buffer.readByte();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeByte(deltaTemp);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> AirConClientHandler.deltaTemp = deltaTemp);
        ctx.get().setPacketHandled(true);
    }
}
