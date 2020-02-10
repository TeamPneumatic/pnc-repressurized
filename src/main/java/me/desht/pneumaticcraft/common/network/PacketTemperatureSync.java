package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync the client temperature for in-world display purposes.
 */
public class PacketTemperatureSync {
    private final BlockPos pos;
    private final int entityID;
    private final Direction dir;
    private final int temp;

    public PacketTemperatureSync(BlockPos pos, Direction dir, int temp) {
        this.pos = pos;
        this.entityID = -1;
        this.dir = dir;
        this.temp = temp;
    }

    public PacketTemperatureSync(int entityID, int temp) {
        this.pos = null;
        this.entityID = entityID;
        this.dir = null;
        this.temp = temp;
    }

    PacketTemperatureSync(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            pos = buffer.readBlockPos();
            byte d = buffer.readByte();
            dir = d == 6 ? null : Direction.byIndex(d);
            entityID = -1;
        } else {
            pos = null;
            dir = null;
            entityID = buffer.readInt();
        }
        temp = buffer.readVarInt();
    }

    public void toBytes(PacketBuffer buffer) {
        if (entityID < 0) {
            buffer.writeBoolean(true);
            buffer.writeBlockPos(pos);
            buffer.writeByte(dir == null ? 6 : dir.getIndex());
        } else {
            buffer.writeBoolean(false);
            buffer.writeInt(entityID);
        }
        buffer.writeVarInt(temp);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ClientUtils.getClientWorld();
            if (entityID < 0) {
                TileEntity te = world.getTileEntity(pos);
                te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, dir).ifPresent(handler -> handler.setTemperature(temp));
                world.notifyBlockUpdate(pos, te.getBlockState(), te.getBlockState(), 0);
            } else {
                ISemiBlock semiBlock = ISemiBlock.byTrackingId(world, entityID);
                if (semiBlock instanceof ICapabilityProvider) {
                    ((ICapabilityProvider) semiBlock).getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, null).ifPresent(handler -> handler.setTemperature(temp));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
