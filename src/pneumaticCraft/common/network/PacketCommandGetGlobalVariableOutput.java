package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkPosition;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketCommandGetGlobalVariableOutput extends AbstractPacket<PacketCommandGetGlobalVariableOutput>{
    private String varName;
    private ChunkPosition pos;
    private ItemStack stack;

    public PacketCommandGetGlobalVariableOutput(){}

    public PacketCommandGetGlobalVariableOutput(String varName, ChunkPosition pos, ItemStack stack){
        this.varName = varName;
        this.pos = pos;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        varName = ByteBufUtils.readUTF8String(buf);
        pos = new ChunkPosition(buf.readInt(), buf.readInt(), buf.readInt());
        stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeUTF8String(buf, varName);
        buf.writeInt(pos.chunkPosX);
        buf.writeInt(pos.chunkPosY);
        buf.writeInt(pos.chunkPosZ);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public void handleClientSide(PacketCommandGetGlobalVariableOutput message, EntityPlayer player){
        player.addChatMessage(new ChatComponentText(I18n.format("command.getGlobalVariable.output", message.varName, message.pos.chunkPosX, message.pos.chunkPosY, message.pos.chunkPosZ, message.stack != null ? message.stack.getDisplayName() : "-")));
    }

    @Override
    public void handleServerSide(PacketCommandGetGlobalVariableOutput message, EntityPlayer player){}

}
