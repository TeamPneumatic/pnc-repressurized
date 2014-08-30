package pneumaticCraft.common.tileentity;

public interface IAssemblyMachine{
    /**
     * Returns true when the machine is done with moving/drilling/...
     * @return
     */
    public boolean isDone();

    /**
     * Sets the speed of the machine, used when Speed Upgrades are inserted in the Assembly Controller
     * @param speed
     */
    public void setSpeed(float speed);
}
