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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.LevelEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * Helper which allows querying BE's of specific types, like the Universal Sensor, Security Station and Charging Station
 * This is very important for performance, especially in worlds with many block entities.
 *
 * @author MineMaarten
 */
@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public class GlobalBlockEntityCacheManager {
    private static final GlobalBlockEntityCacheManager CLIENT_INSTANCE = new GlobalBlockEntityCacheManager();
    private static final GlobalBlockEntityCacheManager SERVER_INSTANCE = new GlobalBlockEntityCacheManager();

    private final GlobalTileEntityCache<UniversalSensorBlockEntity> universalSensors = new GlobalTileEntityCache<>();
    private final GlobalTileEntityCache<ChargingStationBlockEntity> chargingStations = new GlobalTileEntityCache<>();
    private final GlobalTileEntityCache<SecurityStationBlockEntity> securityStations = new GlobalTileEntityCache<>();
    private final GlobalTileEntityCache<AerialInterfaceBlockEntity> aerialInterfaces = new GlobalTileEntityCache<>();

    private void removeFromWorld(LevelAccessor level) {
        universalSensors.removeFromWorld(level);
        chargingStations.removeFromWorld(level);
        securityStations.removeFromWorld(level);
        aerialInterfaces.removeFromWorld(level);
    }

    public static GlobalBlockEntityCacheManager getInstance(@Nullable LevelAccessor level) {
        return level != null && level.isClientSide() ? CLIENT_INSTANCE : SERVER_INSTANCE;
    }

    @SubscribeEvent
    public static void onWorldUnloaded(LevelEvent.Unload event) {
        getInstance(event.getLevel()).removeFromWorld(event.getLevel());
    }

    public GlobalTileEntityCache<UniversalSensorBlockEntity> getUniversalSensors() {
        return universalSensors;
    }

    public GlobalTileEntityCache<ChargingStationBlockEntity> getChargingStations() {
        return chargingStations;
    }

    public GlobalTileEntityCache<SecurityStationBlockEntity> getSecurityStations() {
        return securityStations;
    }

    public GlobalTileEntityCache<AerialInterfaceBlockEntity> getAerialInterfaces() {
        return aerialInterfaces;
    }

    public static class GlobalTileEntityCache<T extends BlockEntity> implements Iterable<T> {
        private final Set<T> blockEntities = Collections.newSetFromMap(new WeakHashMap<>());

        public void add(T blockEntity) {
            blockEntities.add(blockEntity);
        }

        public void remove(T blockEntity) {
            blockEntities.remove(blockEntity);
        }

        public void removeFromWorld(LevelAccessor world) {
            blockEntities.removeIf(be -> be.getLevel() == world);
        }

        public Stream<T> stream() {
            return blockEntities.stream();
        }

        @Override
        public Iterator<T> iterator() {
            return blockEntities.iterator();
        }
    }
}


