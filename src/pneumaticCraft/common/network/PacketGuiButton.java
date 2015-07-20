package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.tileentity.IGUIButtonSensitive;

public class PacketGuiButton extends LocationIntPacket<PacketGuiButton>{
    private int buttonID;
    private IDescSynced.Type type;

    public PacketGuiButton(){}

    public PacketGuiButton(IGUIButtonSensitive te, int buttonID){
        super(te.getX(), te.getY(), te.getZ());
        this.buttonID = buttonID;
        type = te.getSyncType();
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeByte(type.ordinal());
        buffer.writeInt(buttonID);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        type = IDescSynced.Type.values()[buffer.readByte()];
        buttonID = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketGuiButton message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketGuiButton message, EntityPlayer player){
        Object syncable = PacketDescription.getSyncableForType(message, player, message.type);
        if(syncable instanceof IGUIButtonSensitive) {
            ((IGUIButtonSensitive)syncable).handleGUIButtonPress(message.buttonID, player);
        }
    }

}
