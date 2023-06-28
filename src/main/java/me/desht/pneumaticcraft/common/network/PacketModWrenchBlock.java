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

import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent from client when trying to rotate a block with a wrench other than PneumaticCraft's own wrench
 */
public class PacketModWrenchBlock extends LocationIntPacket {
    private final Direction side;
    private final InteractionHand hand;
    private final int entityID;

    public PacketModWrenchBlock(BlockPos pos, Direction side, InteractionHand hand) {
        super(pos);
        this.side = side;
        this.hand = hand;
        this.entityID = -1;
    }

    public PacketModWrenchBlock(BlockPos pos, InteractionHand hand, int entityID) {
        super(pos);
        this.side = null;
        this.hand = hand;
        this.entityID = entityID;
    }

    public PacketModWrenchBlock(FriendlyByteBuf buffer) {
        super(buffer);
        hand = InteractionHand.values()[buffer.readByte()];
        if (buffer.readBoolean()) {
            entityID = buffer.readInt();
            side = null;
        } else {
            side = Direction.values()[buffer.readByte()];
            entityID = -1;
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(hand.ordinal());
        if (entityID >= 0) {
            buf.writeBoolean(true);
            buf.writeInt(entityID);
        } else {
            buf.writeBoolean(false);
            buf.writeByte(side.get3DDataValue());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Level level = player.level();
            if (level.isAreaLoaded(pos, 0) && PneumaticCraftUtils.canPlayerReach(player, pos)) {
                if (ModdedWrenchUtils.getInstance().isModdedWrench(player.getItemInHand(hand))) {
                    if (entityID >= 0) {
                        Entity e = level.getEntity(entityID);
                        if (e instanceof IPneumaticWrenchable && e.isAlive()) {
                            ((IPneumaticWrenchable) e).onWrenched(level, player, pos, side, hand);
                        }
                    } else if (side != null) {
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof IPneumaticWrenchable) {
                            ((IPneumaticWrenchable) state.getBlock()).onWrenched(level, player, pos, side, hand);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
