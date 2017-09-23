package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import net.minecraft.entity.player.EntityPlayer;

public class PacketGuiButton extends AbstractPacket<PacketGuiButton> {
    private int buttonID;

    public PacketGuiButton() {
    }

    public PacketGuiButton(int buttonID) {
        this.buttonID = buttonID;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(buttonID);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        buttonID = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketGuiButton message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketGuiButton message, EntityPlayer player) {
        if (player.openContainer instanceof IGUIButtonSensitive) {
            ((IGUIButtonSensitive) player.openContainer).handleGUIButtonPress(message.buttonID, player);
        }
    }

}
