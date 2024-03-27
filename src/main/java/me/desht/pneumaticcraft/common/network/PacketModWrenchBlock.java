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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent from client when trying to rotate a block with a wrench other than PneumaticCraft's own wrench
 */
public record PacketModWrenchBlock(BlockPos pos, InteractionHand hand, Either<Direction,Integer> context) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("mod_wrench_block");

    public static PacketModWrenchBlock forSide(BlockPos pos, InteractionHand hand, Direction side) {
        return new PacketModWrenchBlock(pos, hand, Either.left(side));
    }

    public static PacketModWrenchBlock forEntity(BlockPos pos, InteractionHand hand, int entityID) {
        return new PacketModWrenchBlock(pos, hand, Either.right(entityID));
    }

    public static PacketModWrenchBlock fromNetwork(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        var hand = buffer.readEnum(InteractionHand.class);
        return buffer.readBoolean() ?
                new PacketModWrenchBlock(pos, hand, Either.left(buffer.readEnum(Direction.class))) :
                new PacketModWrenchBlock(pos, hand, Either.right(buffer.readInt()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(hand);
        context.ifLeft(side -> {
            buf.writeBoolean(true);
            buf.writeEnum(side);
        }).ifRight(id -> {
            buf.writeBoolean(false);
            buf.writeInt(id);
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketModWrenchBlock message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
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
        }));
    }
}
