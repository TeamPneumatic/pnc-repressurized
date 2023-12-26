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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to sync up the settings of a redstone module
 */
public record PacketSyncThermostatModuleToClient(ModuleLocator locator, int channel, int level, int temperature) implements TubeModulePacket<ThermostatModule> {
    public static final ResourceLocation ID = RL("sync_thermostat_module_to_client");

    public static PacketSyncThermostatModuleToClient create(ThermostatModule module) {
        return new PacketSyncThermostatModuleToClient(
                ModuleLocator.forModule(module),
                module.getColorChannel(),
                module.getLevel(),
                module.getTemperature()
        );
    }

    public static PacketSyncThermostatModuleToClient fromNetwork(FriendlyByteBuf buffer) {
        return new PacketSyncThermostatModuleToClient(
                ModuleLocator.fromNetwork(buffer),
                buffer.readByte(),
                buffer.readInt(),
                buffer.readInt()
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        locator.write(buf);
        buf.writeByte(channel);
        buf.writeInt(level);
        buf.writeInt(temperature);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void onModuleUpdate(ThermostatModule module, Player player) {
        module.setColorChannel(channel);
        module.setLevel(level);
        module.setTemperature(temperature);
    }
}
