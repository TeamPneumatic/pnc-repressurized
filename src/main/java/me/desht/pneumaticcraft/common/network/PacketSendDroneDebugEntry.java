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

import me.desht.pneumaticcraft.common.debug.DroneDebugEntry;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

/**
 * Received on: CLIENT
 * Sent by server to add a debug message to a debugged drone.
 */
public class PacketSendDroneDebugEntry extends PacketDroneDebugBase {
    private final DroneDebugEntry entry;

    public PacketSendDroneDebugEntry(DroneDebugEntry entry, IDroneBase drone) {
        super(drone);
        this.entry = entry;
    }

    public PacketSendDroneDebugEntry(FriendlyByteBuf buffer) {
        super(buffer);
        entry = new DroneDebugEntry(buffer);
    }

    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        entry.toBytes(buf);
    }

    @Override
    void handle(Player player, IDroneBase drone) {
        drone.getDebugger().addEntry(entry);
    }
}
