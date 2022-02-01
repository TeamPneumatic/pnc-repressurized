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

import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to spawn coloured redstone particles in multiple positions around an initial position
 */
public class PacketSpawnIndicatorParticles {
    private final BlockPos pos0;
    private final DyeColor dyeColor;
    private final List<ByteOffset> offsets = new ArrayList<>();

    public PacketSpawnIndicatorParticles(List<BlockPos> posList, DyeColor dyeColor) {
        this.pos0 = posList.get(0);
        this.dyeColor = dyeColor;
        for (int i = 1; i < posList.size(); i++) {
            BlockPos off = posList.get(i).subtract(pos0);
            if (off.getX() >= -128 && off.getX() <= 127 && off.getY() >= -128 && off.getY() <= 127 && off.getZ() >= -128 && off.getZ() <= 127) {
                offsets.add(new ByteOffset(off.getX(), off.getY(), off.getZ()));
            }
        }
    }

    public PacketSpawnIndicatorParticles(FriendlyByteBuf buffer) {
        pos0 = buffer.readBlockPos();
        int nOffsets = buffer.readVarInt();
        for (int i = 0; i < nOffsets; i++) {
            offsets.add(new ByteOffset(buffer.readByte(), buffer.readByte(), buffer.readByte()));
        }
        dyeColor = DyeColor.byId(buffer.readVarInt());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos0);
        buffer.writeVarInt(offsets.size());
        for (ByteOffset offset : offsets) {
            buffer.writeByte(offset.x);
            buffer.writeByte(offset.y);
            buffer.writeByte(offset.z);
        }
        buffer.writeVarInt(dyeColor.getId());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level world = ClientUtils.getClientLevel();
            float[] cols = dyeColor.getTextureDiffuseColors();
            ParticleOptions particle = new DustParticleOptions(new Vector3f(cols[0], cols[1], cols[2]), 1f);
            world.addParticle(particle, pos0.getX() + 0.5, pos0.getY() + 0.5, pos0.getZ() + 0.5, 0, 0, 0);
            for (ByteOffset offset : offsets) {
                world.addParticle(particle, pos0.getX() + offset.x + 0.5, pos0.getY() + offset.y + 0.5, pos0.getZ() + offset.z + 0.5, 0, 0, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ByteOffset {
        private final byte x, y, z;

        private ByteOffset(int x, int y, int z) {
            this.x = (byte) x;
            this.y = (byte) y;
            this.z = (byte) z;
        }
    }
}
