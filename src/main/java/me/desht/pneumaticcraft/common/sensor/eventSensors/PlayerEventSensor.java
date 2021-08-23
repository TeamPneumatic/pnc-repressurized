package me.desht.pneumaticcraft.common.sensor.eventSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IEventSensorSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

abstract class PlayerEventSensor implements IEventSensorSetting {

    @Override
    public String getSensorPath() {
        return "Player";
    }

    @Override
    public Set<EnumUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.ENTITY_TRACKER);
    }

    @Override
    public int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, String textboxText) {
        if (event instanceof PlayerEvent) {
            PlayerEntity player = ((PlayerEvent) event).getPlayer();
            if (Math.abs(player.getX() - sensor.getBlockPos().getX() + 0.5D) < range + 0.5D && Math.abs(player.getY() - sensor.getBlockPos().getY() + 0.5D) < range + 0.5D && Math.abs(player.getZ() - sensor.getBlockPos().getZ() + 0.5D) < range + 0.5D) {
                return emitRedstoneOnEvent((PlayerEvent) event, sensor, range);
            }
        }
        return 0;
    }

    protected abstract int emitRedstoneOnEvent(PlayerEvent event, TileEntity sensor, int range);

    @Override
    public int getRedstonePulseLength() {
        return 5;
    }

}
