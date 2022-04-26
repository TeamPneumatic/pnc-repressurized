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

package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.util.WildcardedRLMatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;

public class WorldGenFiltering {
    private static WildcardedRLMatcher dimensionMatcherB = null;
    private static WildcardedRLMatcher dimensionMatcherW = null;
    private static WildcardedRLMatcher biomeMatcherB = null;
    private static WildcardedRLMatcher biomeMatcherW = null;

    public static void clearMatcherCaches() {
        dimensionMatcherB = null;
        dimensionMatcherW = null;
        biomeMatcherB = null;
        biomeMatcherW = null;
    }

    static boolean isBiomeOK(ResourceLocation biomeName) {
        if (biomeMatcherB == null) {
            biomeMatcherB = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenBlacklist.get());
        }
        if (biomeMatcherW == null) {
            biomeMatcherW = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenWhitelist.get());
        }
        // non-empty whitelist match OR no blacklist match
        return biomeMatcherW.isEmpty() ? !biomeMatcherB.test(biomeName) : biomeMatcherW.test(biomeName);
    }

    static boolean isDimensionOK(WorldGenLevel level) {
        if (dimensionMatcherB == null) {
            dimensionMatcherB = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenDimensionBlacklist.get());
        }
        if (dimensionMatcherW == null) {
            dimensionMatcherW = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenDimensionWhitelist.get());
        }
        // non-empty whitelist match OR no blacklist match
        ResourceLocation name = level.getLevel().dimension().location();
        return dimensionMatcherW.isEmpty() ? !dimensionMatcherB.test(name) : dimensionMatcherW.test(name);
    }
}
