package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class PacketSecurityStation extends LocationIntPacket {

    protected UUID username;

    public PacketSecurityStation() {
    }

    public PacketSecurityStation(TileEntity te, UUID player) {
        super(te.getPos());
        this.username = player;
    }

    public PacketSecurityStation(PacketBuffer buffer) {
        super(buffer);
        username = UUID.fromString(PacketUtil.readUTF8String(buffer));
    }

    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        PacketUtil.writeUTF8String(buffer, username.toString());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = getTileEntity(ctx);
            handle(te, username);
        });
        ctx.get().setPacketHandled(true);
    }

    protected abstract void handle(TileEntity te, UUID username);

}
