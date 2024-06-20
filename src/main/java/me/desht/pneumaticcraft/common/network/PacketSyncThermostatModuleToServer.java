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

import me.desht.pneumaticcraft.common.tubemodules.ThermostatModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to update server-side settings when redstone module GUI is closed
 */
public record PacketSyncThermostatModuleToServer(ModuleLocator locator, byte channel, int threshold) implements TubeModulePacket<ThermostatModule> {
    public static Type<PacketSyncThermostatModuleToServer> TYPE = new Type<>(RL("sync_thermostat_module_to_server"));

    public static StreamCodec<FriendlyByteBuf, PacketSyncThermostatModuleToServer> STREAM_CODEC = StreamCodec.composite(
            ModuleLocator.STREAM_CODEC, PacketSyncThermostatModuleToServer::locator,
            ByteBufCodecs.BYTE, PacketSyncThermostatModuleToServer::channel,
            ByteBufCodecs.VAR_INT, PacketSyncThermostatModuleToServer::threshold,
            PacketSyncThermostatModuleToServer::new
    );

    public static PacketSyncThermostatModuleToServer create(ThermostatModule module) {
        return new PacketSyncThermostatModuleToServer(ModuleLocator.forModule(module), (byte) module.getColorChannel(), module.getThreshold());
    }

    @Override
    public Type<PacketSyncThermostatModuleToServer> type() {
        return TYPE;
    }

    @Override
    public void onModuleUpdate(ThermostatModule module, Player player) {
        if (PneumaticCraftUtils.canPlayerReach(player, module.getTube().getBlockPos())) {
            module.setColorChannel(channel);
            module.setThreshold(threshold);
            module.updateNeighbors();
            module.setUpdate(true); // Force recalc
        }
    }
}
