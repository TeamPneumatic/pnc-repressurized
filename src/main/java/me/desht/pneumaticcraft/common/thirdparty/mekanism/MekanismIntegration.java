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

package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.common.thirdparty.RadiationSourceCheck;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.common.registries.MekanismDamageSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class MekanismIntegration {
    public static final Capability<IHeatHandler> CAPABILITY_HEAT_HANDLER = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IRadiationShielding> CAPABILITY_RADIATION_SHIELDING = CapabilityManager.get(new CapabilityToken<>() {});

    static void mekSetup() {
        // FIXME non-api usage here (ask Mek team to provide an API method?)
        RadiationSourceCheck.INSTANCE.registerRadiationSource(s -> s == MekanismDamageSource.RADIATION);
    }
}
