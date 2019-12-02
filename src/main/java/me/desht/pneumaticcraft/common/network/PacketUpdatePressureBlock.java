package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.pressure.AirHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent periodically from server for tubes with a pressure gauge on them so the TESR can draw the needle correctly.
 */
public class PacketUpdatePressureBlock extends LocationIntPacket {
    private int currentAir;

    public PacketUpdatePressureBlock() {
    }

    public PacketUpdatePressureBlock(TileEntityPneumaticBase te) {
        super(te.getPos());
        currentAir = te.getAirHandler(null).getAir();
    }

    public PacketUpdatePressureBlock(PacketBuffer buffer) {
        super(buffer);
        this.currentAir = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeInt(currentAir);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = getTileEntity(ctx);
            if (te instanceof TileEntityPneumaticBase) {
                ((AirHandler) ((TileEntityPneumaticBase) te).getAirHandler(null)).setAir(currentAir);
            } else {
                TileEntityPressureTube tube = TileEntityPressureTube.getTube(te);
                if (tube != null) ((AirHandler) tube.getAirHandler(null)).setAir(currentAir);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
