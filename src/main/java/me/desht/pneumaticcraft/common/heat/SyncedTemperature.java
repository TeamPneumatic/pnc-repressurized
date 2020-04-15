package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.common.network.DescSynced;

/**
 * Designed to sync temperature to client but only when changed by a certain amount, the delta being dependent on the
 * temperature at the time.  Include as a field in your TE and mark as @DescSynced, or in an Entity and use the data
 * manager to sync the temperature.
 */
public class SyncedTemperature {
    private int currentTemp = 300;

    @DescSynced
    private int syncedTemp = -1;

    /**
     * Call client side to get the synced temperature.
     * @return the synced temperature
     */
    public int getSyncedTemp() {
        return syncedTemp;
    }

    /**
     * Call server side on a regular basis to set the temperature of the heat exchanger.
     *
     * @param currentTemp the heat exchanger's current temp
     */
    public void setCurrentTemp(double currentTemp) {
        this.currentTemp = (int) currentTemp;

        if (shouldSync()) {
            this.syncedTemp = (int) currentTemp;
        }
    }

    private boolean shouldSync() {
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
