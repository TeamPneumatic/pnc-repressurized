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

import me.desht.pneumaticcraft.api.drone.debug.DroneDebugEntry;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to add a debug message to a debugged drone.
 */
public record PacketSendDroneDebugEntry(DroneTarget droneTarget, DroneDebugEntry entry) implements DronePacket {
    public static final Type<PacketSendDroneDebugEntry> TYPE = new Type<>(RL("send_drone_debug_entry"));

    public static final StreamCodec<FriendlyByteBuf, PacketSendDroneDebugEntry> STREAM_CODEC = StreamCodec.composite(
            DroneTarget.STREAM_CODEC, PacketSendDroneDebugEntry::droneTarget,
            DroneDebugEntry.STREAM_CODEC, PacketSendDroneDebugEntry::entry,
            PacketSendDroneDebugEntry::new
    );

    public static PacketSendDroneDebugEntry create(IDroneBase drone, DroneDebugEntry entry) {
        return new PacketSendDroneDebugEntry(drone.getPacketTarget(), entry);
    }

    @Override
    public Type<PacketSendDroneDebugEntry> type() {
        return TYPE;
    }

    @Override
    public void handle(Player player, IDroneBase drone) {
        drone.getDebugger().addEntry(entry);
    }
}
