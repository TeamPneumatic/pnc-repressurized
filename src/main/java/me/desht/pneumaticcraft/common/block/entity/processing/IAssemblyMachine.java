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

package me.desht.pneumaticcraft.common.block.entity.processing;

import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public interface IAssemblyMachine {
    /**
     * Returns true when the machine is done with moving/drilling/... and has returned to its idle position
     *
     * @return true when the machine is idle
     */
    boolean isIdle();

    /**
     * Sets the speed of the machine, used when Speed Upgrades are inserted in the Assembly Controller
     *
     * @param speed the speed
     */
    void setSpeed(float speed);

    /**
     * Get this machine's type.
     *
     * @return the type of assembly machine.
     */
    @Nonnull AssemblyProgram.EnumMachine getAssemblyType();

    /**
     * Inform this machine of its controller's position.
     *
     * @param controllerPos the controller's blockpos
     */
    void setControllerPos(BlockPos controllerPos);
}
