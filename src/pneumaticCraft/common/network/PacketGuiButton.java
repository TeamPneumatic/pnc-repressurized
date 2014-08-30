package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.IGUIButtonSensitive;

public class PacketGuiButton extends LocationIntPacket<PacketGuiButton>{
    private int buttonID;

    public PacketGuiButton(){}

    public PacketGuiButton(TileEntity te, int buttonID){
        super(te.xCoord, te.yCoord, te.zCoord);
        this.buttonID = buttonID;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(buttonID);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        buttonID = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketGuiButton message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketGuiButton message, EntityPlayer player){
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(te instanceof IGUIButtonSensitive) {
            ((IGUIButtonSensitive)te).handleGUIButtonPress(message.buttonID, player);
        }
    }

}
