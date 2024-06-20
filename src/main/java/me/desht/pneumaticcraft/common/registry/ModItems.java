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

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.entity.drone.*;
import me.desht.pneumaticcraft.common.fluid.FluidPlastic;
import me.desht.pneumaticcraft.common.item.*;
import me.desht.pneumaticcraft.common.item.DrillBitItem.DrillBitType;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem.NetworkComponentType;
import me.desht.pneumaticcraft.common.item.logistics.*;
import me.desht.pneumaticcraft.common.item.minigun.*;
import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.common.tubemodules.*;
import me.desht.pneumaticcraft.common.upgrades.BuiltinUpgrade;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Names.MOD_ID);

    public static final DeferredItem<Item> COMPRESSED_IRON_INGOT = register("ingot_iron_compressed");
    public static final DeferredItem<Item> PRESSURE_GAUGE = register("pressure_gauge");
    public static final DeferredItem<Item> STONE_BASE = register("stone_base");
    public static final DeferredItem<Item> CANNON_BARREL = register("cannon_barrel");
    public static final DeferredItem<Item> TURBINE_BLADE = register("turbine_blade");
    public static final DeferredItem<Item> PLASTIC = register("plastic");
    public static final DeferredItem<Item> PNEUMATIC_CYLINDER = register("pneumatic_cylinder");
    public static final DeferredItem<Item> TURBINE_ROTOR = register("turbine_rotor");
    public static final DeferredItem<Item> PCB_BLUEPRINT = register("pcb_blueprint");
    public static final DeferredItem<Item> TRANSISTOR = register("transistor");
    public static final DeferredItem<Item> CAPACITOR = register("capacitor");
    public static final DeferredItem<Item> PRINTED_CIRCUIT_BOARD = register("printed_circuit_board");
    public static final DeferredItem<Item> SOLAR_WAFER = register("solar_wafer");
    public static final DeferredItem<Item> SOLAR_CELL = register("solar_cell");
    public static final DeferredItem<Item> STOP_WORM = register("stop_worm");
    public static final DeferredItem<Item> NUKE_VIRUS = register("nuke_virus");
    public static final DeferredItem<Item> COMPRESSED_IRON_GEAR = register("compressed_iron_gear");
    public static final DeferredItem<Item> PROGRAMMING_PUZZLE = register("programming_puzzle");
    public static final DeferredItem<Item> MODULE_EXPANSION_CARD = register("module_expansion_card");
    public static final DeferredItem<Item> LOGISTICS_CORE = register("logistics_core");
    public static final DeferredItem<Item> UPGRADE_MATRIX = register("upgrade_matrix");
    public static final DeferredItem<Item> WHEAT_FLOUR = register("wheat_flour");
    public static final DeferredItem<Item> SOURDOUGH = register("sourdough");
    public static final DeferredItem<Item> SPAWNER_CORE_SHELL = register("spawner_core_shell");
    public static final DeferredItem<Item> REINFORCED_CHEST_KIT = register("reinforced_chest_kit", AbstractChestUpgradeKitItem.Reinforced::new);
    public static final DeferredItem<Item> SMART_CHEST_KIT = register("smart_chest_kit", AbstractChestUpgradeKitItem.Smart::new);
    public static final DeferredItem<Item> RAW_SALMON_TEMPURA = register("raw_salmon_tempura");
    public static final DeferredItem<Item> UNASSEMBLED_NETHERITE_DRILL_BIT = register("unassembled_netherite_drill_bit");
    public static final DeferredItem<Item> COPPER_NUGGET = register("copper_nugget");

    public static final DeferredItem<Item> SOURDOUGH_BREAD = registerFood("sourdough_bread", ModFoods.SOURDOUGH);
    public static final DeferredItem<Item> CHIPS = registerFood("chips", ModFoods.CHIPS);
    public static final DeferredItem<Item> COD_N_CHIPS = registerFood("cod_n_chips", ModFoods.COD_N_CHIPS);
    public static final DeferredItem<Item> SALMON_TEMPURA = registerFood("salmon_tempura", ModFoods.SALMON_TEMPURA);

    public static final DeferredItem<GPSToolItem> GPS_TOOL = register("gps_tool", GPSToolItem::new);
    public static final DeferredItem<GPSAreaToolItem> GPS_AREA_TOOL = register("gps_area_tool", GPSAreaToolItem::new);
    public static final DeferredItem<RemoteItem> REMOTE = register("remote", RemoteItem::new);
    public static final DeferredItem<SeismicSensorItem> SEISMIC_SENSOR = register("seismic_sensor", SeismicSensorItem::new);
    public static final DeferredItem<MicromissilesItem> MICROMISSILES = register("micromissiles", MicromissilesItem::new);
    public static final DeferredItem<MemoryStickItem> MEMORY_STICK = register("memory_stick", MemoryStickItem::new);
    public static final DeferredItem<TagFilterItem> TAG_FILTER = register("tag_filter", TagFilterItem::new);
    public static final DeferredItem<ClassifyFilterItem> CLASSIFY_FILTER = register("classify_filter", ClassifyFilterItem::new);
    public static final DeferredItem<GlycerolItem> GLYCEROL = register("glycerol", GlycerolItem::new);
    public static final DeferredItem<BandageItem> BANDAGE = register("bandage", BandageItem::new);
    public static final DeferredItem<SpawnerCoreItem> SPAWNER_CORE = register("spawner_core", SpawnerCoreItem::new);

    public static final DeferredItem<PressurizableItem> AIR_CANISTER = register("air_canister",
            AbstractAirCanisterItem.Basic::new);
    public static final DeferredItem<PressurizableItem> REINFORCED_AIR_CANISTER = register("reinforced_air_canister",
            AbstractAirCanisterItem.Reinforced::new);
    public static final DeferredItem<VortexCannonItem> VORTEX_CANNON = register("vortex_cannon",
            VortexCannonItem::new);
    public static final DeferredItem<PneumaticWrenchItem> PNEUMATIC_WRENCH = register("pneumatic_wrench",
            PneumaticWrenchItem::new);
    public static final DeferredItem<ManometerItem> MANOMETER = register("manometer",
            ManometerItem::new);
    public static final DeferredItem<LogisticsConfiguratorItem> LOGISTICS_CONFIGURATOR = register("logistics_configurator",
            LogisticsConfiguratorItem::new);
    public static final DeferredItem<AmadronTabletItem> AMADRON_TABLET = register("amadron_tablet",
            AmadronTabletItem::new);
    public static final DeferredItem<MinigunItem> MINIGUN = register("minigun",
            MinigunItem::new);
    public static final DeferredItem<CamoApplicatorItem> CAMO_APPLICATOR = register("camo_applicator",
            CamoApplicatorItem::new);
    public static final DeferredItem<JackHammerItem> JACKHAMMER = register("jackhammer",
            JackHammerItem::new);

    public static final DeferredItem<Item> COMPRESSED_IRON_HELMET = register("compressed_iron_helmet",
            () -> new CompressedIronArmorItem(ArmorItem.Type.HELMET));
    public static final DeferredItem<Item> COMPRESSED_IRON_CHESTPLATE = register("compressed_iron_chestplate",
            () -> new CompressedIronArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final DeferredItem<Item> COMPRESSED_IRON_LEGGINGS = register("compressed_iron_leggings",
            () -> new CompressedIronArmorItem(ArmorItem.Type.LEGGINGS));
    public static final DeferredItem<Item> COMPRESSED_IRON_BOOTS = register("compressed_iron_boots",
            () -> new CompressedIronArmorItem(ArmorItem.Type.BOOTS));

    public static final DeferredItem<PneumaticArmorItem> PNEUMATIC_HELMET = register("pneumatic_helmet",
            () -> new PneumaticArmorItem(ArmorItem.Type.HELMET));
    public static final DeferredItem<PneumaticArmorItem> PNEUMATIC_CHESTPLATE = register("pneumatic_chestplate",
            () -> new PneumaticArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final DeferredItem<PneumaticArmorItem> PNEUMATIC_LEGGINGS = register("pneumatic_leggings",
            () -> new PneumaticArmorItem(ArmorItem.Type.LEGGINGS));
    public static final DeferredItem<PneumaticArmorItem> PNEUMATIC_BOOTS = register("pneumatic_boots",
            () -> new PneumaticArmorItem(ArmorItem.Type.BOOTS));

    public static final DeferredItem<AssemblyProgramItem> ASSEMBLY_PROGRAM_LASER = register(AssemblyRecipe.AssemblyProgramType.LASER);
    public static final DeferredItem<AssemblyProgramItem> ASSEMBLY_PROGRAM_DRILL = register(AssemblyRecipe.AssemblyProgramType.DRILL);
    public static final DeferredItem<AssemblyProgramItem> ASSEMBLY_PROGRAM_DRILL_LASER = register(AssemblyRecipe.AssemblyProgramType.DRILL_LASER);

    public static final DeferredItem<EmptyPCBItem> EMPTY_PCB = register("empty_pcb",
            EmptyPCBItem::new);
    public static final DeferredItem<NonDespawningItem> UNASSEMBLED_PCB = register("unassembled_pcb",
            NonDespawningItem::new);
    public static final DeferredItem<NonDespawningItem> FAILED_PCB = register("failed_pcb",
            NonDespawningItem::new);

    public static final DeferredItem<NetworkComponentItem> DIAGNOSTIC_SUBROUTINE = register(NetworkComponentType.DIAGNOSTIC_SUBROUTINE);
    public static final DeferredItem<NetworkComponentItem> NETWORK_API = register(NetworkComponentType.NETWORK_API);
    public static final DeferredItem<NetworkComponentItem> NETWORK_DATA_STORAGE = register(NetworkComponentType.NETWORK_DATA_STORAGE);
    public static final DeferredItem<NetworkComponentItem> NETWORK_IO_PORT = register(NetworkComponentType.NETWORK_IO_PORT);
    public static final DeferredItem<NetworkComponentItem> NETWORK_REGISTRY = register(NetworkComponentType.NETWORK_REGISTRY);
    public static final DeferredItem<NetworkComponentItem> NETWORK_NODE = register(NetworkComponentType.NETWORK_NODE);

    public static final DeferredItem<DrillBitItem> IRON_DRILL_BIT = register(DrillBitType.IRON);
    public static final DeferredItem<DrillBitItem> COMPRESSED_IRON_DRILL_BIT = register(DrillBitType.COMPRESSED_IRON);
    public static final DeferredItem<DrillBitItem> DIAMOND_DRILL_BIT = register(DrillBitType.DIAMOND);
    public static final DeferredItem<DrillBitItem> NETHERITE_DRILL_BIT = register(DrillBitType.NETHERITE);

    public static final DeferredItem<DroneItem> DRONE = register("drone",
            () -> new DroneItem(DroneEntity::new, true, DyeColor.WHITE));
    public static final DeferredItem<DroneItem> LOGISTICS_DRONE = register("logistics_drone",
            () -> new DroneItem(LogisticsDroneEntity::new, false, DyeColor.RED));
    public static final DeferredItem<DroneItem> HARVESTING_DRONE = register("harvesting_drone",
            () -> new DroneItem(HarvestingDroneEntity::new, false, DyeColor.GREEN));
    public static final DeferredItem<DroneItem> GUARD_DRONE = register("guard_drone",
            () -> new DroneItem(GuardDroneEntity::new, false, DyeColor.BLUE));
    public static final DeferredItem<DroneItem> COLLECTOR_DRONE = register("collector_drone",
            () -> new DroneItem(CollectorDroneEntity::new, false, DyeColor.YELLOW));

    public static final DeferredItem<LogisticsFrameRequesterItem> LOGISTICS_FRAME_REQUESTER = register("logistics_frame_requester",
            LogisticsFrameRequesterItem::new);
    public static final DeferredItem<LogisticsFrameStorageItem> LOGISTICS_FRAME_STORAGE = register("logistics_frame_storage",
            LogisticsFrameStorageItem::new);
    public static final DeferredItem<LogisticsFrameDefaultStorageItem> LOGISTICS_FRAME_DEFAULT_STORAGE = register("logistics_frame_default_storage",
            LogisticsFrameDefaultStorageItem::new);
    public static final DeferredItem<LogisticsFramePassiveProviderItem> LOGISTICS_FRAME_PASSIVE_PROVIDER = register("logistics_frame_passive_provider",
            LogisticsFramePassiveProviderItem::new);
    public static final DeferredItem<LogisticsFrameActiveProviderItem> LOGISTICS_FRAME_ACTIVE_PROVIDER = register("logistics_frame_active_provider",
            LogisticsFrameActiveProviderItem::new);

    public static final DeferredItem<SemiblockItem> HEAT_FRAME = register("heat_frame",
            SemiblockItem::new);
    public static final DeferredItem<SemiblockItem> SPAWNER_AGITATOR = register("spawner_agitator",
            SpawnerAgitatorItem::new);
    public static final DeferredItem<SemiblockItem> CROP_SUPPORT = register("crop_support",
            SemiblockItem::new);
    public static final DeferredItem<SemiblockItem> TRANSFER_GADGET = register("transfer_gadget",
            SemiblockItem::new);

    public static final DeferredItem<StandardGunAmmoItem> GUN_AMMO = register("gun_ammo",
            StandardGunAmmoItem::new);
    public static final DeferredItem<IncendiaryGunAmmoItem> GUN_AMMO_INCENDIARY = register("gun_ammo_incendiary",
            IncendiaryGunAmmoItem::new);
    public static final DeferredItem<WeightedGunAmmoItem> GUN_AMMO_WEIGHTED = register("gun_ammo_weighted",
            WeightedGunAmmoItem::new);
    public static final DeferredItem<ArmorPiercingGunAmmoItem> GUN_AMMO_AP = register("gun_ammo_ap",
            ArmorPiercingGunAmmoItem::new);
    public static final DeferredItem<ExplosiveGunAmmoItem> GUN_AMMO_EXPLOSIVE = register("gun_ammo_explosive",
            ExplosiveGunAmmoItem::new);
    public static final DeferredItem<FreezingGunAmmoItem> GUN_AMMO_FREEZING = register("gun_ammo_freezing",
            FreezingGunAmmoItem::new);

    public static final DeferredItem<TubeModuleItem> SAFETY_TUBE_MODULE = register("safety_tube_module",
            () -> new TubeModuleItem(SafetyValveModule::new));
    public static final DeferredItem<TubeModuleItem> PRESSURE_GAUGE_MODULE = register("pressure_gauge_module",
            () -> new TubeModuleItem(PressureGaugeModule::new));
    public static final DeferredItem<TubeModuleItem> FLOW_DETECTOR_MODULE = register("flow_detector_module",
            () -> new TubeModuleItem(FlowDetectorModule::new));
    public static final DeferredItem<TubeModuleItem> AIR_GRATE_MODULE = register("air_grate_module",
            () -> new TubeModuleItem(AirGrateModule::new));
    public static final DeferredItem<TubeModuleItem> REGULATOR_TUBE_MODULE = register("regulator_tube_module",
            () -> new TubeModuleItem(RegulatorModule::new));
    public static final DeferredItem<TubeModuleItem> CHARGING_MODULE = register("charging_module",
            () -> new TubeModuleItem(ChargingModule::new));
    public static final DeferredItem<TubeModuleItem> LOGISTICS_MODULE = register("logistics_module",
            () -> new TubeModuleItem(LogisticsModule::new));
    public static final DeferredItem<TubeModuleItem> REDSTONE_MODULE = register("redstone_module",
            () -> new TubeModuleItem(RedstoneModule::new));
    public static final DeferredItem<TubeModuleItem> VACUUM_MODULE = register("vacuum_module",
            () -> new TubeModuleItem(VacuumModule::new));
    public static final DeferredItem<TubeModuleItem> THERMOSTAT_MODULE = register("thermostat_module",
            () -> new TubeModuleItem(ThermostatModule::new));

    public static final DeferredItem<BucketItem> OIL_BUCKET = registerBucket("oil_bucket",
            ModFluids.OIL);
    public static final DeferredItem<BucketItem> ETCHING_ACID_BUCKET = registerBucket("etching_acid_bucket",
            ModFluids.ETCHING_ACID);
    public static final DeferredItem<BucketItem> PLASTIC_BUCKET = register("plastic_bucket",
            FluidPlastic.Bucket::new);
    public static final DeferredItem<BucketItem> DIESEL_BUCKET = registerBucket("diesel_bucket",
            ModFluids.DIESEL);
    public static final DeferredItem<BucketItem> KEROSENE_BUCKET = registerBucket("kerosene_bucket",
            ModFluids.KEROSENE);
    public static final DeferredItem<BucketItem> GASOLINE_BUCKET = registerBucket("gasoline_bucket",
            ModFluids.GASOLINE);
    public static final DeferredItem<BucketItem> LPG_BUCKET = registerBucket("lpg_bucket",
            ModFluids.LPG);
    public static final DeferredItem<BucketItem> LUBRICANT_BUCKET = registerBucket("lubricant_bucket",
            ModFluids.LUBRICANT);
    public static final DeferredItem<BucketItem> MEMORY_ESSENCE_BUCKET = registerBucket("memory_essence_bucket",
            ModFluids.MEMORY_ESSENCE);
    public static final DeferredItem<BucketItem> YEAST_CULTURE_BUCKET = registerBucket("yeast_culture_bucket",
            ModFluids.YEAST_CULTURE);
    public static final DeferredItem<BucketItem> ETHANOL_BUCKET = registerBucket("ethanol_bucket",
            ModFluids.ETHANOL);
    public static final DeferredItem<BucketItem> VEGETABLE_OIL_BUCKET = registerBucket("vegetable_oil_bucket",
            ModFluids.VEGETABLE_OIL);
    public static final DeferredItem<BucketItem> BIODIESEL_BUCKET = registerBucket("biodiesel_bucket",
            ModFluids.BIODIESEL);

    static {
        for (BuiltinUpgrade bu : BuiltinUpgrade.values()) {
            registerUpgrade(bu);
        }
    }

    /* -----------------------*/

    public static Item.Properties defaultProps() {
        return new Item.Properties();
    }

    public static Item.Properties toolProps() {
        return defaultProps().stacksTo(1);
    }

    public static Item.Properties pressurizableProps() {
        return defaultProps().component(ModDataComponents.AIR, 0);
    }

    public static Item.Properties pressurizableToolProps() {
        return toolProps().component(ModDataComponents.AIR, 0);
    }

    public static Item.Properties filledBucketProps() {
        return defaultProps().stacksTo(1).craftRemainder(Items.BUCKET);
    }

    private static <T extends Item> DeferredItem<T> register(final String name, final Supplier<T> sup) {
        return ITEMS.register(name, sup);
    }

    private static DeferredItem<NetworkComponentItem> register(final NetworkComponentType type) {
        return register(type.getRegistryName(), () -> new NetworkComponentItem(type));
    }

    private static DeferredItem<AssemblyProgramItem> register(final AssemblyProgramType type) {
        return register(type.getRegistryName(), () -> new AssemblyProgramItem(type));
    }

    private static DeferredItem<DrillBitItem> register(final DrillBitType type) {
        return register(type.getRegistryName(), () -> new DrillBitItem(type));
    }

    private static DeferredItem<Item> register(final String name) {
        return register(name, () -> new Item(ModItems.defaultProps()));
    }

    private static DeferredItem<BucketItem> registerBucket(String name, Supplier<? extends Fluid> sup) {
        return register(name, () -> new PneumaticCraftBucketItem(sup.get()));
    }

    private static DeferredItem<Item> registerFood(final String name, FoodProperties food) {
        return register(name, () -> new Item(defaultProps().food(food)));
    }

    private static void registerUpgrade(BuiltinUpgrade builtin) {
        PNCUpgrade pncUpgrade = builtin.registerUpgrade();
        IntStream.rangeClosed(1, builtin.getMaxTier()).forEach(tier -> {
            register(pncUpgrade.getItemRegistryName(tier).getPath(), () -> new UpgradeItem(pncUpgrade, tier, builtin.getRarity()));
        });
    }
}
