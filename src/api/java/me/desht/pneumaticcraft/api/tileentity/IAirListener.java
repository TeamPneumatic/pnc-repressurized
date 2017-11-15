package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Optionally implement this by your TileEntity to gain more control about the pneumatics.
 * This will be called by any IAirHandler that is validated through 'validate' with the implementing object as parameter.
 */
public interface IAirListener extends IPneumaticMachine {

    /**
     * Called when air is added to, or removed from a handler, dispersed into/from a certain direction.
     *
     * @param handler       The handler it is about, could be used to distinguish when having multiple handlers.
     * @param dir
     * @param airTransfered The amount transfered to a connecting handler (so decreased from this block). Negative when adding air.
     */
    void onAirDispersion(IAirHandler handler, @Nullable EnumFacing dir, int airTransfered);

    /**
     * Method fired to get the maximum amount of air allowed to disperse to the given direction. Used in the regulator tube to prevent air from travelling.
     * return Integer.MAX_VALUE to have no limit.
     *
     * @param handler The handler it is about, could be used to distinguish when having multiple handlers.
     * @param dir
     * @return
     */
    int getMaxDispersion(IAirHandler handler, @Nullable EnumFacing dir);

    /**
     * In here you can add pneumatic machines that aren't directly connected, but should be considered connected (for the dispersion logic).
     * Used in Pressure Chamber Valves to make them connect when they are part of the same Pressure Chamber for example.
     *
     * @param pneumatics, EnumFacing has a direction for the default pneumatics, but often is null for custom ones.
     */
    void addConnectedPneumatics(List<Pair<EnumFacing, IAirHandler>> pneumatics);
}
