package pneumaticCraft.api.universalSensor;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.Event;

public interface IBlockAndCoordinateEventSensor{
    /**
     * See {@link ISensorSetting#getSensorPath()}
     * @return
     */
    public String getSensorPath();

    /**
     * Extended version of the normal emitRedstoneOnEvent. This method will only invoke with a valid GPS tool, and when the coordinate is within range.
     * @param event
     * @param sensor
     * @param range
     * @param toolX
     * @param toolY
     * @param toolZ
     * @return
     */
    public int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, int toolX, int toolY, int toolZ);

    /**
     * See {@link IEventSensorSetting#getRedstonePulseLength()}
     * @return
     */
    public int getRedstonePulseLength();

    /**
     * See {@link ISensorSetting#needsTextBox()}
     * @return
     */
    public boolean needsTextBox();

    /**
     * See {@link ISensorSetting#getDescription()}
     * @return
     */
    public List<String> getDescription();
}
