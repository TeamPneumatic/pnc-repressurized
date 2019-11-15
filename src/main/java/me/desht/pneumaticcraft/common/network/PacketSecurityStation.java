package me.desht.pneumaticcraft.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class PacketSecurityStation extends LocationIntPacket {

    protected String username;

    public PacketSecurityStation() {
    }

    public PacketSecurityStation(TileEntity te, String username) {
        super(te.getPos());
        this.username = username;
    }

    public PacketSecurityStation(PacketBuffer buffer) {
        super(buffer);
        username = buffer.readString();
    }

    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeString(username);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = getTileEntity(ctx);
            handle(te, username);
        });
        ctx.get().setPacketHandled(true);
    }

    protected abstract void handle(TileEntity te, String username);

}
