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

import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;

public class PacketUpdatePressureModule extends PacketUpdateTubeModule {
    private final float lower;
    private final float higher;
    private final boolean advanced;

    public PacketUpdatePressureModule(TubeModule module) {
        super(module);
        this.lower = module.lowerBound;
        this.higher = module.higherBound;
        this.advanced = module.advancedConfig;
    }

    public PacketUpdatePressureModule(FriendlyByteBuf buffer) {
        super(buffer);
        this.lower = buffer.readFloat();
        this.higher = buffer.readFloat();
        this.advanced = buffer.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeFloat(lower);
        buffer.writeFloat(higher);
        buffer.writeBoolean(advanced);
    }

    @Override
    protected void onModuleUpdate(TubeModule module, Player player) {
        module.lowerBound = lower;
        module.higherBound = higher;
        module.advancedConfig = advanced;
    }
}
