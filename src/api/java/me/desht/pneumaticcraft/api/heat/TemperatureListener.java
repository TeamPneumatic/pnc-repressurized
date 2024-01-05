package me.desht.pneumaticcraft.api.heat;

/**
 * An implementation of this can be registered with {@link IHeatExchangerLogic#addTemperatureListener(TemperatureListener)}
 * to monitor changes in the heat exchanger's temperature. Take care to also call
 * {@link IHeatExchangerLogic#removeTemperatureListener(TemperatureListener)} when the object that implements this
 * goes out of scope, to avoid memory leaks.
 */
@FunctionalInterface
public interface TemperatureListener {
    /**
     * Called when the monitored heat exchanger's temperature changes.
     *
     * @param prevTemperature the previous temperature
     * @param newTemperature  the new temperature
     */
    void onTemperatureChanged(double prevTemperature, double newTemperature);
}
