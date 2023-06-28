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

package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IClientArmorRegistry;
import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.misc.DamageSources;
import me.desht.pneumaticcraft.api.misc.IMiscHelpers;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachineFactory;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorRegistry;
import me.desht.pneumaticcraft.api.upgrade.IUpgradeRegistry;
import me.desht.pneumaticcraft.api.wrench.IWrenchRegistry;
import me.desht.pneumaticcraft.client.ClientRegistryImpl;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.common.drone.DroneRegistry;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.common.pressure.AirHandlerMachineFactory;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;

public enum PneumaticCraftAPIHandler implements PneumaticRegistry.IPneumaticCraftInterface {
    INSTANCE;
//    private final static PneumaticCraftAPIHandler INSTANCE = new PneumaticCraftAPIHandler();

    public static PneumaticCraftAPIHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public IPneumaticRecipeRegistry getRecipeRegistry() {
        return PneumaticRecipeRegistry.getInstance();
    }

    @Override
    public IAirHandlerMachineFactory getAirHandlerMachineFactory() {
        return AirHandlerMachineFactory.getInstance();
    }

    @Override
    public IClientArmorRegistry getClientArmorRegistry() {
        return ClientArmorRegistry.getInstance();
    }

    @Override
    public ICommonArmorRegistry getCommonArmorRegistry() {
        return CommonArmorRegistry.getInstance();
    }

    @Override
    public IDroneRegistry getDroneRegistry() {
        return DroneRegistry.getInstance();
    }

    @Override
    public IHeatRegistry getHeatRegistry() {
        return HeatExchangerManager.getInstance();
    }

    @Override
    public IClientRegistry getClientRegistry() {
        return ClientRegistryImpl.getInstance();
    }

    @Override
    public ISensorRegistry getSensorRegistry() {
        return SensorHandler.getInstance();
    }

    @Override
    public IItemRegistry getItemRegistry() {
        return ItemRegistry.getInstance();
    }

    @Override
    public IUpgradeRegistry getUpgradeRegistry() {
        return ApplicableUpgradesDB.getInstance();
    }

    @Override
    public IFuelRegistry getFuelRegistry() {
        return FuelRegistry.getInstance();
    }

    @Override
    public IWrenchRegistry getWrenchRegistry() {
        return ModdedWrenchUtils.getInstance();
    }

    @Override
    public IMiscHelpers getMiscHelpers() {
        return MiscAPIHandler.getInstance();
    }

    @Override
    public DamageSources getDamageSources() {
        return PNCDamageSource.DamageSourcesImpl.INSTANCE;
    }
}
