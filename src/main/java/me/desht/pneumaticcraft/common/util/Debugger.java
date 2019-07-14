/*
 * This file is part of Blue Power. Blue Power is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Blue Power is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along
 * with Blue Power. If not, see <http://www.gnu.org/licenses/>
 */
package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDebugBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Class aimed for debugging purposes only
 *
 * @author MineMaarten
 */
public class Debugger {

    public static void indicateBlock(TileEntity te) {
        indicateBlock(te.getWorld(), te.getPos());
    }

    public static void indicateBlock(World world, BlockPos pos) {
        if (world != null) {
            if (world.isRemote) {
                for (int i = 0; i < 5; i++) {
                    double dx = pos.getX() + 0.5;
                    double dy = pos.getY() + 0.5;
                    double dz = pos.getZ() + 0.5;
                    world.addParticle(new RedstoneParticleData(1f, 0.2f, 0f, 1.0F), dx, dy, dz, 0, 0, 0);
                }
            } else {
                NetworkHandler.sendToAllAround(new PacketDebugBlock(pos), world);
            }
        }
    }

    /**
     * This can be used as an IntelliJ break condition to ensure mouse pointer is
     * released when client breakpoints are hit (for Linux in particular).
     */
    @SuppressWarnings("unused")
    @OnlyIn(Dist.CLIENT)
    public static boolean ungrabMouse() {
        Minecraft.getInstance().mouseHelper.ungrabMouse();
        return true;
    }
}
