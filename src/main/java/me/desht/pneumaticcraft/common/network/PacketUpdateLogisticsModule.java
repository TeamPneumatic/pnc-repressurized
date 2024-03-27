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

import me.desht.pneumaticcraft.common.tubemodules.LogisticsModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when the status or colour of a logistics module is updated
 */
public record PacketUpdateLogisticsModule(ModuleLocator locator, int colorIndex, int status) implements TubeModulePacket<LogisticsModule> {
    public static final ResourceLocation ID = RL("update_logsistics_module");

    public static PacketUpdateLogisticsModule create(LogisticsModule module, int action) {
        int status = action > 0 ? 1 + action : module.hasPower() ? 1 : 0;
        return new PacketUpdateLogisticsModule(ModuleLocator.forModule(module), module.getColorChannel(), status);
    }

    public static PacketUpdateLogisticsModule fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdateLogisticsModule(ModuleLocator.fromNetwork(buffer), buffer.readByte(), buffer.readByte());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        locator.write(buf);
        buf.writeByte(colorIndex);
        buf.writeByte(status);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void onModuleUpdate(LogisticsModule module, Player player) {
        module.onUpdatePacket(status, colorIndex);
    }
}
