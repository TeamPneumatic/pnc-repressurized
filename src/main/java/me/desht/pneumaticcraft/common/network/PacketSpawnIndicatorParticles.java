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

import me.desht.pneumaticcraft.client.util.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to spawn coloured redstone particles in multiple positions around an initial position
 */
public record PacketSpawnIndicatorParticles(BlockPos pos0, DyeColor dyeColor, List<ByteOffset> offsets) implements CustomPacketPayload {
    public static final Type<PacketSpawnIndicatorParticles> TYPE = new Type<>(RL("indicator_particles"));

    public static final StreamCodec<FriendlyByteBuf, PacketSpawnIndicatorParticles> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketSpawnIndicatorParticles::pos0,
            NeoForgeStreamCodecs.enumCodec(DyeColor.class), PacketSpawnIndicatorParticles::dyeColor,
            ByteOffset.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketSpawnIndicatorParticles::offsets,
            PacketSpawnIndicatorParticles::new
    );

    public static PacketSpawnIndicatorParticles create(List<BlockPos> posList, DyeColor dyeColor) {
        BlockPos pos0 = posList.get(0);
        List<ByteOffset> offsets = new ArrayList<>();
        for (int i = 1; i < posList.size(); i++) {
            BlockPos off = posList.get(i).subtract(pos0);
            if (off.getX() >= -128 && off.getX() <= 127 && off.getY() >= -128 && off.getY() <= 127 && off.getZ() >= -128 && off.getZ() <= 127) {
                offsets.add(new ByteOffset(off.getX(), off.getY(), off.getZ()));
            }
        }

        return new PacketSpawnIndicatorParticles(pos0, dyeColor, offsets);
    }

    @Override
    public Type<PacketSpawnIndicatorParticles> type() {
        return TYPE;
    }

    public static void handle(PacketSpawnIndicatorParticles message, IPayloadContext ctx) {
        Level world = ctx.player().level();
        float[] cols = RenderUtils.decomposeColorF(message.dyeColor().getTextureDiffuseColor());
        ParticleOptions particle = new DustParticleOptions(new Vector3f(cols[0], cols[1], cols[2]), 1f);
        BlockPos pos0 = message.pos0();
        world.addParticle(particle, pos0.getX() + 0.5, pos0.getY() + 0.5, pos0.getZ() + 0.5, 0, 0, 0);
        for (ByteOffset offset : message.offsets()) {
            world.addParticle(particle,
                    pos0.getX() + offset.x + 0.5,
                    pos0.getY() + offset.y + 0.5,
                    pos0.getZ() + offset.z + 0.5,
                    0, 0, 0
            );
        }
    }

    private record ByteOffset(byte x, byte y, byte z) {
        public static StreamCodec<FriendlyByteBuf, ByteOffset> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE, ByteOffset::x,
                ByteBufCodecs.BYTE, ByteOffset::y,
                ByteBufCodecs.BYTE, ByteOffset::z,
                ByteOffset::new
        );

        public ByteOffset(int x, int y, int z) {
            this((byte) x, (byte) y, (byte) z);
        }
    }
}
