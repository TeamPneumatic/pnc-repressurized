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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to start a new MovingSound playing
 */
public class PacketPlayMovingSound {
    private final MovingSounds.Sound sound;
    private final MovingSoundFocus source;

    public PacketPlayMovingSound(MovingSounds.Sound sound, MovingSoundFocus source) {
        this.sound = sound;
        this.source = source;
    }

    public PacketPlayMovingSound(FriendlyByteBuf buffer) {
        sound = buffer.readEnum(MovingSounds.Sound.class);
        source = MovingSoundFocus.fromBytes(buffer);
    }

    public void toBytes(FriendlyByteBuf buffer) {
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
        ENTITY(buf -> MovingSoundFocus.of(buf.readInt())),
        STATIC_POS(buf -> MovingSoundFocus.of(buf.readBlockPos()));

        private final Function<FriendlyByteBuf, MovingSoundFocus> creator;

        SourceType(Function<FriendlyByteBuf, MovingSoundFocus> creator) {
            this.creator = creator;
        }

        public MovingSoundFocus getSource(FriendlyByteBuf buf) {
            return creator.apply(buf);
        }
    }

    public static class MovingSoundFocus {
        private final Either<Entity,BlockPos> entityOrPos;

        private MovingSoundFocus(Either<Entity, BlockPos> entityOrPos) {
            this.entityOrPos = entityOrPos;
        }

        public static MovingSoundFocus of(Entity e) {
            return new MovingSoundFocus(Either.left(e));
        }

        public static MovingSoundFocus of(int id) {
            Entity e = ClientUtils.getClientLevel().getEntity(id);
            return e == null ? null : of(e);
        }

        public static MovingSoundFocus of(BlockPos pos) {
            return new MovingSoundFocus(Either.right(pos));
        }

        public static MovingSoundFocus of(BlockEntity te) {
            return new MovingSoundFocus(Either.right(te.getBlockPos()));
        }

        public static MovingSoundFocus fromBytes(FriendlyByteBuf buf) {
            SourceType type = buf.readEnum(SourceType.class);
            return type.getSource(buf);
        }

        void toBytes(FriendlyByteBuf buf) {
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
