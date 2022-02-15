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

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.item.TubeModuleItem;
import me.desht.pneumaticcraft.lib.PneumaticValues;

public class SafetyValveModule extends AbstractRedstoneReceivingModule {
    private boolean shouldAddVenting = false;

    public SafetyValveModule(TubeModuleItem item) {
        super(item);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (shouldAddVenting) {
            pressureTube.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).orElseThrow(RuntimeException::new)
                    .enableSafetyVenting(p -> p > getThreshold(), getDirection());
            shouldAddVenting = false;
        }
    }

    @Override
    public float getThreshold() {
        if (upgraded) return super.getThreshold();

        // 4.92 instead of 4.9 because if the system is being fed via regulator from a high pressure line,
        // then it will be at 4.9 bar, which would cause safety modules to leak unnecessarily...
        return getTube().getDangerPressure() == PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE ? 19.9f : 4.92f;
    }

    @Override
    public void setTube(PressureTubeBlockEntity tube) {
        super.setTube(tube);

        // can't add immediately because the tube's world could be null (this is called on world load as well as
        // when player places module)
        shouldAddVenting = true;
    }

    @Override
    public void onRemoved() {
        if (!pressureTube.nonNullLevel().isClientSide) {
            pressureTube.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(h -> {
                h.disableSafetyVenting();
                h.setSideLeaking(null);
            });
        }
    }
}
