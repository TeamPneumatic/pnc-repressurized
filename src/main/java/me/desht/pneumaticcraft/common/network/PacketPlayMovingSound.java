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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.function.Function;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to start a new MovingSound playing
 */
public record PacketPlayMovingSound(MovingSounds.Sound sound, MovingSoundFocus source) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("play_moving_sound");

    public static PacketPlayMovingSound fromNetwork(FriendlyByteBuf buffer) {
        return new PacketPlayMovingSound(buffer.readEnum(MovingSounds.Sound.class), MovingSoundFocus.fromNetwork(buffer));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(sound);
        source.toNetwork(buffer);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketPlayMovingSound message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (message.source() != null) message.source().handle(message.sound());
        });
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

    public record MovingSoundFocus(Either<Entity,BlockPos> entityOrPos) {
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

        public static MovingSoundFocus fromNetwork(FriendlyByteBuf buf) {
            SourceType type = buf.readEnum(SourceType.class);
            return type.getSource(buf);
        }

        void toNetwork(FriendlyByteBuf buf) {
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

//        public Either<Entity,BlockPos> asEntityOrPos() {
//            return entityOrPos;
//        }
    }
}
