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

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to start a new MovingSound playing
 */
public class PacketPlayMovingSound {
    private final MovingSounds.Sound sound;
    private final SoundSource source;

    public PacketPlayMovingSound(MovingSounds.Sound sound, SoundSource source) {
        this.sound = sound;
        this.source = source;
    }

    public PacketPlayMovingSound(PacketBuffer buffer) {
        sound = buffer.readEnum(MovingSounds.Sound.class);
        source = SoundSource.fromBytes(buffer);
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeEnum(sound);
        source.toBytes(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (source != null) source.handle(sound);
        });
        ctx.get().setPacketHandled(true);
    }

    private enum SourceType {
        ENTITY(buf -> SoundSource.of(buf.readInt())),
        STATIC_POS(buf -> SoundSource.of(buf.readBlockPos()));

        private final Function<PacketBuffer, SoundSource> creator;

        SourceType(Function<PacketBuffer,SoundSource> creator) {
            this.creator = creator;
        }

        public SoundSource getSource(PacketBuffer buf) {
            return creator.apply(buf);
        }
    }

    public static class SoundSource {
        private final Either<Entity,BlockPos> entityOrPos;

        private SoundSource(Either<Entity, BlockPos> entityOrPos) {
            this.entityOrPos = entityOrPos;
        }

        public static SoundSource of(Entity e) {
            return new SoundSource(Either.left(e));
        }

        public static SoundSource of(int id) {
            Entity e = ClientUtils.getClientWorld().getEntity(id);
            return e == null ? null : of(e);
        }

        public static SoundSource of(BlockPos pos) {
            return new SoundSource(Either.right(pos));
        }

        public static SoundSource of(TileEntity te) {
            return new SoundSource(Either.right(te.getBlockPos()));
        }

        public static SoundSource fromBytes(PacketBuffer buf) {
            SourceType type = buf.readEnum(SourceType.class);
            return type.getSource(buf);
        }

        void toBytes(PacketBuffer buf) {
            entityOrPos.ifLeft(id -> {
                buf.writeEnum(SourceType.ENTITY);
                buf.writeInt(id.getId());
            }).ifRight(pos -> {
                buf.writeEnum(SourceType.STATIC_POS);
                buf.writeBlockPos(pos);
            });
        }

        public void handle(MovingSounds.Sound sound) {
            entityOrPos
                    .ifLeft(e -> MovingSounds.playMovingSound(sound, e))
                    .ifRight(pos -> MovingSounds.playMovingSound(sound, pos));
        }

        public Either<Entity,BlockPos> asEntityOrPos() {
            return entityOrPos;
        }
    }
}
