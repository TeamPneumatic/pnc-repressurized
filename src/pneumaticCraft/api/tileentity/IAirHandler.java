package pneumaticCraft.api.tileentity;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public interface IAirHandler extends IManoMeasurable{

    /**
     * -----------Needs to be forwarded by the implementing TileEntity's onUpdate() method.
     * Updates the pneumatic machine's logic like air dispersion and checking if it needs to explode.
     */
    public void update();

    /**
     * -----------Needs to be forwarded by the implementing TileEntity.
     * @param nbt
     */
    public void readFromNBT(NBTTagCompound nbt);

    /**
     * -----------Needs to be forwarded by the implementing TileEntity.
     * @param nbt
     */
    public void writeToNBT(NBTTagCompound nbt);

    /**
     * -----------Needs to be forwarded by the implementing TileEntity with itself as parameter.
     * @param parent TileEntity that is referencing this air handler.
     */
    public void validate(TileEntity parent);

    /**
     * Method to release air in the air. It takes air from a specific side, plays a sound effect, and spawns smoke particles.
     * It automatically detects if it needs to release air (when under pressure), suck air (when in vacuum) or do nothing.
     * @param side
     */
    public void airLeak(ForgeDirection side);

    /**
     * Returns a list of all the connecting pneumatics. It takes sides in account.
     * @return
     */
    public List<IPneumaticMachine> getSurroundingPneumatics();

    /**
     * Adds air to the tank of the given side of this TE. It also updates clients where needed (when they have a GUI opened).
     * @param amount
     * @param side
     */
    public void addAir(float amount, ForgeDirection side);

    /**
     * Sets the volume of this TE's air tank. When the volume decreases the pressure will remain the same, meaning air will
     * be lost. When the volume increases, the air remains the same, meaning the pressure will drop.
     * Used in the Volume Upgrade calculations.
     * @param newVolume
     */
    public void setVolume(float newVolume);

    public float getVolume();

    /**
     * Returns the pressure at which this TE will explode.
     * @return
     */
    public float getMaxPressure();

    public float getPressure(ForgeDirection sideRequested);

    /**
     * When you're TileEntity is implementing IInventory and has slots that accept PneumaticCraft upgrades, register these slots
     * to the air handler by calling this method once on initialization of the TileEntity.
     * @param upgradeSlots all upgrade slots stored in an array.
     */
    public void setUpgradeSlots(int[] upgradeSlots);

    public int[] getUpgradeSlots();

    public int getXCoord();

    public int getYCoord();

    public int getZCoord();

}
