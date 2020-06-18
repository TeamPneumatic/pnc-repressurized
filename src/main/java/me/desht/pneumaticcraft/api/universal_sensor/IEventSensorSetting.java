package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

public interface IEventSensorSetting extends ISensorSetting {
    /**
     * This method is called when a Forge event of interest is triggered.  Events of interest are:
     * <ul>
     *     <li>{@link PlayerInteractEvent}</li>
     *     <li>{@link EntityItemPickupEvent}</li>
     *     <li>{@link AttackEntityEvent}</li>
     * </ul>
     *
     * @param event the Forge event (one of PlayerInteractEvent, EntityItemPickupEvent or AttackEntityEvent)
     * @param sensor the Universal Sensor tile entity
     * @param range the Universal Sensor's range, in blocks
     * @param textboxText any text which was entered in the sensor configuration's textfield
     * @return the redstone strength which should be emitted
     */
    int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, String textboxText);

    /**
     * How long should an emitted pulse last for?  5 ticks is a suitable value.
     *
     * @return a redstone pulse length, in ticks
     */
    int getRedstonePulseLength();
}
