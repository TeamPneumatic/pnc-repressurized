package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
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
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                PacketUtil.getTE(player, pos, TileEntitySecurityStation.class).ifPresent(te -> {
                    if (!te.isPlayerOnWhiteList(ctx.get().getSender())) {
                        player.attackEntityFrom(DamageSourcePneumaticCraft.SECURITY_STATION, player.getMaxHealth() - 0.5f);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
