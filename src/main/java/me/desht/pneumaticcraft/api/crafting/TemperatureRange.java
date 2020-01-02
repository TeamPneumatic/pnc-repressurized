package me.desht.pneumaticcraft.api.crafting;

import org.apache.commons.lang3.Validate;

/**
 * Defines a valid operating temperature range for machines which use heat.  Temperatures are in Kelvin (i.e. absolute),
 * so negative values are not accepted.
 */
public class TemperatureRange {
    private static TemperatureRange INVALID = new TemperatureRange(0, 0) {
        @Override
        public boolean inRange(int temp) {
            return false;
        }
    };
    private static TemperatureRange ANY = new TemperatureRange(0, Integer.MAX_VALUE);

    private final int min;
    private final int max;

    private TemperatureRange(int min, int max) {
        Validate.isTrue(min >= 0 && max >= 0, "negative temperatures are not accepted!");
        Validate.isTrue(min <= max, "min temp must be <= max temp!");
        this.min = min;
        this.max = max;
    }

    /**
     * Get the minimum temperature for this range.
     *
     * @return the minimum temperature
     */
    public int getMin() {
        return min;
    }

    /**
     * Get the maximum temperature for this range.
     *
     * @return the maximum temperature
     */
    public int getMax() {
        return max;
    }

    /**
     * Get a specific temperature range.
     *
     * @param minTemp an inclusive minimum temperature
     * @param maxTemp an inclusive maximum temperature
     * @return a temperature range
     */
    public static TemperatureRange of(int minTemp, int maxTemp) {
        return new TemperatureRange(minTemp, maxTemp);
    }

    /**
     * Special "don't care" temperature range which always accepts any temperature.
     *
     * @return a temperature range
     */
    public static TemperatureRange any() {
        return ANY;
    }

    /**
     * Get a temperature range which accepts temperatures higher than or equal to the supplied value.
     *
     * @param minTemp an inclusive minimum temperature
     * @return a temperature range
     */
    public static TemperatureRange min(int minTemp) {
        return new TemperatureRange(minTemp, Integer.MAX_VALUE);
    }

    /**
     * Get a temperature range which accepts temperatures lower than or equal to the supplied value.
     *
     * @param maxTemp an inclusive maximum temperature
     * @return a temperature range
     */
    public static TemperatureRange max(int maxTemp) {
        return new TemperatureRange(0, maxTemp);
    }

    /**
     * Check if the given temperature is valid for this range object.
     *
     * @param temp the temperature
     * @return true if valid, false otherwise
     */
    public boolean inRange(int temp) {
        return temp >= min && temp <= max;
    }

    /**
     * Check if the given temperature is valid for this range object.
     *
     * @param temp the temperature
     * @return true if valid, false otherwise
     */
    public boolean inRange(double temp) {
        return (int)temp >= min && (int)temp <= max;
    }

    /**
     * Get a temperature range which never accepts any temperature as valid.
     * @return a temperature range
     */
    public static TemperatureRange invalid() {
        return INVALID;
    }

    /**
     * Check if this temperature is the special "don't care" temperature, as returned by {@link #any()}.
     *
     * @return true if this temperature is a "don't care".
     */
    public boolean isAny() {
        return this == TemperatureRange.ANY;
    }
}
