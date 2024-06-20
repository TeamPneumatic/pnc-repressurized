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

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to sync a (debugged) drone's programming widgets
 */
public record PacketSyncDroneProgWidgets(DroneTarget droneTarget, List<IProgWidget> progWidgets) implements DronePacket {
    public static final Type<PacketSyncDroneProgWidgets> TYPE = new Type<>(RL("sync_drone_prog_widgets"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncDroneProgWidgets> STREAM_CODEC = StreamCodec.composite(
            DroneTarget.STREAM_CODEC, PacketSyncDroneProgWidgets::droneTarget,
            ProgWidget.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketSyncDroneProgWidgets::progWidgets,
            PacketSyncDroneProgWidgets::new
    );

    public static PacketSyncDroneProgWidgets create(IDroneBase drone) {
        return new PacketSyncDroneProgWidgets(drone.getPacketTarget(), drone.getActiveAIManager().widgets());
    }

    @Override
    public Type<PacketSyncDroneProgWidgets> type() {
        return TYPE;
    }

    @Override
    public void handle(Player player, IDroneBase droneBase) {
        List<IProgWidget> widgets = droneBase.getProgWidgets();
        widgets.clear();
        widgets.addAll(progWidgets);
        DroneDebugClientHandler.onWidgetsChanged();
    }
}
