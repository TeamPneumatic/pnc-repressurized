package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.CommonHUDHandler;

public class PacketToggleHelmetFeature extends AbstractPacket<PacketToggleHelmetFeature>{
    private byte featureIndex;
    private boolean state;

    public PacketToggleHelmetFeature(){}

    public PacketToggleHelmetFeature(byte featureIndex, boolean state){
        this.featureIndex = featureIndex;
        this.state = state;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        featureIndex = buf.readByte();
        state = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeByte(featureIndex);
        buf.writeBoolean(state);
    }

    @Override
    public void handleClientSide(PacketToggleHelmetFeature message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketToggleHelmetFeature message, EntityPlayer player){
        boolean[] enabledHandlers = CommonHUDHandler.getHandlerForPlayer(player).upgradeRenderersEnabled;
        if(enabledHandlers.length > message.featureIndex) enabledHandlers[message.featureIndex] = message.state;
    }

}
