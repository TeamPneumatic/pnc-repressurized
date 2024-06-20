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
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent from client when trying to rotate a block with a wrench other than PneumaticCraft's own wrench
 */
public record PacketModWrenchBlock(BlockPos pos, InteractionHand hand, Either<Direction,Integer> context) implements CustomPacketPayload {
    public static final Type<PacketModWrenchBlock> TYPE = new Type<>(RL("mod_wrench_block"));

    public static final StreamCodec<FriendlyByteBuf, PacketModWrenchBlock> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketModWrenchBlock::pos,
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), PacketModWrenchBlock::hand,
            ByteBufCodecs.either(Direction.STREAM_CODEC, ByteBufCodecs.INT), PacketModWrenchBlock::context,
            PacketModWrenchBlock::new
    );

    public static PacketModWrenchBlock forSide(BlockPos pos, InteractionHand hand, Direction side) {
        return new PacketModWrenchBlock(pos, hand, Either.left(side));
    }

    public static PacketModWrenchBlock forEntity(BlockPos pos, InteractionHand hand, int entityID) {
        return new PacketModWrenchBlock(pos, hand, Either.right(entityID));
    }

    @Override
    public Type<PacketModWrenchBlock> type() {
        return TYPE;
    }

    public static void handle(PacketModWrenchBlock message, IPayloadContext ctx) {
        Player player = ctx.player();
        Level level = player.level();
        BlockPos pos = message.pos();

        if (level.isAreaLoaded(pos, 0) && PneumaticCraftUtils.canPlayerReach(player, pos)) {
            InteractionHand hand = message.hand();
            if (ModdedWrenchUtils.getInstance().isModdedWrench(player.getItemInHand(hand))) {
                message.context().ifLeft(side -> {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof IPneumaticWrenchable wrenchable) {
                        wrenchable.onWrenched(level, player, pos, side, hand);
                    }
                }).ifRight(entityId -> {
                    Entity e = level.getEntity(entityId);
                    if (e instanceof IPneumaticWrenchable wrenchable && e.isAlive()) {
                        wrenchable.onWrenched(level, player, pos, null, hand);
                    }
                });
            }
        }
    }
}
