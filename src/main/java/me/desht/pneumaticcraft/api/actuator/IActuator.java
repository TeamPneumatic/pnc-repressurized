package me.desht.pneumaticcraft.api.actuator;

import net.minecraft.tileentity.TileEntity;

import java.util.List;

public interface IActuator {
    /**
     * Same as {@link me.desht.pneumaticcraft.api.universalSensor.ISensorSetting#getSensorPath()}
     *
     * @return
     */
    String getSensorPath();

    /**
     * When returned true, the GUI will enable the textbox writing, otherwise not.
     *
     * @return
     */
    boolean needsTextBox();

    /**
     * Should return the description of this sensor displayed in the GUI stat. Information should at least include
     * when this sensor emits redstone and how (analog (1 through 15), or digital).
     *
     * @return
     */
    List<String> getDescription();

    /**
     * @param universalActuator
     */
    void actuate(TileEntity universalActuator);
}
