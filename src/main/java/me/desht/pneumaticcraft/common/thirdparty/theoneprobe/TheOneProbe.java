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

package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;

public class TheOneProbe implements IThirdParty {
    public static boolean oneProbeEnabled = false;

    public static boolean isProbeEnabled(ItemStack stack) {
        return oneProbeEnabled && stack.getItem() == ProbeHelmet.PNEUMATIC_HELMET_PROBE.get();
    }

    @Override
    public void preInit(IEventBus modBus) {
        oneProbeEnabled = true;

        ProbeHelmet.init();
    }

    @Override
    public void init() {
        InterModComms.sendTo(ModIds.TOP, "getTheOneProbe", TOPInit::new);
    }
}
