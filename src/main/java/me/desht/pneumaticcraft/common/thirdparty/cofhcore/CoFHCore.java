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

package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.neoforged.fml.ModList;

public class CoFHCore implements IThirdParty {
    @Override
    public void init() {
        // note: fuel registration is now done by datapack: see conditional recipes in ModRecipeProvider

        // Registers items to be compatible with CoFH Core enchantments
        if(ModList.get().isLoaded(ModIds.COFH_CORE)) {
            COFHEnchantmentCompatibility.makeHoldingCompatible();
        }

        // Holding enchantment adds another volume multiplier
        if(ModList.get().isLoaded(ModIds.COFH_CORE)) {
            HoldingEnchantableProvider.registerVolumeModifier();
        }

        // Launching compatibility with Thermal Foundation TNT and Grenades
        if (ModList.get().isLoaded(ModIds.THERMAL_FOUNDATION)) {
            ThermalFoundationExplosiveLaunching.registerExplosiveLaunchBehaviour();
        }

        // Launching compatibility with Thermal Locomotion Minecarts
        if (ModList.get().isLoaded(ModIds.THERMAL_LOCOMOTION)) {
            ThermalLocomotionMinecartLaunching.registerMinecartLaunchBehaviour();
        }
    }
}
