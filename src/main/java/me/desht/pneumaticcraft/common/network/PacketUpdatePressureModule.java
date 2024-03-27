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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client when module settings updated via GUI
 */
public record PacketUpdatePressureModule(ModuleLocator locator, float lower, float higher, boolean advanced) implements TubeModulePacket<AbstractTubeModule> {
    public static final ResourceLocation ID = RL("update_pressure_module");

    public static PacketUpdatePressureModule create(AbstractTubeModule module) {
        return new PacketUpdatePressureModule(ModuleLocator.forModule(module), module.lowerBound, module.higherBound, module.advancedConfig);
    }

    public static PacketUpdatePressureModule fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdatePressureModule(ModuleLocator.fromNetwork(buffer), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        locator.write(buffer);
        buffer.writeFloat(lower);
        buffer.writeFloat(higher);
        buffer.writeBoolean(advanced);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void onModuleUpdate(AbstractTubeModule module, Player player) {
        module.lowerBound = lower;
        module.higherBound = higher;
        module.advancedConfig = advanced;
    }
}
