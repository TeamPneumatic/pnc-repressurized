package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.tileentity.IGUITextFieldSensitive;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client GUI's to update a IGUITextFieldSensitive object server-side
 */
public class PacketUpdateTextfield extends LocationIntPacket {

    private int textFieldID;
    private String text;

    public PacketUpdateTextfield() {
    }

    public PacketUpdateTextfield(TileEntity te, int textfieldID) {
        super(te.getPos());
        textFieldID = textfieldID;
        text = ((IGUITextFieldSensitive) te).getText(textfieldID);
    }

    public PacketUpdateTextfield(PacketBuffer buffer) {
        super(buffer);
        textFieldID = buffer.readInt();
        text = PacketUtil.readUTF8String(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(textFieldID);
        PacketUtil.writeUTF8String(buffer, text);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = getTileEntity(ctx);
            if (te instanceof IGUITextFieldSensitive) {
                ((IGUITextFieldSensitive) te).setText(textFieldID, text);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
