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

package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.api.item.PNCUpgrade;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.core.ModUpgrades.BuiltinUpgrade;
import me.desht.pneumaticcraft.common.entity.drone.*;
import me.desht.pneumaticcraft.common.fluid.FluidPlastic;
import me.desht.pneumaticcraft.common.item.*;
import me.desht.pneumaticcraft.common.item.DrillBitItem.DrillBitType;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem.NetworkComponentType;
import me.desht.pneumaticcraft.common.item.logistics.*;
import me.desht.pneumaticcraft.common.item.minigun.*;
import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.common.tubemodules.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Names.MOD_ID);

    public static final RegistryObject<Item> COMPRESSED_IRON_INGOT = register("ingot_iron_compressed");
    public static final RegistryObject<Item> PRESSURE_GAUGE = register("pressure_gauge");
    public static final RegistryObject<Item> STONE_BASE = register("stone_base");
    public static final RegistryObject<Item> CANNON_BARREL = register("cannon_barrel");
    public static final RegistryObject<Item> TURBINE_BLADE = register("turbine_blade");
    public static final RegistryObject<Item> PLASTIC = register("plastic");
    public static final RegistryObject<Item> PNEUMATIC_CYLINDER = register("pneumatic_cylinder");
    public static final RegistryObject<Item> TURBINE_ROTOR = register("turbine_rotor");
    public static final RegistryObject<Item> PCB_BLUEPRINT = register("pcb_blueprint");
    public static final RegistryObject<Item> TRANSISTOR = register("transistor");
    public static final RegistryObject<Item> CAPACITOR = register("capacitor");
    public static final RegistryObject<Item> PRINTED_CIRCUIT_BOARD = register("printed_circuit_board");
    public static final RegistryObject<Item> SOLAR_WAFER = register("solar_wafer");
    public static final RegistryObject<Item> SOLAR_CELL = register("solar_cell");
    public static final RegistryObject<Item> STOP_WORM = register("stop_worm");
    public static final RegistryObject<Item> NUKE_VIRUS = register("nuke_virus");
    public static final RegistryObject<Item> COMPRESSED_IRON_GEAR = register("compressed_iron_gear");
    public static final RegistryObject<Item> PROGRAMMING_PUZZLE = register("programming_puzzle");
    public static final RegistryObject<Item> MODULE_EXPANSION_CARD = register("module_expansion_card");
    public static final RegistryObject<Item> LOGISTICS_CORE = register("logistics_core");
    public static final RegistryObject<Item> UPGRADE_MATRIX = register("upgrade_matrix");
    public static final RegistryObject<Item> WHEAT_FLOUR = register("wheat_flour");
    public static final RegistryObject<Item> SOURDOUGH = register("sourdough");
    public static final RegistryObject<Item> SPAWNER_CORE_SHELL = register("spawner_core_shell");
    public static final RegistryObject<Item> REINFORCED_CHEST_KIT = register("reinforced_chest_kit", AbstractChestUpgradeKitItem.Reinforced::new);
    public static final RegistryObject<Item> SMART_CHEST_KIT = register("smart_chest_kit", AbstractChestUpgradeKitItem.Smart::new);
    public static final RegistryObject<Item> RAW_SALMON_TEMPURA = register("raw_salmon_tempura");
    public static final RegistryObject<Item> UNASSEMBLED_NETHERITE_DRILL_BIT = register("unassembled_netherite_drill_bit");

    public static final RegistryObject<Item> SOURDOUGH_BREAD = registerFood("sourdough_bread", ModFoods.SOURDOUGH);
    public static final RegistryObject<Item> CHIPS = registerFood("chips", ModFoods.CHIPS);
    public static final RegistryObject<Item> COD_N_CHIPS = registerFood("cod_n_chips", ModFoods.COD_N_CHIPS);
    public static final RegistryObject<Item> SALMON_TEMPURA = registerFood("salmon_tempura", ModFoods.SALMON_TEMPURA);

    public static final RegistryObject<GPSToolItem> GPS_TOOL = register("gps_tool", GPSToolItem::new);
    public static final RegistryObject<GPSAreaToolItem> GPS_AREA_TOOL = register("gps_area_tool", GPSAreaToolItem::new);
    public static final RegistryObject<RemoteItem> REMOTE = register("remote", RemoteItem::new);
    public static final RegistryObject<SeismicSensorItem> SEISMIC_SENSOR = register("seismic_sensor", SeismicSensorItem::new);
    public static final RegistryObject<MicromissilesItem> MICROMISSILES = register("micromissiles", MicromissilesItem::new);
    public static final RegistryObject<MemoryStickItem> MEMORY_STICK = register("memory_stick", MemoryStickItem::new);
    public static final RegistryObject<TagFilterItem> TAG_FILTER = register("tag_filter", TagFilterItem::new);
    public static final RegistryObject<ClassifyFilterItem> CLASSIFY_FILTER = register("classify_filter", ClassifyFilterItem::new);
    public static final RegistryObject<GlycerolItem> GLYCEROL = register("glycerol", GlycerolItem::new);
    public static final RegistryObject<BandageItem> BANDAGE = register("bandage", BandageItem::new);
    public static final RegistryObject<SpawnerCoreItem> SPAWNER_CORE = register("spawner_core", SpawnerCoreItem::new);

    public static final RegistryObject<PressurizableItem> AIR_CANISTER = register("air_canister",
            AbstractAirCanisterItem.Basic::new);
    public static final RegistryObject<PressurizableItem> REINFORCED_AIR_CANISTER = register("reinforced_air_canister",
            AbstractAirCanisterItem.Reinforced::new);
    public static final RegistryObject<VortexCannonItem> VORTEX_CANNON = register("vortex_cannon",
            VortexCannonItem::new);
    public static final RegistryObject<PneumaticWrenchItem> PNEUMATIC_WRENCH = register("pneumatic_wrench",
            PneumaticWrenchItem::new);
    public static final RegistryObject<ManometerItem> MANOMETER = register("manometer",
            ManometerItem::new);
    public static final RegistryObject<LogisticsConfiguratorItem> LOGISTICS_CONFIGURATOR = register("logistics_configurator",
            LogisticsConfiguratorItem::new);
    public static final RegistryObject<AmadronTabletItem> AMADRON_TABLET = register("amadron_tablet",
            AmadronTabletItem::new);
    public static final RegistryObject<MinigunItem> MINIGUN = register("minigun",
            MinigunItem::new);
    public static final RegistryObject<CamoApplicatorItem> CAMO_APPLICATOR = register("camo_applicator",
            CamoApplicatorItem::new);
    public static final RegistryObject<JackHammerItem> JACKHAMMER = register("jackhammer",
            JackHammerItem::new);

    public static final RegistryObject<Item> COMPRESSED_IRON_HELMET = register("compressed_iron_helmet",
            () -> new CompressedIronArmorItem(EquipmentSlot.HEAD));
    public static final RegistryObject<Item> COMPRESSED_IRON_CHESTPLATE = register("compressed_iron_chestplate",
            () -> new CompressedIronArmorItem(EquipmentSlot.CHEST));
    public static final RegistryObject<Item> COMPRESSED_IRON_LEGGINGS = register("compressed_iron_leggings",
            () -> new CompressedIronArmorItem(EquipmentSlot.LEGS));
    public static final RegistryObject<Item> COMPRESSED_IRON_BOOTS = register("compressed_iron_boots",
            () -> new CompressedIronArmorItem(EquipmentSlot.FEET));

    public static final RegistryObject<PneumaticArmorItem> PNEUMATIC_HELMET = register("pneumatic_helmet",
            () -> new PneumaticArmorItem(EquipmentSlot.HEAD));
    public static final RegistryObject<PneumaticArmorItem> PNEUMATIC_CHESTPLATE = register("pneumatic_chestplate",
            () -> new PneumaticArmorItem(EquipmentSlot.CHEST));
    public static final RegistryObject<PneumaticArmorItem> PNEUMATIC_LEGGINGS = register("pneumatic_leggings",
            () -> new PneumaticArmorItem(EquipmentSlot.LEGS));
    public static final RegistryObject<PneumaticArmorItem> PNEUMATIC_BOOTS = register("pneumatic_boots",
            () -> new PneumaticArmorItem(EquipmentSlot.FEET));

    public static final RegistryObject<AssemblyProgramItem> ASSEMBLY_PROGRAM_LASER = register(AssemblyRecipe.AssemblyProgramType.LASER);
    public static final RegistryObject<AssemblyProgramItem> ASSEMBLY_PROGRAM_DRILL = register(AssemblyRecipe.AssemblyProgramType.DRILL);
    public static final RegistryObject<AssemblyProgramItem> ASSEMBLY_PROGRAM_DRILL_LASER = register(AssemblyRecipe.AssemblyProgramType.DRILL_LASER);

    public static final RegistryObject<EmptyPCBItem> EMPTY_PCB = register("empty_pcb",
            EmptyPCBItem::new);
    public static final RegistryObject<NonDespawningItem> UNASSEMBLED_PCB = register("unassembled_pcb",
            NonDespawningItem::new);
    public static final RegistryObject<NonDespawningItem> FAILED_PCB = register("failed_pcb",
            NonDespawningItem::new);

    public static final RegistryObject<NetworkComponentItem> DIAGNOSTIC_SUBROUTINE = register(NetworkComponentType.DIAGNOSTIC_SUBROUTINE);
    public static final RegistryObject<NetworkComponentItem> NETWORK_API = register(NetworkComponentType.NETWORK_API);
    public static final RegistryObject<NetworkComponentItem> NETWORK_DATA_STORAGE = register(NetworkComponentType.NETWORK_DATA_STORAGE);
    public static final RegistryObject<NetworkComponentItem> NETWORK_IO_PORT = register(NetworkComponentType.NETWORK_IO_PORT);
    public static final RegistryObject<NetworkComponentItem> NETWORK_REGISTRY = register(NetworkComponentType.NETWORK_REGISTRY);
    public static final RegistryObject<NetworkComponentItem> NETWORK_NODE = register(NetworkComponentType.NETWORK_NODE);

    public static final RegistryObject<DrillBitItem> IRON_DRILL_BIT = register(DrillBitType.IRON);
    public static final RegistryObject<DrillBitItem> COMPRESSED_IRON_DRILL_BIT = register(DrillBitType.COMPRESSED_IRON);
    public static final RegistryObject<DrillBitItem> DIAMOND_DRILL_BIT = register(DrillBitType.DIAMOND);
    public static final RegistryObject<DrillBitItem> NETHERITE_DRILL_BIT = register(DrillBitType.NETHERITE);

    public static final RegistryObject<DroneItem> DRONE = register("drone",
            () -> new DroneItem(DroneEntity::new, true, DyeColor.WHITE));
    public static final RegistryObject<DroneItem> LOGISTICS_DRONE = register("logistics_drone",
            () -> new DroneItem(LogisticsDroneEntity::new, false, DyeColor.RED));
    public static final RegistryObject<DroneItem> HARVESTING_DRONE = register("harvesting_drone",
            () -> new DroneItem(HarvestingDroneEntity::new, false, DyeColor.GREEN));
    public static final RegistryObject<DroneItem> GUARD_DRONE = register("guard_drone",
            () -> new DroneItem(GuardDroneEntity::new, false, DyeColor.BLUE));
    public static final RegistryObject<DroneItem> COLLECTOR_DRONE = register("collector_drone",
            () -> new DroneItem(CollectorDroneEntity::new, false, DyeColor.YELLOW));

    public static final RegistryObject<LogisticsFrameRequesterItem> LOGISTICS_FRAME_REQUESTER = register("logistics_frame_requester",
            LogisticsFrameRequesterItem::new);
    public static final RegistryObject<LogisticsFrameStorageItem> LOGISTICS_FRAME_STORAGE = register("logistics_frame_storage",
            LogisticsFrameStorageItem::new);
    public static final RegistryObject<LogisticsFrameDefaultStorageItem> LOGISTICS_FRAME_DEFAULT_STORAGE = register("logistics_frame_default_storage",
            LogisticsFrameDefaultStorageItem::new);
    public static final RegistryObject<LogisticsFramePassiveProviderItem> LOGISTICS_FRAME_PASSIVE_PROVIDER = register("logistics_frame_passive_provider",
            LogisticsFramePassiveProviderItem::new);
    public static final RegistryObject<LogisticsFrameActiveProviderItem> LOGISTICS_FRAME_ACTIVE_PROVIDER = register("logistics_frame_active_provider",
            LogisticsFrameActiveProviderItem::new);

    public static final RegistryObject<SemiblockItem> HEAT_FRAME = register("heat_frame",
            SemiblockItem::new);
    public static final RegistryObject<SemiblockItem> SPAWNER_AGITATOR = register("spawner_agitator",
            SpawnerAgitatorItem::new);
    public static final RegistryObject<SemiblockItem> CROP_SUPPORT = register("crop_support",
            SemiblockItem::new);
    public static final RegistryObject<SemiblockItem> TRANSFER_GADGET = register("transfer_gadget",
            SemiblockItem::new);

    public static final RegistryObject<StandardGunAmmoItem> GUN_AMMO = register("gun_ammo",
            StandardGunAmmoItem::new);
    public static final RegistryObject<IncendiaryGunAmmoItem> GUN_AMMO_INCENDIARY = register("gun_ammo_incendiary",
            IncendiaryGunAmmoItem::new);
    public static final RegistryObject<WeightedGunAmmoItem> GUN_AMMO_WEIGHTED = register("gun_ammo_weighted",
            WeightedGunAmmoItem::new);
    public static final RegistryObject<ArmorPiercingGunAmmoItem> GUN_AMMO_AP = register("gun_ammo_ap",
            ArmorPiercingGunAmmoItem::new);
    public static final RegistryObject<ExplosiveGunAmmoItem> GUN_AMMO_EXPLOSIVE = register("gun_ammo_explosive",
            ExplosiveGunAmmoItem::new);
    public static final RegistryObject<FreezingGunAmmoItem> GUN_AMMO_FREEZING = register("gun_ammo_freezing",
            FreezingGunAmmoItem::new);

    public static final RegistryObject<TubeModuleItem> SAFETY_TUBE_MODULE = register("safety_tube_module",
            () -> new TubeModuleItem(SafetyValveModule::new));
    public static final RegistryObject<TubeModuleItem> PRESSURE_GAUGE_MODULE = register("pressure_gauge_module",
            () -> new TubeModuleItem(PressureGaugeModule::new));
    public static final RegistryObject<TubeModuleItem> FLOW_DETECTOR_MODULE = register("flow_detector_module",
            () -> new TubeModuleItem(FlowDetectorModule::new));
    public static final RegistryObject<TubeModuleItem> AIR_GRATE_MODULE = register("air_grate_module",
            () -> new TubeModuleItem(AirGrateModule::new));
    public static final RegistryObject<TubeModuleItem> REGULATOR_TUBE_MODULE = register("regulator_tube_module",
            () -> new TubeModuleItem(RegulatorModule::new));
    public static final RegistryObject<TubeModuleItem> CHARGING_MODULE = register("charging_module",
            () -> new TubeModuleItem(ChargingModule::new));
    public static final RegistryObject<TubeModuleItem> LOGISTICS_MODULE = register("logistics_module",
            () -> new TubeModuleItem(LogisticsModule::new));
    public static final RegistryObject<TubeModuleItem> REDSTONE_MODULE = register("redstone_module",
            () -> new TubeModuleItem(RedstoneModule::new));
    public static final RegistryObject<TubeModuleItem> VACUUM_MODULE = register("vacuum_module",
            () -> new TubeModuleItem(VacuumModule::new));

    public static final RegistryObject<BucketItem> OIL_BUCKET = registerBucket("oil_bucket",
            ModFluids.OIL);
    public static final RegistryObject<BucketItem> ETCHING_ACID_BUCKET = registerBucket("etching_acid_bucket",
            ModFluids.ETCHING_ACID);
    public static final RegistryObject<BucketItem> PLASTIC_BUCKET = register("plastic_bucket",
            FluidPlastic.Bucket::new);
    public static final RegistryObject<BucketItem> DIESEL_BUCKET = registerBucket("diesel_bucket",
            ModFluids.DIESEL);
    public static final RegistryObject<BucketItem> KEROSENE_BUCKET = registerBucket("kerosene_bucket",
            ModFluids.KEROSENE);
    public static final RegistryObject<BucketItem> GASOLINE_BUCKET = registerBucket("gasoline_bucket",
            ModFluids.GASOLINE);
    public static final RegistryObject<BucketItem> LPG_BUCKET = registerBucket("lpg_bucket",
            ModFluids.LPG);
    public static final RegistryObject<BucketItem> LUBRICANT_BUCKET = registerBucket("lubricant_bucket",
            ModFluids.LUBRICANT);
    public static final RegistryObject<BucketItem> MEMORY_ESSENCE_BUCKET = registerBucket("memory_essence_bucket",
            ModFluids.MEMORY_ESSENCE);
    public static final RegistryObject<BucketItem> YEAST_CULTURE_BUCKET = registerBucket("yeast_culture_bucket",
            ModFluids.YEAST_CULTURE);
    public static final RegistryObject<BucketItem> ETHANOL_BUCKET = registerBucket("ethanol_bucket",
            ModFluids.ETHANOL);
    public static final RegistryObject<BucketItem> VEGETABLE_OIL_BUCKET = registerBucket("vegetable_oil_bucket",
            ModFluids.VEGETABLE_OIL);
    public static final RegistryObject<BucketItem> BIODIESEL_BUCKET = registerBucket("biodiesel_bucket",
            ModFluids.BIODIESEL);

    static {
        // no values assigned; items can be retrieved via PNCUpgrade methods
        registerUpgrade(ModUpgrades.VOLUME, BuiltinUpgrade.VOLUME);
        registerUpgrade(ModUpgrades.DISPENSER, BuiltinUpgrade.DISPENSER);
        registerUpgrade(ModUpgrades.ITEM_LIFE, BuiltinUpgrade.ITEM_LIFE);
        registerUpgrade(ModUpgrades.ENTITY_TRACKER, BuiltinUpgrade.ENTITY_TRACKER);
        registerUpgrade(ModUpgrades.BLOCK_TRACKER, BuiltinUpgrade.BLOCK_TRACKER);
        registerUpgrade(ModUpgrades.SPEED, BuiltinUpgrade.SPEED);
        registerUpgrade(ModUpgrades.SEARCH, BuiltinUpgrade.SEARCH);
        registerUpgrade(ModUpgrades.COORDINATE_TRACKER, BuiltinUpgrade.COORDINATE_TRACKER);
        registerUpgrade(ModUpgrades.RANGE, BuiltinUpgrade.RANGE);
        registerUpgrade(ModUpgrades.SECURITY, BuiltinUpgrade.SECURITY);
        registerUpgrade(ModUpgrades.MAGNET, BuiltinUpgrade.MAGNET);
        registerUpgrade(ModUpgrades.THAUMCRAFT, BuiltinUpgrade.THAUMCRAFT);
        registerUpgrade(ModUpgrades.CHARGING, BuiltinUpgrade.CHARGING);
        registerUpgrade(ModUpgrades.ARMOR, BuiltinUpgrade.ARMOR);
        registerUpgrade(ModUpgrades.JET_BOOTS, BuiltinUpgrade.JET_BOOTS);
        registerUpgrade(ModUpgrades.NIGHT_VISION, BuiltinUpgrade.NIGHT_VISION);
        registerUpgrade(ModUpgrades.SCUBA, BuiltinUpgrade.SCUBA);
        registerUpgrade(ModUpgrades.CREATIVE, BuiltinUpgrade.CREATIVE);
        registerUpgrade(ModUpgrades.AIR_CONDITIONING, BuiltinUpgrade.AIR_CONDITIONING);
        registerUpgrade(ModUpgrades.INVENTORY, BuiltinUpgrade.INVENTORY);
        registerUpgrade(ModUpgrades.JUMPING, BuiltinUpgrade.JUMPING);
        registerUpgrade(ModUpgrades.FLIPPERS, BuiltinUpgrade.FLIPPERS);
        registerUpgrade(ModUpgrades.STANDBY, BuiltinUpgrade.STANDBY);
        registerUpgrade(ModUpgrades.MINIGUN, BuiltinUpgrade.MINIGUN);
        registerUpgrade(ModUpgrades.RADIATION_SHIELDING, BuiltinUpgrade.RADIATION_SHIELDING);
        registerUpgrade(ModUpgrades.GILDED, BuiltinUpgrade.GILDED);
        registerUpgrade(ModUpgrades.ENDER_VISOR, BuiltinUpgrade.ENDER_VISOR);
        registerUpgrade(ModUpgrades.STOMP, BuiltinUpgrade.STOMP);
        registerUpgrade(ModUpgrades.ELYTRA, BuiltinUpgrade.ELYTRA);
    }

    /* -----------------------*/

    public static Item.Properties defaultProps() {
        return new Item.Properties().tab(ItemGroups.PNC_CREATIVE_TAB);
    }

    public static Item.Properties toolProps() {
        return defaultProps().stacksTo(1);
    }

    public static Item.Properties filledBucketProps() {
        return defaultProps().stacksTo(1).craftRemainder(Items.BUCKET);
    }

    private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return ITEMS.register(name, sup);
    }

    private static RegistryObject<NetworkComponentItem> register(final NetworkComponentType type) {
        return register(type.getRegistryName(), () -> new NetworkComponentItem(type));
    }

    private static RegistryObject<AssemblyProgramItem> register(final AssemblyProgramType type) {
        return register(type.getRegistryName(), () -> new AssemblyProgramItem(type));
    }

    private static RegistryObject<DrillBitItem> register(final DrillBitType type) {
        return register(type.getRegistryName(), () -> new DrillBitItem(type));
    }

    private static RegistryObject<Item> register(final String name) {
        return register(name, () -> new Item(ModItems.defaultProps()));
    }

    private static RegistryObject<BucketItem> registerBucket(String name, Supplier<? extends Fluid> sup) {
        return register(name, () -> new PneumaticCraftBucketItem(sup));
    }

    private static RegistryObject<Item> registerFood(final String name, FoodProperties food) {
        return register(name, () -> new Item(defaultProps().food(food)));
    }

    private static void registerUpgrade(RegistryObject<PNCUpgrade> upgrade, BuiltinUpgrade upgradeDetails) {
        IntStream.range(1, upgradeDetails.getMaxTier() + 1).forEach(tier -> {
                    String baseName = upgradeDetails.getName() + "_upgrade";
                    String itemName = upgradeDetails.getMaxTier() > 1 ? baseName + "_" + tier : baseName;
                    register(itemName, () -> new UpgradeItem(upgrade, tier));
                }
        );
    }

    static class ItemGroups {
        static final CreativeModeTab PNC_CREATIVE_TAB = new CreativeModeTab(Names.MOD_ID) {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(ModBlocks.AIR_COMPRESSOR.get());
            }
        };
    }
}
