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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.inventory.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES 
            = DeferredRegister.create(Registries.MENU, Names.MOD_ID);

    public static final Supplier<MenuType<AdvancedAirCompressorMenu>> ADVANCED_AIR_COMPRESSOR
            = register("advanced_air_compressor", AdvancedAirCompressorMenu::new);
    public static final Supplier<MenuType<AdvancedLiquidCompressorMenu>> ADVANCED_LIQUID_COMPRESSOR
            = register("advanced_liquid_compressor", AdvancedLiquidCompressorMenu::new);
    public static final Supplier<MenuType<AerialInterfaceMenu>> AERIAL_INTERFACE
            = register("aerial_interface", AerialInterfaceMenu::new);
    public static final Supplier<MenuType<AirCannonMenu>> AIR_CANNON
            = register("air_cannon", AirCannonMenu::new);
    public static final Supplier<MenuType<AirCompressorMenu>> AIR_COMPRESSOR
            = register("air_compressor", AirCompressorMenu::new);
    public static final Supplier<MenuType<AssemblyControllerMenu>> ASSEMBLY_CONTROLLER
            = register("assembly_controller", AssemblyControllerMenu::new);
    public static final Supplier<MenuType<ChargingStationMenu>> CHARGING_STATION
            = register("charging_station", ChargingStationMenu::new);
    public static final Supplier<MenuType<ChargingStationUpgradeManagerMenu>> CHARGING_MINIGUN
            = register("charging_minigun", ChargingStationUpgradeManagerMenu::createMinigunContainer);
    public static final Supplier<MenuType<ChargingStationUpgradeManagerMenu>> CHARGING_DRONE
            = register("charging_drone", ChargingStationUpgradeManagerMenu::createDroneContainer);
    public static final Supplier<MenuType<ChargingStationUpgradeManagerMenu>> CHARGING_ARMOR
            = register("charging_armor", ChargingStationUpgradeManagerMenu::createArmorContainer);
    public static final Supplier<MenuType<ChargingStationUpgradeManagerMenu>> CHARGING_JACKHAMMER
            = register("charging_jackhammer", ChargingStationUpgradeManagerMenu::createJackhammerContainer);
    public static final Supplier<MenuType<ChargingStationUpgradeManagerMenu>> CHARGING_AMADRON
            = register("charging_amadron", ChargingStationUpgradeManagerMenu::createAmadronContainer);
    public static final Supplier<MenuType<CreativeCompressorMenu>> CREATIVE_COMPRESSOR
            = register("creative_compressor", CreativeCompressorMenu::new);
    public static final Supplier<MenuType<ElectrostaticCompressorMenu>> ELECTROSTATIC_COMPRESSOR
            = register("electrostatic_compressor", ElectrostaticCompressorMenu::new);
    public static final Supplier<MenuType<ElevatorMenu>> ELEVATOR
            = register("elevator", ElevatorMenu::new);
    public static final Supplier<MenuType<FluxCompressorMenu>> FLUX_COMPRESSOR
            = register("flux_compressor", FluxCompressorMenu::new);
    public static final Supplier<MenuType<SolarCompressorMenu>> SOLAR_COMPRESSOR
            = register("solar_compressor", SolarCompressorMenu::new);
    public static final Supplier<MenuType<GasLiftMenu>> GAS_LIFT
            = register("gas_lift", GasLiftMenu::new);
    public static final Supplier<MenuType<KeroseneLampMenu>> KEROSENE_LAMP
            = register("kerosene_lamp", KeroseneLampMenu::new);
    public static final Supplier<MenuType<LiquidCompressorMenu>> LIQUID_COMPRESSOR
            = register("liquid_compressor", LiquidCompressorMenu::new);
    public static final Supplier<MenuType<LiquidHopperMenu>> LIQUID_HOPPER
            = register("liquid_hopper", LiquidHopperMenu::new);
    public static final Supplier<MenuType<OmnidirectionalHopperMenu>> OMNIDIRECTIONAL_HOPPER
            = register("omnidirectional_hopper", OmnidirectionalHopperMenu::new);
    public static final Supplier<MenuType<PneumaticDoorBaseMenu>> PNEUMATIC_DOOR_BASE
            = register("pneumatic_door_base", PneumaticDoorBaseMenu::new);
    public static final Supplier<MenuType<PneumaticDynamoMenu>> PNEUMATIC_DYNAMO
            = register("pneumatic_dynamo", PneumaticDynamoMenu::new);
    public static final Supplier<MenuType<PressureChamberInterfaceMenu>> PRESSURE_CHAMBER_INTERFACE
            = register("pressure_chamber_interface", PressureChamberInterfaceMenu::new);
    public static final Supplier<MenuType<PressureChamberValveMenu>> PRESSURE_CHAMBER_VALVE
            = register("pressure_chamber_valve", PressureChamberValveMenu::new);
    public static final Supplier<MenuType<ProgrammableControllerMenu>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ProgrammableControllerMenu::new);
    public static final Supplier<MenuType<ProgrammerMenu>> PROGRAMMER
            = register("programmer", ProgrammerMenu::new);
    public static final Supplier<MenuType<RefineryMenu>> REFINERY
            = register("refinery", RefineryMenu::new);
    public static final Supplier<MenuType<SecurityStationMainMenu>> SECURITY_STATION_MAIN
            = register("security_station_main", SecurityStationMainMenu::new);
    public static final Supplier<MenuType<SecurityStationHackingMenu>> SECURITY_STATION_HACKING
            = register("security_station_hacking", SecurityStationHackingMenu::new);
    public static final Supplier<MenuType<SentryTurretMenu>> SENTRY_TURRET
            = register("sentry_turret", SentryTurretMenu::new);
    public static final Supplier<MenuType<ThermalCompressorMenu>> THERMAL_COMPRESSOR
            = register("thermal_compressor", ThermalCompressorMenu::new);
    public static final Supplier<MenuType<ThermoPlantMenu>> THERMOPNEUMATIC_PROCESSING_PLANT
            = register("thermopneumatic_processing_plant", ThermoPlantMenu::new);
    public static final Supplier<MenuType<UniversalSensorMenu>> UNIVERSAL_SENSOR
            = register("universal_sensor", UniversalSensorMenu::new);
    public static final Supplier<MenuType<UVLightBoxMenu>> UV_LIGHT_BOX
            = register("uv_light_box", UVLightBoxMenu::new);
    public static final Supplier<MenuType<VacuumPumpMenu>> VACUUM_PUMP
            = register("vacuum_pump", VacuumPumpMenu::new);
    public static final Supplier<MenuType<AmadronMenu>> AMADRON
            = register("amadron", AmadronMenu::new);
    public static final Supplier<MenuType<AmadronAddTradeMenu>> AMADRON_ADD_TRADE
            = register("amadron_add_trade", AmadronAddTradeMenu::new);
    public static final Supplier<MenuType<MinigunMagazineMenu>> MINIGUN_MAGAZINE
            = register("minigun_magazine", MinigunMagazineMenu::new);
    public static final Supplier<MenuType<LogisticsMenu>> LOGISTICS_FRAME_STORAGE
            = register("logistics_frame_storage", LogisticsMenu::createStorageContainer);
    public static final Supplier<MenuType<LogisticsMenu>> LOGISTICS_FRAME_PROVIDER
            = register("logistics_frame_provider", LogisticsMenu::createProviderContainer);
    public static final Supplier<MenuType<LogisticsMenu>> LOGISTICS_FRAME_REQUESTER
            = register("logistics_frame_requester", LogisticsMenu::createRequesterContainer);
    public static final Supplier<MenuType<InventorySearcherMenu>> INVENTORY_SEARCHER
            = register("inventory_searcher", InventorySearcherMenu::new);
    public static final Supplier<MenuType<RemoteMenu>> REMOTE
            = register("remote", RemoteMenu::createRemoteContainer);
    public static final Supplier<MenuType<RemoteMenu>> REMOTE_EDITOR
            = register("remote_editor", RemoteMenu::createRemoteEditorContainer);
    public static final Supplier<MenuType<ItemSearcherMenu>> ITEM_SEARCHER
            = register("item_searcher", ItemSearcherMenu::new);
    public static final Supplier<MenuType<EtchingTankMenu>> ETCHING_TANK
            = register("etching_tank", EtchingTankMenu::new);
    public static final Supplier<MenuType<FluidTankMenu>> FLUID_TANK
            = register("fluid_tank", FluidTankMenu::new);
    public static final Supplier<MenuType<ReinforcedChestMenu>> REINFORCED_CHEST
            = register("reinforced_chest", ReinforcedChestMenu::new);
    public static final Supplier<MenuType<SmartChestMenu>> SMART_CHEST
            = register("smart_chest", SmartChestMenu::new);
    public static final Supplier<MenuType<TagWorkbenchMenu>> TAG_MATCHER
            = register("tag_workbench", TagWorkbenchMenu::new);
    public static final Supplier<MenuType<FluidMixerMenu>> FLUID_MIXER
            = register("fluid_mixer", FluidMixerMenu::new);
    public static final Supplier<MenuType<JackhammerSetupMenu>> JACKHAMMER_SETUP
            = register("jackhammer_setup", JackhammerSetupMenu::new);
    public static final Supplier<MenuType<VacuumTrapMenu>> VACUUM_TRAP
            = register("vacuum_trap", VacuumTrapMenu::new);
    public static final Supplier<MenuType<SpawnerExtractorMenu>> SPAWNER_EXTRACTOR
            = register("spawner_extractor", SpawnerExtractorMenu::new);
    public static final Supplier<MenuType<PressurizedSpawnerMenu>> PRESSURIZED_SPAWNER
            = register("pressurized_spawner", PressurizedSpawnerMenu::new) ;
    public static final Supplier<MenuType<CreativeCompressedIronBlockMenu>> CREATIVE_COMPRESSED_IRON_BLOCK
            = register("creative_compressed_iron_block", CreativeCompressedIronBlockMenu::new);

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> Supplier<T> register(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return MENU_TYPES.register(name, () -> (T) IMenuTypeExtension.create(f));
    }
}
