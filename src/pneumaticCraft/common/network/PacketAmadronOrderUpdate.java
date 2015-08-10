package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.inventory.ContainerAmadron;

public class PacketAmadronOrderUpdate extends AbstractPacket<PacketAmadronOrderUpdate>{

    private int orderId, mouseButton;
    private boolean sneaking;

    public PacketAmadronOrderUpdate(int orderId, int mouseButton, boolean sneaking){
        this.orderId = orderId;
        this.mouseButton = mouseButton;
        this.sneaking = sneaking;
    }

    public PacketAmadronOrderUpdate(){}

    @Override
    public void fromBytes(ByteBuf buf){
        orderId = buf.readInt();
        mouseButton = buf.readByte();
        sneaking = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(orderId);
        buf.writeByte(mouseButton);
        buf.writeBoolean(sneaking);
    }

    @Override
    public void handleClientSide(PacketAmadronOrderUpdate message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketAmadronOrderUpdate message, EntityPlayer player){
        if(player.openContainer instanceof ContainerAmadron) {
            ((ContainerAmadron)player.openContainer).clickOffer(message.orderId, message.mouseButton, message.sneaking, player);
        }
    }

}
