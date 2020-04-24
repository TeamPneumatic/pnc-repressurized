package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent periodically from server to sync pressure level:
 * - For pressure tubes with an attached pressure gauge
 * - For machine air handlers which are currently leaking
 */
public class PacketUpdatePressureBlock extends LocationIntPacket {
    private static final byte NO_DIRECTION = 127;

    private Direction leakDir;
    private Direction handlerDir;
    private int currentAir;

    public PacketUpdatePressureBlock() {
    }

//    public PacketUpdatePressureBlock(TileEntity te) {
//        this(te, null);
//    }

    public PacketUpdatePressureBlock(TileEntity te, Direction handlerDir, Direction leakDir, int currentAir) {
        super(te.getPos());

        this.handlerDir = handlerDir;
        this.leakDir = leakDir;
        this.currentAir = currentAir;
    }

    public PacketUpdatePressureBlock(PacketBuffer buffer) {
        super(buffer);
        this.currentAir = buffer.readVarInt();
        byte idx = buffer.readByte();
        this.handlerDir = idx >= 0 && idx < 6 ? Direction.byIndex(idx) : null;
        idx = buffer.readByte();
        this.leakDir = idx >= 0 && idx < 6 ? Direction.byIndex(idx) : null;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeVarInt(currentAir);
        buf.writeVarInt(handlerDir == null ? NO_DIRECTION : handlerDir.getIndex());
        buf.writeVarInt(leakDir == null ? NO_DIRECTION : leakDir.getIndex());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ClientUtils.getClientTE(pos);
            if (te != null) {
                te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, handlerDir).ifPresent(handler -> {
                    handler.setSideLeaking(leakDir);
                    handler.addAir(currentAir - handler.getAir());
                    if (handlerDir != null && te instanceof TileEntityPneumaticBase) {
                        ((TileEntityPneumaticBase) te).initializeHullAirHandler(handlerDir, handler);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
