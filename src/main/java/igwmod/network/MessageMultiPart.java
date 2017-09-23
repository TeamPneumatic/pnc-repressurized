package igwmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class MessageMultiPart extends AbstractPacket<MessageMultiPart>{
    private byte[] payload;

    public MessageMultiPart(){

    }

    public MessageMultiPart(byte[] payload){
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        payload = new byte[buf.readInt()];
        buf.readBytes(payload);
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(payload.length);
        buf.writeBytes(payload);
    }

    @Override
    public void handleClientSide(MessageMultiPart message, EntityPlayer player){
        MessageMultiHeader.receivePayload(message.payload);
    }

    @Override
    public void handleServerSide(MessageMultiPart message, EntityPlayer player){

    }

}
