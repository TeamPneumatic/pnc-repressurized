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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to start a new MovingSound playing
 */
public record PacketPlayMovingSound(MovingSounds.Sound sound, MovingSoundFocus source) implements CustomPacketPayload {
    public static final Type<PacketPlayMovingSound> TYPE = new Type<>(RL("play_moving_sound"));

    public static final StreamCodec<FriendlyByteBuf, PacketPlayMovingSound> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(MovingSounds.Sound.class), PacketPlayMovingSound::sound,
            MovingSoundFocus.STREAM_CODEC, PacketPlayMovingSound::source,
            PacketPlayMovingSound::new
    );

    @Override
    public Type<PacketPlayMovingSound> type() {
        return TYPE;
    }

    public static void handle(PacketPlayMovingSound message, IPayloadContext ctx) {
        if (message.source() != null) message.source().handle(message.sound());
    }

    public record MovingSoundFocus(Either<Integer,BlockPos> entityOrPos) {
        public static StreamCodec<FriendlyByteBuf, MovingSoundFocus> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.either(ByteBufCodecs.INT, BlockPos.STREAM_CODEC), MovingSoundFocus::entityOrPos,
                MovingSoundFocus::new
        );

        public static MovingSoundFocus of(Entity e) {
            return new MovingSoundFocus(Either.left(e.getId()));
        }

        public static MovingSoundFocus of(int id) {
            return new MovingSoundFocus(Either.left(id));
        }

        public static MovingSoundFocus of(BlockPos pos) {
            return new MovingSoundFocus(Either.right(pos));
        }

        public static MovingSoundFocus of(BlockEntity te) {
            return new MovingSoundFocus(Either.right(te.getBlockPos()));
        }

        public void handle(MovingSounds.Sound sound) {
            entityOrPos
                    .ifLeft(id -> {
                        Entity e = ClientUtils.getClientLevel().getEntity(id);
                        if (e != null) {
                            MovingSounds.playMovingSound(sound, e);
                        }
                    })
                    .ifRight(pos -> MovingSounds.playMovingSound(sound, pos));
        }
    }
}
