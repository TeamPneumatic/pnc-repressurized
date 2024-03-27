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

import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import me.desht.pneumaticcraft.common.tubemodules.INetworkedModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client when logistics module colour is updated via GUI
 */
public record PacketTubeModuleColor<T extends AbstractTubeModule & INetworkedModule>(ModuleLocator locator, int color) implements TubeModulePacket<T> {
    public static final ResourceLocation ID = RL("tube_module_color");

    public static <T extends AbstractTubeModule & INetworkedModule> PacketTubeModuleColor<T> create(T module) {
        return new PacketTubeModuleColor<>(ModuleLocator.forModule(module), module.getColorChannel());
    }

    public static <T extends AbstractTubeModule & INetworkedModule> PacketTubeModuleColor<T> fromNetwork(FriendlyByteBuf buffer) {
        return new PacketTubeModuleColor<>(ModuleLocator.fromNetwork(buffer), buffer.readByte());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        locator.write(buf);
        buf.writeByte(color);
    }

    @Override
    public void onModuleUpdate(AbstractTubeModule module, Player player) {
        if (module instanceof INetworkedModule net) {
            net.setColorChannel(color);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
