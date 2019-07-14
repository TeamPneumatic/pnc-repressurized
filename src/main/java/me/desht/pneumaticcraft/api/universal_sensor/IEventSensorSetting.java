package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.eventbus.api.Event;

public interface IEventSensorSetting extends ISensorSetting {

    /**
     * This method is only invoked when a subscribed event is triggered.
     *
     * @param event
     * @param sensor
     * @param range
     * @param textboxText
     * @return Redstone strength for the given event.
     */
    int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, String textboxText);

    /**
     * Should return how long a pulse should hold in ticks. By default this is 5 ticks (1/4 second).
     *
     * @return
     */
    int getRedstonePulseLength();
}
