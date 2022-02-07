/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import com.mojang.math.Vector3f;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDebugBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;

public class BlockIndicator {
    public static void indicateBlock(Level world, BlockPos pos) {
        if (world != null) {
            if (world.isClientSide) {
                for (int i = 0; i < 5; i++) {
                    double dx = pos.getX() + 0.5;
                    double dy = pos.getY() + 0.5;
                    double dz = pos.getZ() + 0.5;
                    world.addParticle(new DustParticleOptions(new Vector3f(1f, 0.2f, 0f), 1.0F), dx, dy, dz, 0, 0, 0);
                }
            } else {
                NetworkHandler.sendToAllTracking(new PacketDebugBlock(pos), world, pos);
            }
        }
    }
}
