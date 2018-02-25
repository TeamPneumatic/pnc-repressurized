package me.desht.pneumaticcraft.common.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Helper which allows querying TE's of specific (owned) types, like the universal sensor, Security Station and Charging Station
 * @author MineMaarten
 */
@EventBusSubscriber(modid = Names.MOD_ID)
public class GlobalTileEntityCacheManager{
    private static final GlobalTileEntityCacheManager CLIENT_INSTANCE = new GlobalTileEntityCacheManager();
    private static final GlobalTileEntityCacheManager SERVER_INSTANCE = new GlobalTileEntityCacheManager();
    
    public static GlobalTileEntityCacheManager getInstance(){
        return PneumaticCraftRepressurized.proxy.getSide() == Side.CLIENT ? CLIENT_INSTANCE : SERVER_INSTANCE;
    }
    
    @SubscribeEvent
    public static void onWorldUnloaded(WorldEvent.Unload event){
        getInstance().removeFromWorld(event.getWorld());
    }
    
    public final GlobalTileEntityCache<TileEntityUniversalSensor> universalSensors = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<TileEntityChargingStation> chargingStations = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<TileEntitySecurityStation> securityStations = new GlobalTileEntityCache<>();
    
    private void removeFromWorld(World world){
        universalSensors.removeFromWorld(world);
        chargingStations.removeFromWorld(world);
        securityStations.removeFromWorld(world);
    }
    
    public static class GlobalTileEntityCache<T extends TileEntity> implements Iterable<T>{
        private final Set<T> tileEntities = Collections.newSetFromMap(new WeakHashMap<>());
        
        public void add(T te){
            tileEntities.add(te);
        }
        
        public void remove(T te){
            tileEntities.remove(te);
        }
        
        public void removeFromWorld(World world){
            tileEntities.removeIf(te -> te.getWorld() == world);
        }
        
        public Stream<T> stream(){
            return tileEntities.stream();
        }

        @Override
        public Iterator<T> iterator(){
            return tileEntities.iterator();
        }
    }
}


