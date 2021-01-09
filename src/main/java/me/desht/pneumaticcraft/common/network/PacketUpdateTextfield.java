package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.tileentity.IGUITextFieldSensitive;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client GUI's to update a IGUITextFieldSensitive tile entity server-side
 */
public class PacketUpdateTextfield {
    private final int textFieldID;
    private final String text;

    public PacketUpdateTextfield(TileEntity te, int textfieldID) {
        textFieldID = textfieldID;
        text = ((IGUITextFieldSensitive) te).getText(textfieldID);
    }

    public PacketUpdateTextfield(PacketBuffer buffer) {
        textFieldID = buffer.readInt();
        text = buffer.readString(32767);
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(textFieldID);
        buffer.writeString(text);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PacketUtil.getTE(ctx.get().getSender(), TileEntity.class).ifPresent(te -> {
                if (te instanceof IGUITextFieldSensitive) {
                    ((IGUITextFieldSensitive) te).setText(textFieldID, text);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
