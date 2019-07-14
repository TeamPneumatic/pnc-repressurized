package me.desht.pneumaticcraft.common.sensor.eventSensors;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.universal_sensor.IEventSensorSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashSet;
import java.util.Set;

abstract class PlayerEventSensor implements IEventSensorSetting {

    @Override
    public String getSensorPath() {
        return "Player";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        Set<Item> upgrades = new HashSet<>();
        upgrades.add(IItemRegistry.EnumUpgrade.ENTITY_TRACKER.getItem());
        return upgrades;
    }

    @Override
    public int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, String textboxText) {
        if (event instanceof PlayerEvent) {
            PlayerEntity player = ((PlayerEvent) event).getEntityPlayer();
            if (Math.abs(player.posX - sensor.getPos().getX() + 0.5D) < range + 0.5D && Math.abs(player.posY - sensor.getPos().getY() + 0.5D) < range + 0.5D && Math.abs(player.posZ - sensor.getPos().getZ() + 0.5D) < range + 0.5D) {
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
