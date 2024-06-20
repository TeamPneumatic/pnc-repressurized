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

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client when the drone debug key is pressed, for a valid entity or programmable controller target
 */
public record PacketUpdateDebuggingDrone(DroneTarget droneTarget) implements DronePacket {
    public static final Type<PacketUpdateDebuggingDrone> TYPE = new Type<>(RL("update_debugging_drone"));

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateDebuggingDrone> STREAM_CODEC = StreamCodec.composite(
            DroneTarget.STREAM_CODEC, PacketUpdateDebuggingDrone::droneTarget,
            PacketUpdateDebuggingDrone::new
    );

    public static PacketUpdateDebuggingDrone create(IDroneBase drone) {
        return new PacketUpdateDebuggingDrone(drone == null ? DroneTarget.none() : drone.getPacketTarget());
    }

    @Override
    public Type<PacketUpdateDebuggingDrone> type() {
        return TYPE;
    }

    @Override
    public void handle(Player player, IDroneBase droneBase) {
        if (player instanceof ServerPlayer) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.upgradeUsable(CommonUpgradeHandlers.droneDebugHandler, false)) {
                ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
                if (droneBase == null) {
                    stack.remove(ModDataComponents.DRONE_DEBUG_TARGET);
                } else {
                    droneBase.storeTrackerData(stack);
                    droneBase.getDebugger().trackAsDebugged((ServerPlayer) player);
                }
            }
        }
    }
}
