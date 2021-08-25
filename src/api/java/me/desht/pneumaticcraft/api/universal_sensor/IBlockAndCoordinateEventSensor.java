package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

public interface IBlockAndCoordinateEventSensor extends IBaseSensor {
    /**
     * Extended version of the normal emitRedstoneOnEvent. This method will only invoke with a valid GPS tool, and when
     * all the coordinates are within range.
     *
     * @param event the Forge event (one of PlayerInteractEvent, EntityItemPickupEvent or AttackEntityEvent)
     * @param sensor the Universal Sensor tile entity
     * @param range the Universal Sensor's range, in blocks
     * @param positions When a GPS Tool is inserted this contains the position of that tool. If a GPS Area Tool is
     *                  inserted this is set of all positions in that area.
     * @return the redstone level that should be emitted
     */
    int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, Set<BlockPos> positions);

    /**
     * See {@link IEventSensorSetting#getRedstonePulseLength()}
     *
     * @return a redstone pulse length, in ticks
     */
    int getRedstonePulseLength();
}
