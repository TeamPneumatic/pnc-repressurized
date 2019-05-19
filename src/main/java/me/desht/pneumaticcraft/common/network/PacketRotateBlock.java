package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class PacketRotateBlock extends LocationIntPacket<PacketRotateBlock> {
    private EnumFacing side;
    private EnumHand hand;

    @SuppressWarnings("unused")
    public PacketRotateBlock() {
    }

    public PacketRotateBlock(BlockPos pos, EnumFacing side, EnumHand hand) {
        super(pos);
        this.side = side;
        this.hand = hand;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(side.ordinal());
        buf.writeByte(hand.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        side = EnumFacing.values()[buf.readByte()];
        hand = EnumHand.values()[buf.readByte()];
    }

    @Override
    public void handleClientSide(PacketRotateBlock message, EntityPlayer player) {
        // empty
    }

    @Override
    public void handleServerSide(PacketRotateBlock message, EntityPlayer player) {
        if (player.world.isBlockLoaded(message.pos) && player.getDistanceSq(message.pos) < 64) {
            if (ModInteractionUtils.getInstance().isModdedWrench(player.getHeldItem(message.hand))) {
                IBlockState state = player.world.getBlockState(message.pos);
                if (state.getBlock() instanceof BlockPneumaticCraft) {
                    ((BlockPneumaticCraft) state.getBlock()).rotateBlock(player.world, player, message.pos, message.side, message.hand);
                }
            }
        }
    }
}
