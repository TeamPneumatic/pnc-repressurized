package igwmod.network;

import igwmod.IGWMod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

public class MessageMultiHeader extends AbstractPacket<MessageMultiHeader>{
    private int size;
    public static byte[] totalMessage;
    public static int offset = 0;

    public MessageMultiHeader(){

    }

    public MessageMultiHeader(int size){
        this.size = size;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        size = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(size);
    }

    @Override
    public void handleClientSide(MessageMultiHeader message, EntityPlayer player){
        totalMessage = new byte[message.size];
        offset = 0;
    }

    @Override
    public void handleServerSide(MessageMultiHeader message, EntityPlayer player){}

    public static void receivePayload(byte[] payload){
        System.arraycopy(payload, 0, totalMessage, offset, payload.length);
        offset += NetworkHandler.MAX_SIZE;
        if(offset >= totalMessage.length) {
            MessageSendServerTab m = new MessageSendServerTab();
            ByteBuf buf = Unpooled.wrappedBuffer(totalMessage);
            m.fromBytes(buf);
            m.handleClientSide(m, IGWMod.proxy.getPlayer());
        }
    }

}
