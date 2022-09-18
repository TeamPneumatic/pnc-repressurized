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
import net.minecraft.world.entity.player.Player;

/**
 * Received on: SERVER
 * Sent by client when logistics module colour is updated via GUI
 */
public class PacketTubeModuleColor extends PacketUpdateTubeModule {
    private final int color;

    public PacketTubeModuleColor(AbstractTubeModule module) {
        super(module);

        this.color = ((INetworkedModule) module).getColorChannel();
    }

    PacketTubeModuleColor(FriendlyByteBuf buffer) {
        super(buffer);

        this.color = buffer.readByte();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);

        buf.writeByte(color);
    }

    @Override
    protected void onModuleUpdate(AbstractTubeModule module, Player player) {
        if (module instanceof INetworkedModule net) {
            net.setColorChannel(color);
        }
    }
}
