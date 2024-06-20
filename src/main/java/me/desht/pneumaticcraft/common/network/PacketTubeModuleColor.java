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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client when logistics module colour is updated via GUI
 */
public record PacketTubeModuleColor<T extends AbstractTubeModule & INetworkedModule>(ModuleLocator locator, int color) implements TubeModulePacket<T> {
    public static final Type<PacketTubeModuleColor<?>> TYPE = new Type<>(RL("tube_module_color"));

    public static final StreamCodec<FriendlyByteBuf, PacketTubeModuleColor<?>> STREAM_CODEC = StreamCodec.composite(
            ModuleLocator.STREAM_CODEC, PacketTubeModuleColor::locator,
            ByteBufCodecs.INT, PacketTubeModuleColor::color,
            PacketTubeModuleColor::new
    );

    public static <T extends AbstractTubeModule & INetworkedModule> PacketTubeModuleColor<T> forModule(T module) {
        return new PacketTubeModuleColor<>(ModuleLocator.forModule(module), module.getColorChannel());
    }

    @Override
    public void onModuleUpdate(AbstractTubeModule module, Player player) {
        if (module instanceof INetworkedModule net) {
            net.setColorChannel(color);
        }
    }

    @Override
    public Type<PacketTubeModuleColor<?>> type() {
        return TYPE;
    }
}
