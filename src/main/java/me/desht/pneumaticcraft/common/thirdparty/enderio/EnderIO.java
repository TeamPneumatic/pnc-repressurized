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

package me.desht.pneumaticcraft.common.thirdparty.enderio;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class EnderIO implements IThirdParty {

    @Override
    public void init() {
//        registerFuel(ModFluids.DIESEL.get());
//        registerFuel(ModFluids.KEROSENE.get());
//        registerFuel(ModFluids.GASOLINE.get());
//        registerFuel(ModFluids.LPG.get());
    }

//    private void registerFuel(Fluid fluid) {
//        IFuelRegistry api = PneumaticCraftAPIHandler.getInstance().getFuelRegistry();
//        float burnTime = api.getBurnRateMultiplier(fluid) * 60;
//        registerFuel(fluid, (int)burnTime, api.getFuelValue(fluid) / (int)burnTime);
//    }
//
//    private void registerFuel(Fluid fluid, int powerPerCycle, int burnTime) {
//        CompoundNBT tag = new CompoundNBT();
//        tag.putString("fluidName", fluid.getRegistryName().toString());
//        tag.putInt("powerPerCycle", powerPerCycle);
//        tag.putInt("totalBurnTime", burnTime);
//        // TODO 1.14 is this right?
//        InterModComms.sendTo(ModIds.EIO, "fluidFuel:add", () -> tag);
//    }

    @Override
    public void postInit() {
//        IThirdParty.registerFuel("enderio:hootch", 60 * 6000);
//        IThirdParty.registerFuel("enderio:rocket_fuel", 160 * 7000);
//        IThirdParty.registerFuel("enderio:fire_water",80 * 15000);
    }
}
