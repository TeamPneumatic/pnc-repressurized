package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public abstract class PacketSecurityStation<REQ extends PacketSecurityStation> extends LocationIntPacket<REQ> {

    protected String username;

    public PacketSecurityStation() {
    }

    public PacketSecurityStation(TileEntity te, String player) {
        super(te.getPos());
        this.username = player;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        ByteBufUtils.writeUTF8String(buffer, username);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        username = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handleClientSide(REQ message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(REQ message, EntityPlayer player) {
        TileEntity te = message.getTileEntity(player.getEntityWorld());
        handleServerSide(te, message.username);
    }

    protected abstract void handleServerSide(TileEntity te, String username);

}
