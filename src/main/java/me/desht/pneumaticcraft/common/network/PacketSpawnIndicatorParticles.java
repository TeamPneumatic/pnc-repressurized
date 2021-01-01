package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.item.DyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to spawn coloured redstone particles in multiple positions around an initial position
 */
public class PacketSpawnIndicatorParticles {
    private final BlockPos pos0;
    private final int dyeColor;
    private final List<ByteOffset> offsets = new ArrayList<>();

    public PacketSpawnIndicatorParticles(List<BlockPos> posList, DyeColor dyeColor) {
        this.pos0 = posList.get(0);
        this.dyeColor = dyeColor.getColorValue();
        for (int i = 1; i < posList.size(); i++) {
            BlockPos off = posList.get(i).subtract(pos0);
            if (off.getX() >= -128 && off.getX() <= 127 && off.getY() >= -128 && off.getY() <= 127 && off.getZ() >= -128 && off.getZ() <= 127) {
                offsets.add(new ByteOffset(off.getX(), off.getY(), off.getZ()));
            }
        }
    }

    public PacketSpawnIndicatorParticles(PacketBuffer buffer) {
        pos0 = buffer.readBlockPos();
        int nOffsets = buffer.readVarInt();
        for (int i = 0; i < nOffsets; i++) {
            offsets.add(new ByteOffset(buffer.readByte(), buffer.readByte(), buffer.readByte()));
        }
        dyeColor = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos0);
        buffer.writeVarInt(offsets.size());
        for (ByteOffset offset : offsets) {
            buffer.writeByte(offset.x);
            buffer.writeByte(offset.y);
            buffer.writeByte(offset.z);
        }
        buffer.writeInt(dyeColor);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ClientUtils.getClientWorld();
            int r = (dyeColor >> 16) & 0xFF;
            int g = (dyeColor >> 8) & 0xFF;
            int b = dyeColor & 0xFF;
            IParticleData particle = new RedstoneParticleData(r / 255f, g / 255f, b / 255f, 1f);
            world.addParticle(particle, pos0.getX() + 0.5, pos0.getY() + 0.5, pos0.getZ() + 0.5, 0, 0, 0);
            for (ByteOffset offset : offsets) {
                world.addParticle(particle, pos0.getX() + offset.x + 0.5, pos0.getY() + offset.y + 0.5, pos0.getZ() + offset.z + 0.5, 0, 0, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ByteOffset {
        private final byte x, y, z;

        private ByteOffset(int x, int y, int z) {
            this.x = (byte) x;
            this.y = (byte) y;
            this.z = (byte) z;
        }
    }
}
