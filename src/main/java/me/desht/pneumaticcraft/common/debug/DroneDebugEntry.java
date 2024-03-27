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

package me.desht.pneumaticcraft.common.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class DroneDebugEntry {
    private final int progWidgetId;
    private final String message;
    private final BlockPos pos;
    private long receivedTime; // timestamp for when packet was received on client

    /**
     * Called server side when a debug message is added to a drone.
     *
     * @param message the message text
     * @param progWidgetId a programming widget ID
     * @param pos block position
     */
    public DroneDebugEntry(String message, int progWidgetId, BlockPos pos) {
        this.message = message;
        this.pos = pos != null ? pos : BlockPos.ZERO;
        this.progWidgetId = progWidgetId;
    }

    /**
     * Called client-side when a message is synced.
     *
     * @param buf message buffer
     */
    public DroneDebugEntry(FriendlyByteBuf buf) {
        message = buf.readUtf();
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        progWidgetId = buf.readInt();
        receivedTime = System.currentTimeMillis();
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(message);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(progWidgetId);
    }

    public String getMessage() {
        return message;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getProgWidgetId() {
        return progWidgetId;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public boolean hasCoords() {
        return pos.getX() != 0 || pos.getY() != 0 || pos.getZ() != 0;
    }

}
