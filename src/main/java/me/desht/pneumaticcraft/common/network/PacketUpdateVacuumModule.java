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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.tubemodules.VacuumModule;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to tell client if Vacuum module needs to spin
 */
public class PacketUpdateVacuumModule extends LocationIntPacket {
    private final int lastAmount;
    private final Direction side;

    public PacketUpdateVacuumModule(VacuumModule module) {
        super(module.getTube().getBlockPos());
        this.lastAmount = module.getLastAmount();
        this.side = module.getDirection();
    }

    public PacketUpdateVacuumModule(FriendlyByteBuf buffer) {
        super(buffer);
        this.lastAmount = buffer.readInt();
        this.side = buffer.readEnum(Direction.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(lastAmount);
        buffer.writeEnum(side);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PneumaticCraftUtils.getTileEntityAt(ClientUtils.getClientLevel(), pos, PressureTubeBlockEntity.class).ifPresent(te -> {
                if (te.getModule(side) instanceof VacuumModule vac) {
                    vac.setLastAmount(lastAmount);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
