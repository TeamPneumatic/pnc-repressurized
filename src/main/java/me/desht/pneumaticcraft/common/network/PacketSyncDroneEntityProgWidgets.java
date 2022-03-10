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

import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.List;

/**
 * Received on: CLIENT
 * Sent by server to sync a (debugged) drone's programming widgets
 */
public class PacketSyncDroneEntityProgWidgets extends PacketDroneDebugBase {
    private final List<IProgWidget> progWidgets;

    public PacketSyncDroneEntityProgWidgets(IDroneBase drone) {
        super(drone);
        progWidgets = drone.getActiveAIManager().getProgWidgets();
    }

    PacketSyncDroneEntityProgWidgets(PacketBuffer buffer) {
        super(buffer);
        progWidgets = TileEntityProgrammer.getWidgetsFromNBT(buffer.readNbt());
    }

    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeNbt(TileEntityProgrammer.putWidgetsToNBT(progWidgets, new CompoundNBT()));
    }

    @Override
    void handle(PlayerEntity player, IDroneBase droneBase) {
        List<IProgWidget> widgets = droneBase.getProgWidgets();
        widgets.clear();
        widgets.addAll(progWidgets);
        DroneDebugClientHandler.onWidgetsChanged();
    }
}
