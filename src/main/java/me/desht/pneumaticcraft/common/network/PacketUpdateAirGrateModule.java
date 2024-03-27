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
import me.desht.pneumaticcraft.common.tubemodules.AirGrateModule;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Update the entity filter of an air grate module
 */
public record PacketUpdateAirGrateModule(ModuleLocator locator, String entityFilter) implements TubeModulePacket<AirGrateModule> {
    public static final ResourceLocation ID = RL("update_air_grate");

    public static PacketUpdateAirGrateModule create(AbstractTubeModule module, String entityFilter) {
        return new PacketUpdateAirGrateModule(ModuleLocator.forModule(module), entityFilter);
    }

    public static PacketUpdateAirGrateModule fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdateAirGrateModule(ModuleLocator.fromNetwork(buffer), buffer.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        locator.write(buffer);
        buffer.writeUtf(entityFilter);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void onModuleUpdate(AirGrateModule module, Player player) {
        if (module.isUpgraded()) {
            try {
                module.setEntityFilter(new EntityFilter(entityFilter));
            } catch (IllegalArgumentException e) {
                Log.warning("ignoring invalid entity filter " + entityFilter + " (" + e.getMessage() + ")");
            }
        }
    }
}
