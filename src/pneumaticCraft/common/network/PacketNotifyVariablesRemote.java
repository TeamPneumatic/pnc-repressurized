package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.inventory.ContainerRemote;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketNotifyVariablesRemote extends AbstractPacket<PacketNotifyVariablesRemote>{
    private String[] variables;

    public PacketNotifyVariablesRemote(){

    }

    public PacketNotifyVariablesRemote(String[] variables){
        this.variables = variables;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        variables = new String[buf.readInt()];
        for(int i = 0; i < variables.length; i++) {
            variables[i] = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(variables.length);
        for(String s : variables)
            ByteBufUtils.writeUTF8String(buf, s);
    }

    @Override
    public void handleClientSide(PacketNotifyVariablesRemote message, EntityPlayer player){
        if(player.openContainer instanceof ContainerRemote) {
            ((ContainerRemote)player.openContainer).variables = message.variables;
        }
    }

    @Override
    public void handleServerSide(PacketNotifyVariablesRemote message, EntityPlayer player){}

}
