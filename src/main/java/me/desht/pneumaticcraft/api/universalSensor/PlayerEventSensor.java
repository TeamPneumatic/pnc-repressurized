package me.desht.pneumaticcraft.api.universalSensor;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashSet;
import java.util.Set;

public abstract class PlayerEventSensor implements IEventSensorSetting {

    @Override
    public String getSensorPath() {
        return "Player";
    }

    @Override
    public Set<Item> getRequiredUpgrades() {
        Set<Item> upgrades = new HashSet<Item>();
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.ENTITY_TRACKER));
        return upgrades;
    }

    @Override
    public int emitRedstoneOnEvent(Event event, TileEntity sensor, int range, String textboxText) {
        if (event instanceof PlayerEvent) {
            EntityPlayer player = ((PlayerEvent) event).getEntityPlayer();
            if (Math.abs(player.posX - sensor.getPos().getX() + 0.5D) < range + 0.5D && Math.abs(player.posY - sensor.getPos().getY() + 0.5D) < range + 0.5D && Math.abs(player.posZ - sensor.getPos().getZ() + 0.5D) < range + 0.5D) {
                return emitRedstoneOnEvent((PlayerEvent) event, sensor, range);
            }
        }
        return 0;
    }

    public abstract int emitRedstoneOnEvent(PlayerEvent event, TileEntity sensor, int range);

    @Override
    public int getRedstonePulseLength() {
        return 5;
    }

}
