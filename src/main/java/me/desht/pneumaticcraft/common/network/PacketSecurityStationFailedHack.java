package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when a hack attempt fails
 */
public class PacketSecurityStationFailedHack extends LocationIntPacket {

    public PacketSecurityStationFailedHack() {
    }

    public PacketSecurityStationFailedHack(BlockPos pos) {
        super(pos);
    }

    public PacketSecurityStationFailedHack(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ctx.get().getSender().world.getTileEntity(pos);
            if (te instanceof TileEntitySecurityStation) {
                TileEntitySecurityStation station = (TileEntitySecurityStation) te;
                if (!station.isPlayerOnWhiteList(ctx.get().getSender())) {
                    ctx.get().getSender().attackEntityFrom(DamageSourcePneumaticCraft.SECURITY_STATION, 19);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
