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
import me.desht.pneumaticcraft.common.entity.EntityRing;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to get the client to spawn a new client-side ring entity
 */
public class PacketSpawnRing extends LocationDoublePacket {
    private final int[] colors;
    private final int targetEntityId;

    public PacketSpawnRing(double x, double y, double z, Entity targetEntity, int... colors) {
        super(x, y, z);
        targetEntityId = targetEntity.getId();
        this.colors = colors;
    }

    public PacketSpawnRing(FriendlyByteBuf buffer) {
        super(buffer);
        targetEntityId = buffer.readInt();
        colors = new int[buffer.readVarInt()];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = buffer.readInt();
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(targetEntityId);
        buffer.writeVarInt(colors.length);
        Arrays.stream(colors).forEach(buffer::writeInt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level world = ClientUtils.getClientLevel();
            Entity entity = world.getEntity(targetEntityId);
            if (entity != null) {
                for (int color : colors) {
                    ClientUtils.spawnEntityClientside(new EntityRing(world, x, y, z, entity, color));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
