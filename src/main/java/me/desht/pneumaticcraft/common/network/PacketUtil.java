package me.desht.pneumaticcraft.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class PacketUtil {
    public static void writeGlobalPos(PacketBuffer buf, GlobalPos gPos) {
        buf.writeResourceLocation(gPos.getDimension().getLocation());
        buf.writeBlockPos(gPos.getPos());
    }

    public static GlobalPos readGlobalPos(PacketBuffer buf) {
        RegistryKey<World> worldKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, buf.readResourceLocation());
        BlockPos pos = buf.readBlockPos();
        return GlobalPos.getPosition(worldKey, pos);
    }
}
