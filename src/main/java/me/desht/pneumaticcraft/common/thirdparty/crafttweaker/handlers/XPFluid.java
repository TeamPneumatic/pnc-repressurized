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

package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("mods.pneumaticcraft.XPFluid")
@ZenRegister
public class XPFluid {
    @ZenCodeType.Method
    public void registerXPFluid(CTFluidIngredient fluid, int ratio) {
        CraftTweakerAPI.apply(new XPFluidActionAdd(fluid, ratio));
    }

    @ZenCodeType.Method
    public void unregisterXPFluid(CTFluidIngredient fluid) {
        CraftTweakerAPI.apply(new XPFluidActionAdd(fluid, 0));
    }

    public static class XPFluidActionAdd implements IRuntimeAction {
        private final CTFluidIngredient fluidIngredient;
        private final int ratio;

        public XPFluidActionAdd(CTFluidIngredient fluidIngredient, int ratio) {
            this.fluidIngredient = fluidIngredient;
            this.ratio = ratio;
        }

        @Override
        public void apply() {
            XPFluidManager.getInstance().registerXPFluid(CTUtils.toFluidIngredient(fluidIngredient), ratio);
        }

        @Override
        public String describe() {
            if (ratio <= 0) {
                return "Unregistering XP fluid " + fluidIngredient.getCommandString();
            } else {
                return "Registering XP fluid " + fluidIngredient.getCommandString() + " with mB->XP ratio: " + ratio;
            }
        }

        @Override
        public String systemName() {
            return PneumaticRegistry.MOD_ID;
        }
    }
}
