package pneumaticCraft.common;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerUniversalSensor{
    @SubscribeEvent
    public void onInteraction(PlayerInteractEvent event){
        sendEventToSensors(event.entity.worldObj, event);
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event){
        sendEventToSensors(event.entity.worldObj, event);
    }

    @SubscribeEvent
    public void onItemPickUp(EntityItemPickupEvent event){
        sendEventToSensors(event.entity.worldObj, event);
    }

    private void sendEventToSensors(World world, Event event){
        if(!world.isRemote) {
            for(TileEntity te : (List<TileEntity>)world.loadedTileEntityList) {
                if(te instanceof TileEntityUniversalSensor) {
                    ((TileEntityUniversalSensor)te).onEvent(event);
                }
            }
        }
    }
}
