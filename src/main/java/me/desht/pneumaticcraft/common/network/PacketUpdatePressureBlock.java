package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
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
        currentAir = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                .map(IAirHandlerMachine::getAir)
                .orElseThrow(RuntimeException::new);
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
            TileEntity te = ClientUtils.getClientTE(pos);
            te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                    .ifPresent(h -> h.addAir(currentAir - h.getAir()));
        });
        ctx.get().setPacketHandled(true);
    }
}
