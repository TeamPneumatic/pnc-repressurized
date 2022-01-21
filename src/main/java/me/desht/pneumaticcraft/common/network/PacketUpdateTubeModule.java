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
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when tube module settings are updated via GUI.
 */
public abstract class PacketUpdateTubeModule extends LocationIntPacket {
    private final Direction moduleSide;

    public PacketUpdateTubeModule(TubeModule module) {
        super(module.getTube().getBlockPos());
        this.moduleSide = module.getDirection();
    }

    public PacketUpdateTubeModule(FriendlyByteBuf buffer) {
        super(buffer);
        this.moduleSide = Direction.from3DDataValue(buffer.readByte());
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeByte((byte) moduleSide.get3DDataValue());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player != null) {
                PneumaticCraftUtils.getTileEntityAt(player.getCommandSenderWorld(), pos, TileEntityPressureTube.class).ifPresent(te -> {
                    TubeModule tm = te.getModule(moduleSide);
                    if (tm != null && PneumaticCraftUtils.canPlayerReach(player, te.getBlockPos())) {
                        onModuleUpdate(tm, player);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    protected abstract void onModuleUpdate(TubeModule module, Player player);

}
