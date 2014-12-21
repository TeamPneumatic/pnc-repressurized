package pneumaticCraft.common.tileentity;

public interface IAssemblyMachine{
    /**
     * Returns true when the machine is done with moving/drilling/... and has returned to its idle position
     * @return
     */
    public boolean isIdle();

    /**
     * Sets the speed of the machine, used when Speed Upgrades are inserted in the Assembly Controller
     * @param speed
     */
    public void setSpeed(float speed);
}
