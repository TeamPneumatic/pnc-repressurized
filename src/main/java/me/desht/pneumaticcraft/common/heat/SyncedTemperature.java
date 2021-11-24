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

package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.network.DescSynced;

/**
 * Designed to sync temperature to client but only when changed by a certain amount, the delta being dependent on the
 * temperature at the time.  Include as a field in your TE and mark as @DescSynced, or in an Entity and use the data
 * manager to sync the temperature.
 */
public class SyncedTemperature {
    private static final int SYNC_RATE = 60;

    private final IHeatExchangerLogic logic;

    private int syncTimer = -1;
    private int pendingTemp;

    @DescSynced
    private int syncedTemp = -1;

    public SyncedTemperature(IHeatExchangerLogic logic) {
        this.logic = logic;
    }

    /**
     * Call client side to get the synced temperature.
     * @return the synced temperature
     */
    public int getSyncedTemp() {
        // -1 indicates no temp synced yet, so use the heat exchanger's default initial temp (generally the ambient biome temp)
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/602
        return syncedTemp == -1 ? logic.getTemperatureAsInt() : syncedTemp;
    }

    /**
     * Call server side on a regular basis.
     */
    public void tick() {
        int currentTemp = logic.getTemperatureAsInt();

        if (shouldSyncNow()) {
            // large temperature delta: sync immediately
            this.syncedTemp = currentTemp;
            syncTimer = -1;
        } else if (currentTemp != syncedTemp) {
            // small temperature delta: schedule a sync to happen in 60 ticks, unless one is already scheduled
            if (syncTimer == -1) syncTimer = SYNC_RATE;
            pendingTemp = currentTemp;
        }

        if (syncTimer >= 0) {
            if (--syncTimer == -1) {
                this.syncedTemp = pendingTemp;
            }
        }
    }

    private boolean shouldSyncNow() {
        int currentTemp = logic.getTemperatureAsInt();

        if (syncedTemp < 0) return true; // initial sync

        int delta = Math.abs(syncedTemp - currentTemp);

        if (currentTemp < 73) {
            return false;
        } else if (currentTemp < 473) {
            return delta >= 10;
        } else if (currentTemp < 873) {
            return delta >= 30;
        } else if (currentTemp < 1473) {
            return delta >= 80;
        } else {
            return false;
        }
    }
}
