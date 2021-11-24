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

package me.desht.pneumaticcraft.common.thirdparty.buildcraft;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class BuildCraft implements IThirdParty {

    @Override
    public void init() {
//        ItemStack stoneGear = PneumaticCraftUtils.getBuildcraftItemStack(EnumBuildcraftModule.CORE, "stoneGearItem");
//
//        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Itemss.compressedIronGear), " i ", "isi", " i ", 'i', Names.INGOT_IRON_COMPRESSED, 's', stoneGear));

        //PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("fuel"), 1500000);
    }

}
