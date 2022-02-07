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

import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

/**
 * Received on: SERVER
 * Update the entity filter of an air grate module
 */
public class PacketUpdateAirGrateModule extends PacketUpdateTubeModule {
    private final String entityFilter;

    public PacketUpdateAirGrateModule(TubeModule module, String entityFilter) {
        super(module);
        this.entityFilter = entityFilter;
    }

    public PacketUpdateAirGrateModule(FriendlyByteBuf buffer) {
        super(buffer);
        entityFilter = buffer.readUtf(32767);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeUtf(entityFilter);
    }

    @Override
    protected void onModuleUpdate(TubeModule module, Player player) {
        if (module instanceof ModuleAirGrate && module.isUpgraded()) {
            ((ModuleAirGrate) module).setEntityFilter(entityFilter);
        }
    }
}
