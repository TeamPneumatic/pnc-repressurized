package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
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
    private Direction side;
    private Hand hand;
    private int entityID;

    @SuppressWarnings("unused")
    public PacketModWrenchBlock() {
    }

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
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(hand.ordinal());
        if (entityID >= 0) {
            buf.writeBoolean(true);
            buf.writeInt(entityID);
        } else {
            buf.writeBoolean(false);
            buf.writeByte(side.ordinal());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player.world.isAreaLoaded(pos, 0)
                    && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64) {
                if (ModdedWrenchUtils.getInstance().isModdedWrench(player.getHeldItem(hand))) {
                    if (entityID >= 0) {
                        Entity e = player.world.getEntityByID(entityID);
                        if (e instanceof IPneumaticWrenchable && e.isAlive()) {
                            ((IPneumaticWrenchable) e).onWrenched(player.world, player, pos, side, hand);
                        }
                    } else if (side != null) {
                        BlockState state = player.world.getBlockState(pos);
                        if (state.getBlock() instanceof IPneumaticWrenchable) {
                            ((IPneumaticWrenchable) state.getBlock()).onWrenched(player.world, player, pos, side, hand);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
