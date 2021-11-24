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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent from client when trying to rotate a block with a wrench other than PneumaticCraft's own wrench
 */
public class PacketModWrenchBlock extends LocationIntPacket {
    private final Direction side;
    private final Hand hand;
    private final int entityID;

    public PacketModWrenchBlock(BlockPos pos, Direction side, Hand hand) {
        super(pos);
        this.side = side;
        this.hand = hand;
        this.entityID = -1;
    }

    public PacketModWrenchBlock(BlockPos pos, Hand hand, int entityID) {
        super(pos);
        this.side = null;
        this.hand = hand;
        this.entityID = entityID;
    }

    public PacketModWrenchBlock(PacketBuffer buffer) {
        super(buffer);
        hand = Hand.values()[buffer.readByte()];
        if (buffer.readBoolean()) {
            entityID = buffer.readInt();
            side = null;
        } else {
            side = Direction.values()[buffer.readByte()];
            entityID = -1;
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
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
            ServerPlayerEntity player = ctx.get().getSender();
            if (player.level.isAreaLoaded(pos, 0) && PneumaticCraftUtils.canPlayerReach(player, pos)) {
                if (ModdedWrenchUtils.getInstance().isModdedWrench(player.getItemInHand(hand))) {
                    if (entityID >= 0) {
                        Entity e = player.level.getEntity(entityID);
                        if (e instanceof IPneumaticWrenchable && e.isAlive()) {
                            ((IPneumaticWrenchable) e).onWrenched(player.level, player, pos, side, hand);
                        }
                    } else if (side != null) {
                        BlockState state = player.level.getBlockState(pos);
                        if (state.getBlock() instanceof IPneumaticWrenchable) {
                            ((IPneumaticWrenchable) state.getBlock()).onWrenched(player.level, player, pos, side, hand);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
