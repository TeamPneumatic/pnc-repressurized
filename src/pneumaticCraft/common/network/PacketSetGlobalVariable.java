package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiRemote;
import pneumaticCraft.common.remote.GlobalVariableManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketSetGlobalVariable extends AbstractPacket<PacketSetGlobalVariable>{
    private String varName;
    private ChunkPosition value;

    public PacketSetGlobalVariable(){}

    public PacketSetGlobalVariable(String varName, ChunkPosition value){
        this.varName = varName;
        this.value = value;
    }

    public PacketSetGlobalVariable(String varName, int value){
        this(varName, new ChunkPosition(value, 0, 0));
    }

    public PacketSetGlobalVariable(String varName, boolean value){
        this(varName, value ? 1 : 0);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        varName = ByteBufUtils.readUTF8String(buf);
        value = new ChunkPosition(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeUTF8String(buf, varName);
        buf.writeInt(value.chunkPosX);
        buf.writeInt(value.chunkPosY);
        buf.writeInt(value.chunkPosZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(PacketSetGlobalVariable message, EntityPlayer player){
        handleServerSide(message, player);
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if(screen instanceof GuiRemote) {
            ((GuiRemote)screen).onGlobalVariableChange(message.varName);
        }
    }

    @Override
    public void handleServerSide(PacketSetGlobalVariable message, EntityPlayer player){
        GlobalVariableManager.getInstance().set(message.varName, message.value);
    }

}
