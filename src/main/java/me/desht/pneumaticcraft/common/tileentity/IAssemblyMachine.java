package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import net.minecraft.util.math.BlockPos;

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
