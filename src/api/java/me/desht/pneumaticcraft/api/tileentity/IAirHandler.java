package me.desht.pneumaticcraft.api.tileentity;

import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A way for you to access about everything you need from a pneumatic machine.
 * DO NOT IMPLEMENT THIS YOURSELF! Use {@link IAirHandlerSupplier} to get an instance for your TileEntity,
 * and implement {@link IPneumaticMachine} instead.
 */

public interface IAirHandler extends IManoMeasurable, IUpgradeAcceptor {

    /**
     * Must be forwarded by the implementing TileEntity's update() method.
     * Updates the pneumatic machine's logic like air dispersion and checking if it needs to explode.
     */
    void update();

    /**
     * Must be forwarded by the implementing TileEntity.
     *
     * @param tag
     */
    void readFromNBT(NBTTagCompound tag);

    /**
     * Must be forwarded by the implementing TileEntity.
     *
     * @param tag
     */
    void writeToNBT(NBTTagCompound tag);

    /**
     * Must be forwarded by the implementing TileEntity with itself as parameter.
     *
     * @param parent TileEntity that is referencing this air handler.
     */
    void validate(TileEntity parent);

    /**
     * Must be forwarded from the implementing _Block_! Forward the Block's "onNeighborChange" method to this handler.
     */
    void onNeighborChange();

    /**
     * Method to release air into the atmosphere. It takes air from a specific side, plays a sound effect, and spawns
     * smoke particles.  It automatically detects if it needs to release air (when under pressure), suck air (when in
     * vacuum) or do nothing.
     *
     * @param side this only affects the direction the steam is pointing.
     */
    void airLeak(EnumFacing side);

    /**
     * Returns a list of all the connecting pneumatics. It takes sides in account.
     */
    List<Pair<EnumFacing, IAirHandler>> getConnectedPneumatics();

    /**
     * Adds air to the tank of the given side of this TE.
     */
    void addAir(int amount);

    /**
     * Sets the volume of this TE's air tank. When the volume decreases the pressure will remain the same, meaning air will
     * be lost. When the volume increases, the air remains the same, meaning the pressure will drop.
     * Used in the Volume Upgrade calculations.
     * By default volume we mean the base volume. Volume added due to Volume Upgrades are added to this.
     *
     * @param defaultVolume
     */
    void setDefaultVolume(int defaultVolume);

    int getVolume();

    /**
     * Returns the actual pressure at which this TE will explode. This is a random value between the danger and critical
     * pressure, and should generally not be reported to players!
     *
     * @return the explosion pressure for the TE
     */
    float getMaxPressure();
    
    /**
     * Returns the minimal pressure this machine could explode at.
     * @return the danger pressure
     */
    float getDangerPressure();
    
    /**
     * Returns the maximum pressure this machine could explode at.
     * @return the critical pressure
     */
    float getCriticalPressure();

    /**
     * Get the current pressure for the machine.
     *
     * @return the current pressure
     */
    float getPressure();

    /**
     * Returns the amount of air in the machine.  Note: amount of air = pressure * volume.
     *
     * @return the air in this air handler
     */
    int getAir();

    /**
     * Deprecated method.  This applied in earlier versions of PneumaticCraft but should not be used now,
     * and does nothing useful.  Will be removed in a later release.
     *
     * @param upgradeSlots all upgrade slots stored in an array.
     */
    @Deprecated
    void setUpgradeSlots(int... upgradeSlots);

    /**
     * Deprecated method.  This applied in earlier versions of PneumaticCraft but should not be used now,
     * and does nothing useful.  Will be removed in a later release.
     */
    @Deprecated
    int[] getUpgradeSlots();

    World getWorld();

    BlockPos getPos();

    /**
     * Not necessary if you use validate().
     *
     * @param world
     */
    void setWorld(World world);

    /**
     * Not necessary if you use validate().
     *
     * @param pos
     */
    void setPos(BlockPos pos);

    /**
     * Not necessary if you use validate()
     *
     * @param machine
     */
    void setPneumaticMachine(IPneumaticMachine machine);

    /**
     * Not necessary if you use validate(), or when the parent doesn't implement IAirListener.
     *
     * @param airListener
     */
    void setAirListener(IAirListener airListener);

    /**
     * Creates an air connection with another handler. Can be used to connect up pneumatic machines that aren't
     * neighboring, like AE2's P2P tunnels.  This is a custom method that isn't necessary 99% of the cases. Only when
     * you want to connect two IAirHandler's that aren't adjacent to each other in the world you should need this.  An
     * example is the auxiliary valves in a pressure chamber multiblock.
     *
     * @param otherHandler the other air handler object
     */
    void createConnection(@Nonnull IAirHandler otherHandler);

    /**
     * Remove a connection created with createConnection. You need to call this when one of the hosts of this
     * IAirHandlers is invalidated.
     *
     * @param otherHandler the other air handler object
     */
    void removeConnection(@Nonnull IAirHandler otherHandler);

}
