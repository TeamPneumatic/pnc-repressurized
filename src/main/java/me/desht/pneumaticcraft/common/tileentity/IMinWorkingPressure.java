package me.desht.pneumaticcraft.common.tileentity;

public interface IMinWorkingPressure {
    /**
     * Get the minimum pressure at which this machine can operate.  Used for GUI display purposes (drawing the
     * yellow region of the pressure gauge, but can also be checked elsewhere.
     *
     * @return the minimum pressure at which this machine can operate
     */
    float getMinWorkingPressure();
}
