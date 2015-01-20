package pneumaticCraft.api.drone;

public interface IDrone{
    /**
     * 
     * @param upgradeIndex metadata value of the upgrade item
     * @return amount of inserted upgrades in the drone
     */
    public int getUpgrades(int upgradeIndex);
}
