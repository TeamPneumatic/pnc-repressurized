/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Should be implemented by any block or entity that can be clicked by a Pneumatic Wrench. It uses almost the same
 * rotate method as the Vanilla (Forge) method. However it uses energy to rotate (when rotateBlock() return true).
 * Also, this method gets passed the player who did the rotation.
 */
public interface IPneumaticWrenchable {
    boolean onWrenched(Level world, Player player, BlockPos pos, Direction side, InteractionHand hand);

    static IPneumaticWrenchable forBlock(Block b) {
        return b instanceof IPneumaticWrenchable ? (IPneumaticWrenchable) b : null;
    }
}
