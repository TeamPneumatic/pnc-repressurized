package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by clientside programmer GUI to push the current program to the server-side TE
 */
public class PacketProgrammerUpdate extends LocationIntPacket implements ILargePayload {
    private CompoundNBT progWidgets;

    public PacketProgrammerUpdate() {
    }

    public PacketProgrammerUpdate(TileEntityProgrammer te) {
        super(te.getPos());
        progWidgets = new CompoundNBT();
        te.writeProgWidgetsToNBT(progWidgets);
    }

    public PacketProgrammerUpdate(PacketBuffer buffer) {
        super(buffer);
        try {
            progWidgets = buffer.readCompoundTag();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        try {
            buffer.writeCompoundTag(progWidgets);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> updateTE(getTileEntity(ctx), ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }

    private void updateTE(TileEntity te, PlayerEntity player) {
        if (te instanceof TileEntityProgrammer) {
            ((TileEntityProgrammer) te).readProgWidgetsFromNBT(progWidgets);
            ((TileEntityProgrammer) te).saveToHistory();
            if (!te.getWorld().isRemote) {
                updateOtherWatchingPlayers((TileEntityProgrammer) te, player);
            }
        }
    }

    private void updateOtherWatchingPlayers(TileEntityProgrammer te, PlayerEntity changingPlayer) {
        List<ServerPlayerEntity> players = changingPlayer.world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos.add(-5, -5, -5), pos.add(6, 6, 6)));
        for (ServerPlayerEntity player : players) {
            if (player != changingPlayer) {
                NetworkHandler.sendToPlayer(new PacketProgrammerUpdate(te), player);
            }
        }
    }

    @Override
    public PacketBuffer dumpToBuffer() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        toBytes(buf);
        return buf;
    }

    @Override
    public void handleLargePayload(PlayerEntity player) {
        updateTE(player.world.getTileEntity(pos), player);
    }
}
