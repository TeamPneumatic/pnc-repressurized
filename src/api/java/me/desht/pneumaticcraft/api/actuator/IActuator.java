package me.desht.pneumaticcraft.api.actuator;

import net.minecraft.tileentity.TileEntity;

import java.util.List;

/**
 * What was this interface ever for? Some planned functionality lost to the mists of time?
 */
@Deprecated
public interface IActuator {
    /**
     * Same as {@link me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting#getSensorPath()}
     *
     * @return a path
     */
    String getSensorPath();

    /**
     * When returned true, the GUI will enable the textbox writing, otherwise not.
     *
     * @return true if a text box is needed
     */
    boolean needsTextBox();

    /**
     * Should return the description of this sensor displayed in the GUI stat. Information should at least include
     * when this sensor emits redstone and how (analog (1 through 15), or digital).
     *
     * @return a sensor description
     */
    List<String> getDescription();

    /**
     * @param universalActuator an actuator tile entity
     */
    void actuate(TileEntity universalActuator);
}
