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

    public PacketRotateBlock() {
    }

    public PacketRotateBlock(BlockPos pos, EnumFacing side) {
        super(pos);
        this.side = side;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(side.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        side = EnumFacing.values()[buf.readByte()];
    }

    @Override
    public void handleClientSide(PacketRotateBlock message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketRotateBlock message, EntityPlayer player) {
        if (player.world.isBlockLoaded(message.pos)
                && player.getDistanceSq(message.pos) < 64) {
            for (EnumHand hand : EnumHand.values()) {
                if (ModInteractionUtils.getInstance().isModdedWrench(player.getHeldItem(hand))) {
                    IBlockState state = player.world.getBlockState(message.pos);
                    if (state.getBlock() instanceof BlockPneumaticCraft) {
                        if (((BlockPneumaticCraft) state.getBlock()).rotateBlock(player.world, player, message.pos, message.side)) {
//                            player.world.notifyBlockUpdate(pos, state, state, 3);
                        }
                    }
                    break;
                }
            }
        }
    }

}
