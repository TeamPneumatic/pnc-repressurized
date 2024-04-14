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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractAirCanisterItem extends PressurizableItem {
    private AbstractAirCanisterItem(int maxAir, int volume) {
        super(ModItems.defaultProps(), maxAir, volume);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        // only completely empty (freshly crafted) canisters may stack
        // this makes it easier for players when needed in a crafting recipe
        return getPressure(stack) > 0f ? 1 : super.getMaxStackSize(stack);
    }

    public static class Basic extends AbstractAirCanisterItem {
        public Basic() {
            super(PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
        }
    }

    public static class Reinforced extends AbstractAirCanisterItem {
        public Reinforced() {
            super(PneumaticValues.REINFORCED_AIR_CANISTER_MAX_AIR, PneumaticValues.REINFORCED_AIR_CANISTER_VOLUME);
        }
    }
}
