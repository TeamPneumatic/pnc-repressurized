package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDebugBlock;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockIndicator {
    public static void indicateBlock(World world, BlockPos pos) {
        if (world != null) {
            if (world.isClientSide) {
                for (int i = 0; i < 5; i++) {
                    double dx = pos.getX() + 0.5;
                    double dy = pos.getY() + 0.5;
                    double dz = pos.getZ() + 0.5;
                    world.addParticle(new RedstoneParticleData(1f, 0.2f, 0f, 1.0F), dx, dy, dz, 0, 0, 0);
                }
            } else {
                NetworkHandler.sendToAllTracking(new PacketDebugBlock(pos), world, pos);
            }
        }
    }
}
