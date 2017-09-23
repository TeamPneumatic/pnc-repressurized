package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketCommandGetGlobalVariableOutput extends AbstractPacket<PacketCommandGetGlobalVariableOutput> {
    private String varName;
    private BlockPos pos;
    private ItemStack stack;

    public PacketCommandGetGlobalVariableOutput() {
    }

    public PacketCommandGetGlobalVariableOutput(String varName, BlockPos pos, ItemStack stack) {
        this.varName = varName;
        this.pos = pos;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        varName = ByteBufUtils.readUTF8String(buf);
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, varName);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public void handleClientSide(PacketCommandGetGlobalVariableOutput message, EntityPlayer player) {
        player.sendStatusMessage(new TextComponentTranslation("command.getGlobalVariable.output",
                        message.varName,
                        message.pos.getX(), message.pos.getY(), message.pos.getZ(),
                        message.stack.isEmpty() ? "-" : message.stack.getDisplayName()),
                false);
    }

    @Override
    public void handleServerSide(PacketCommandGetGlobalVariableOutput message, EntityPlayer player) {
    }

}
