package me.desht.pneumaticcraft.common.sensor.eventSensors;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerAttackSensor extends PlayerEventSensor {

    @Override
    public String getSensorPath() {
        return super.getSensorPath() + "/Player Attack";
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public int emitRedstoneOnEvent(PlayerEvent event, TileEntity sensor, int range) {
        if (event instanceof AttackEntityEvent) {
            return 15;
        }
        return 0;
    }
}
