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
import me.desht.pneumaticcraft.common.block.entity.ProgrammableControllerBlockEntity;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class PacketDroneDebugBase {
    final int entityId;
    final BlockPos pos;

    public PacketDroneDebugBase(IDroneBase drone) {
        if (drone instanceof DroneEntity) {
            entityId = ((DroneEntity) drone).getId();
            pos = null;
        } else if (drone instanceof ProgrammableControllerBlockEntity) {
            pos = ((ProgrammableControllerBlockEntity) drone).getBlockPos();
            entityId = -1;
        } else {
            throw new IllegalArgumentException("drone must be an EntityDrone or ProgrammableControllerBlockEntity!");
        }
    }

    public PacketDroneDebugBase(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            entityId = buffer.readInt();
            pos = null;
        } else {
            pos = buffer.readBlockPos();
            entityId = -1;
        }
    }

    PacketDroneDebugBase(int entityId, BlockPos pos) {
        this.entityId = entityId;
        this.pos = pos;
    }

    public void toBytes(FriendlyByteBuf buf) {
        if (pos != null) {
            buf.writeBoolean(false);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(true);
            buf.writeInt(entityId);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level world = ctx.get().getSender() == null ? ClientUtils.getClientLevel() : ctx.get().getSender().level();
            Player player =  ctx.get().getSender() == null ? ClientUtils.getClientPlayer() : ctx.get().getSender();
            if (entityId >= 0) {
                Entity entity = world.getEntity(entityId);
                if (entity instanceof DroneEntity) {
                    handle(player, (IDroneBase) entity);
                }
            } else if (pos != null) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof ProgrammableControllerBlockEntity) {
                    handle(player, (IDroneBase) te);
                }
            } else {
                handle(player, null);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    abstract void handle(Player player, IDroneBase drone);

}
