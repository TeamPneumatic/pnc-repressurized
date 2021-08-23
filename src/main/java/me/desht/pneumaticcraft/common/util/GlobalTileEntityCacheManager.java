package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * Helper which allows querying TE's of specific types, like the Universal Sensor, Security Station and Charging Station
 * This is very important for performance, especially in worlds with many tile entities.
 *
 * @author MineMaarten
 */
@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public class GlobalTileEntityCacheManager{
    private static final GlobalTileEntityCacheManager CLIENT_INSTANCE = new GlobalTileEntityCacheManager();
    private static final GlobalTileEntityCacheManager SERVER_INSTANCE = new GlobalTileEntityCacheManager();
    
    public static GlobalTileEntityCacheManager getInstance(){
        return EffectiveSide.get() == LogicalSide.CLIENT ? CLIENT_INSTANCE : SERVER_INSTANCE;
    }
    
    @SubscribeEvent
    public static void onWorldUnloaded(WorldEvent.Unload event){
        getInstance().removeFromWorld(event.getWorld());
    }

//    private final Map<TileEntityType<? extends TileEntity>, GlobalTileEntityCache<? extends TileEntity>> cacheMap = new HashMap<>();

    public final GlobalTileEntityCache<TileEntityUniversalSensor> universalSensors = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<TileEntityChargingStation> chargingStations = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<TileEntitySecurityStation> securityStations = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<TileEntityAerialInterface> aerialInterfaces = new GlobalTileEntityCache<>();

    private void removeFromWorld(IWorld world){
        universalSensors.removeFromWorld(world);
        chargingStations.removeFromWorld(world);
        securityStations.removeFromWorld(world);
        aerialInterfaces.removeFromWorld(world);
    }

    public static class GlobalTileEntityCache<T extends TileEntity> implements Iterable<T>{
        private final Set<T> tileEntities = Collections.newSetFromMap(new WeakHashMap<>());
        
        public void add(T te){
            tileEntities.add(te);
        }
        
        public void remove(T te){
            tileEntities.remove(te);
        }
        
        public void removeFromWorld(IWorld world){
            tileEntities.removeIf(te -> te.getLevel() == world);
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


