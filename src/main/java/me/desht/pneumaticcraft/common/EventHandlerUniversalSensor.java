package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerUniversalSensor {
    @SubscribeEvent
    public void onInteraction(PlayerInteractEvent event) {
        sendEventToSensors(event.getEntity().world, event);
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        sendEventToSensors(event.getEntity().world, event);
    }

    @SubscribeEvent
    public void onItemPickUp(EntityItemPickupEvent event) {
        sendEventToSensors(event.getEntity().world, event);
    }

    private void sendEventToSensors(World world, Event event) {
        if (!world.isRemote) {
            for (TileEntity te : world.loadedTileEntityList) {
                if (te instanceof TileEntityUniversalSensor) {
                    ((TileEntityUniversalSensor) te).onEvent(event);
                }
            }
        }
    }
}
