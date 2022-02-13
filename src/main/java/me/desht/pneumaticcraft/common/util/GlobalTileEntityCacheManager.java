/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.entity.AerialInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.UniversalSensorBlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.EffectiveSide;

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

    public final GlobalTileEntityCache<UniversalSensorBlockEntity> universalSensors = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<ChargingStationBlockEntity> chargingStations = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<SecurityStationBlockEntity> securityStations = new GlobalTileEntityCache<>();
    public final GlobalTileEntityCache<AerialInterfaceBlockEntity> aerialInterfaces = new GlobalTileEntityCache<>();

    private void removeFromWorld(LevelAccessor world){
        universalSensors.removeFromWorld(world);
        chargingStations.removeFromWorld(world);
        securityStations.removeFromWorld(world);
        aerialInterfaces.removeFromWorld(world);
    }

    public static class GlobalTileEntityCache<T extends BlockEntity> implements Iterable<T>{
        private final Set<T> tileEntities = Collections.newSetFromMap(new WeakHashMap<>());
        
        public void add(T te){
            tileEntities.add(te);
        }
        
        public void remove(T te){
            tileEntities.remove(te);
        }
        
        public void removeFromWorld(LevelAccessor world){
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


