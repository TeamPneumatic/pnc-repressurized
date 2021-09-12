package me.desht.pneumaticcraft.api.pressure;

import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;

public class PressureHelper {
    private static final float[] UPGRADE_CACHE = new float[65];  // 0..64 inclusive

    /**
     * Implement the PneumaticCraft standard for Volume Upgrade calculations. The formula is
     * {@code}2 * sqrt(upgradeCount){@code}.
     *
     * @param baseVolume the base volume, before modification
     * @param upgradeCount the number of Volume Upgrades
     * @return the upgraded volume
     * @throws IllegalArgumentException if the upgrade count is not in the range 0..64 inclusive
     */
    public static int getUpgradedVolume(int baseVolume, int upgradeCount) {
        Validate.isTrue(upgradeCount >= 0 && upgradeCount <= 64, "upgrade count must be in range 0..64 inclusive!");

        if (UPGRADE_CACHE[upgradeCount] == 0f) {
            UPGRADE_CACHE[upgradeCount] = upgradeCount == 0 ? 1f : 2f * MathHelper.sqrt(upgradeCount);
        }
        return (int) (baseVolume * UPGRADE_CACHE[upgradeCount]);
    }
}
