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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.SpeedBoostClientHandler;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.core.component.DataComponentType;

public class SpeedBoostOptions extends AbstractSliderOptions<SpeedBoostClientHandler> {
    public SpeedBoostOptions(IGuiScreen screen, SpeedBoostClientHandler handler) {
        super(screen, handler);
    }

    @Override
    protected DataComponentType<Integer> getIntegerComponent() {
        return ModDataComponents.SPEED_BOOST_PCT.get();
    }
}
