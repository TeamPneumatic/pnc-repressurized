package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A way for you to access about everything you need from a pneumatic machine.
 * DO NOT IMPLEMENT THIS YOURSELF! Use {@link IAirHandlerSupplier} to get an instance for your TileEntity,
 * and implement {@link IPneumaticMachine} instead.
 */
public interface IAirHandler extends IManoMeasurable, INBTSerializable<CompoundNBT> {

    /**
     * Must be forwarded by the implementing TileEntity's tick() method.  Updates the pneumatic machine's logic: air
     * dispersion to adjacent air handlers, and checking for potential explosions.
     */
    void tick();

    @Override
    CompoundNBT serializeNBT();

    @Override
    void deserializeNBT(CompoundNBT nbt);

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
    void airLeak(Direction side);

    /**
     * Returns a list of all the connecting pneumatics. It takes sides in account.
     */
    List<Pair<Direction, IAirHandler>> getConnectedPneumatics();

    /**
     * Adds air to the tank of the given side of this TE.
     * @param amount amount of air to add in mL, may be negative
     */
    void addAir(int amount);

    /**
     * Sets the volume of this TE's air tank. When the volume decreases the pressure will remain the same, meaning
     * air will be lost. When the volume increases, the air remains the same, meaning the pressure will drop.
     * Used in the Volume Upgrade calculations.
     * <p>
     * By default volume we mean the base volume. Volume added due to Volume Upgrades are added to this.
     *
     * @param defaultVolume the base volume of this air handler, without upgrades
     */
    void setDefaultVolume(int defaultVolume);

    /**
     * Get the effective volume of this air handler. This may have been increased by Volume Upgrades.
     * @return the effective volume, in mL
     */
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
     * Get this Air Handler's world
     * @return the world
     */
    World getWorld();

    /**
     * Get this Air Handler's blockpos
     * @return the blockpos
     */
    BlockPos getPos();

    /**
     * Sets the Air Handler's world. Not necessary if you use validate().
     *
     * @param world the world
     */
    void setWorld(World world);

    /**
     * Sets the Air Handler's blockpos. Not necessary if you use validate().
     *
     * @param pos the blockpos
     */
    void setPos(BlockPos pos);

    /**
     * Not necessary if you use validate()
     *
     * @param machine the pneumatic machine
     */
    void setPneumaticMachine(IPneumaticMachine machine);

    /**
     * Not necessary if you use validate(), or when the parent doesn't implement IAirListener.
     *
     * @param airListener the air listener
     */
    void setAirListener(IAirListener airListener);

    /**
     * Creates an air connection with another handler. Can be used to connect up pneumatic machines that aren't
     * directly adjacent. An example is the auxiliary valves in a pressure chamber multiblock.
     *
     * @param otherHandler the other air handler object
     */
    void createConnection(@Nonnull IAirHandler otherHandler);

    /**
     * Remove a connection created with {@link #createConnection(IAirHandler)}. You must call this when one of the
     * hosts of the two {@link IAirHandler air handlers} is invalidated.
     *
     * @param otherHandler the other air handler object
     */
    void removeConnection(@Nonnull IAirHandler otherHandler);

}
