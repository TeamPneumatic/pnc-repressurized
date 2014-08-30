package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.block.BlockPressureTube;
import cpw.mods.fml.common.FMLCommonHandler;

public class PacketOpenTubeModuleGui extends LocationIntPacket<PacketOpenTubeModuleGui>{
    private int guiID;

    public PacketOpenTubeModuleGui(){}

    public PacketOpenTubeModuleGui(int guiID, int x, int y, int z){
        super(x, y, z);
        this.guiID = guiID;

    }

    @Override
    public void fromBytes(ByteBuf buf){
        super.fromBytes(buf);
        guiID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf){
        super.toBytes(buf);
        buf.writeInt(guiID);
    }

    @Override
    public void handleClientSide(PacketOpenTubeModuleGui message, EntityPlayer player){
        if(BlockPressureTube.getLookedModule(player.worldObj, message.x, message.y, message.z, player) != null) {
            Object o = PneumaticCraft.proxy.getClientGuiElement(message.guiID, player, player.worldObj, message.x, message.y, message.z);
            FMLCommonHandler.instance().showGuiScreen(o);
        }
    }

    @Override
    public void handleServerSide(PacketOpenTubeModuleGui message, EntityPlayer player){}

}
