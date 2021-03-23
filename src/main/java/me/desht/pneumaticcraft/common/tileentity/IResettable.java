package me.desht.pneumaticcraft.common.tileentity;

public interface IResettable {
    /**
     * Reset the machine's state
     *
     * @return true when the machine is done resetting
     */
    boolean reset();
}
