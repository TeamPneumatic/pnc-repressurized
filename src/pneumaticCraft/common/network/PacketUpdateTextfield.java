package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.IGUITextFieldSensitive;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketUpdateTextfield extends LocationIntPacket<PacketUpdateTextfield>{

    private int textFieldID;
    private String text;

    public PacketUpdateTextfield(){}

    public PacketUpdateTextfield(TileEntity te, int textfieldID){
        super(te.xCoord, te.yCoord, te.zCoord);
        textFieldID = textfieldID;
        text = ((IGUITextFieldSensitive)te).getText(textfieldID);
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(textFieldID);
        ByteBufUtils.writeUTF8String(buffer, text);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        textFieldID = buffer.readInt();
        text = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handleClientSide(PacketUpdateTextfield message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketUpdateTextfield message, EntityPlayer player){
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(te instanceof IGUITextFieldSensitive) {
            ((IGUITextFieldSensitive)te).setText(message.textFieldID, message.text);
        }
    }

}
