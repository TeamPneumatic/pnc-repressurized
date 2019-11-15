package me.desht.pneumaticcraft.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.dimension.DimensionType;

public class PacketUtil {
    public static void writeGlobalPos(PacketBuffer buf, GlobalPos gPos) {
        buf.writeResourceLocation(gPos.getDimension().getRegistryName());
        new PacketBuffer(buf).writeBlockPos(gPos.getPos());
    }

    public static GlobalPos readGlobalPos(PacketBuffer buf) {
        return GlobalPos.of(DimensionType.byName(buf.readResourceLocation()), buf.readBlockPos());
    }
}
