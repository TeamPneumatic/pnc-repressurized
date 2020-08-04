package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.tubes.*;
import me.desht.pneumaticcraft.common.entity.living.*;
import me.desht.pneumaticcraft.common.item.*;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent.NetworkComponentType;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlock;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

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
    public static final RegistryObject<Item> STOP_WORM = register("stop_worm");
    public static final RegistryObject<Item> NUKE_VIRUS = register("nuke_virus");
    public static final RegistryObject<Item> COMPRESSED_IRON_GEAR = register("compressed_iron_gear");
    public static final RegistryObject<Item> PROGRAMMING_PUZZLE = register("programming_puzzle");
    public static final RegistryObject<Item> ADVANCED_PCB = register("advanced_pcb");
    public static final RegistryObject<Item> LOGISTICS_CORE = register("logistics_core");
    public static final RegistryObject<Item> UPGRADE_MATRIX = register("upgrade_matrix");
    public static final RegistryObject<Item> WHEAT_FLOUR = register("wheat_flour");
    public static final RegistryObject<Item> SOURDOUGH = register("sourdough");
    public static final RegistryObject<Item> SOURDOUGH_BREAD = register("sourdough_bread",
            () -> new Item(defaultProps().food(ModFoods.SOURDOUGH)));

    public static final RegistryObject<ItemGPSTool> GPS_TOOL = register("gps_tool", ItemGPSTool::new);
    public static final RegistryObject<ItemGPSAreaTool> GPS_AREA_TOOL = register("gps_area_tool", ItemGPSAreaTool::new);
    public static final RegistryObject<ItemRemote> REMOTE = register("remote", ItemRemote::new);
    public static final RegistryObject<ItemSeismicSensor> SEISMIC_SENSOR = register("seismic_sensor", ItemSeismicSensor::new);
    public static final RegistryObject<ItemMicromissiles> MICROMISSILES = register("micromissiles", ItemMicromissiles::new);
    public static final RegistryObject<ItemMemoryStick> MEMORY_STICK = register("memory_stick", ItemMemoryStick::new);
    public static final RegistryObject<ItemTagFilter> TAG_FILTER = register("tag_filter", ItemTagFilter::new);
    public static final RegistryObject<ItemGlycerol> GLYCEROL = register("glycerol", ItemGlycerol::new);
    public static final RegistryObject<ItemBandage> BANDAGE = register("bandage", ItemBandage::new);

    public static final RegistryObject<ItemPressurizable> AIR_CANISTER = register("air_canister",
            () -> new ItemPressurizable(PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME));
    public static final RegistryObject<ItemPressurizable> REINFORCED_AIR_CANISTER = register("reinforced_air_canister",
            () -> new ItemPressurizable(PneumaticValues.REINFORCED_AIR_CANISTER_MAX_AIR, PneumaticValues.REINFORCED_AIR_CANISTER_VOLUME));
    public static final RegistryObject<ItemVortexCannon> VORTEX_CANNON = register("vortex_cannon",
            ItemVortexCannon::new);
    public static final RegistryObject<ItemPneumaticWrench> PNEUMATIC_WRENCH = register("pneumatic_wrench",
            ItemPneumaticWrench::new);
    public static final RegistryObject<ItemManometer> MANOMETER = register("manometer",
            ItemManometer::new);
    public static final RegistryObject<ItemLogisticsConfigurator> LOGISTICS_CONFIGURATOR = register("logistics_configurator",
            ItemLogisticsConfigurator::new);
    public static final RegistryObject<ItemAmadronTablet> AMADRON_TABLET = register("amadron_tablet",
            ItemAmadronTablet::new);
    public static final RegistryObject<ItemMinigun> MINIGUN = register("minigun",
            ItemMinigun::new);
    public static final RegistryObject<ItemCamoApplicator> CAMO_APPLICATOR = register("camo_applicator",
            ItemCamoApplicator::new);

    public static final RegistryObject<ItemPneumaticArmor> PNEUMATIC_HELMET = register("pneumatic_helmet",
            () -> new ItemPneumaticArmor(EquipmentSlotType.HEAD));
    public static final RegistryObject<ItemPneumaticArmor> PNEUMATIC_CHESTPLATE = register("pneumatic_chestplate",
            () -> new ItemPneumaticArmor(EquipmentSlotType.CHEST));
    public static final RegistryObject<ItemPneumaticArmor> PNEUMATIC_LEGGINGS = register("pneumatic_leggings",
            () -> new ItemPneumaticArmor(EquipmentSlotType.LEGS));
    public static final RegistryObject<ItemPneumaticArmor> PNEUMATIC_BOOTS = register("pneumatic_boots",
            () -> new ItemPneumaticArmor(EquipmentSlotType.FEET));

    public static final RegistryObject<ItemAssemblyProgram> ASSEMBLY_PROGRAM_LASER = register(AssemblyRecipe.AssemblyProgramType.LASER);
    public static final RegistryObject<ItemAssemblyProgram> ASSEMBLY_PROGRAM_DRILL = register(AssemblyRecipe.AssemblyProgramType.DRILL);
    public static final RegistryObject<ItemAssemblyProgram> ASSEMBLY_PROGRAM_DRILL_LASER = register(AssemblyRecipe.AssemblyProgramType.DRILL_LASER);

    public static final RegistryObject<ItemEmptyPCB> EMPTY_PCB = register("empty_pcb",
            ItemEmptyPCB::new);
    public static final RegistryObject<ItemNonDespawning> UNASSEMBLED_PCB = register("unassembled_pcb",
            ItemNonDespawning::new);
    public static final RegistryObject<ItemNonDespawning> FAILED_PCB = register("failed_pcb",
            ItemNonDespawning::new);

    public static final RegistryObject<ItemNetworkComponent> DIAGNOSTIC_SUBROUTINE = register(NetworkComponentType.DIAGNOSTIC_SUBROUTINE);
    public static final RegistryObject<ItemNetworkComponent> NETWORK_API = register(NetworkComponentType.NETWORK_API);
    public static final RegistryObject<ItemNetworkComponent> NETWORK_DATA_STORAGE = register(NetworkComponentType.NETWORK_DATA_STORAGE);
    public static final RegistryObject<ItemNetworkComponent> NETWORK_IO_PORT = register(NetworkComponentType.NETWORK_IO_PORT);
    public static final RegistryObject<ItemNetworkComponent> NETWORK_REGISTRY = register(NetworkComponentType.NETWORK_REGISTRY);
    public static final RegistryObject<ItemNetworkComponent> NETWORK_NODE = register(NetworkComponentType.NETWORK_NODE);

    public static final RegistryObject<ItemDrone> DRONE = register("drone",
            () -> new ItemDrone(EntityDrone::new, true));
    public static final RegistryObject<ItemDrone> LOGISTICS_DRONE = register("logistics_drone",
            () -> new ItemDrone(EntityLogisticsDrone::new, false));
    public static final RegistryObject<ItemDrone> HARVESTING_DRONE = register("harvesting_drone",
            () -> new ItemDrone(EntityHarvestingDrone::new, false));
    public static final RegistryObject<ItemDrone> GUARD_DRONE = register("guard_drone",
            () -> new ItemDrone(EntityGuardDrone::new, false));
    public static final RegistryObject<ItemDrone> COLLECTOR_DRONE = register("collector_drone",
            () -> new ItemDrone(EntityCollectorDrone::new, false));

    public static final RegistryObject<ItemLogisticsFrameRequester> LOGISTICS_FRAME_REQUESTER = register("logistics_frame_requester",
            ItemLogisticsFrameRequester::new);
    public static final RegistryObject<ItemLogisticsFrameStorage> LOGISTICS_FRAME_STORAGE = register("logistics_frame_storage",
            ItemLogisticsFrameStorage::new);
    public static final RegistryObject<ItemLogisticsFrameDefaultStorage> LOGISTICS_FRAME_DEFAULT_STORAGE = register("logistics_frame_default_storage",
            ItemLogisticsFrameDefaultStorage::new);
    public static final RegistryObject<ItemLogisticsFramePassiveProvider> LOGISTICS_FRAME_PASSIVE_PROVIDER = register("logistics_frame_passive_provider",
            ItemLogisticsFramePassiveProvider::new);
    public static final RegistryObject<ItemLogisticsFrameActiveProvider> LOGISTICS_FRAME_ACTIVE_PROVIDER = register("logistics_frame_active_provider",
            ItemLogisticsFrameActiveProvider::new);

    public static final RegistryObject<ItemSemiBlock> HEAT_FRAME = register("heat_frame",
            ItemSemiBlock::new);
    public static final RegistryObject<ItemSemiBlock> SPAWNER_AGITATOR = register("spawner_agitator",
            ItemSemiBlock::new);
    public static final RegistryObject<ItemSemiBlock> CROP_SUPPORT = register("crop_support",
            ItemSemiBlock::new);
    public static final RegistryObject<ItemSemiBlock> TRANSFER_GADGET = register("transfer_gadget",
            ItemSemiBlock::new);

    public static final RegistryObject<ItemGunAmmoStandard> GUN_AMMO = register("gun_ammo",
            ItemGunAmmoStandard::new);
    public static final RegistryObject<ItemGunAmmoIncendiary> GUN_AMMO_INCENDIARY = register("gun_ammo_incendiary",
            ItemGunAmmoIncendiary::new);
    public static final RegistryObject<ItemGunAmmoWeighted> GUN_AMMO_WEIGHTED = register("gun_ammo_weighted",
            ItemGunAmmoWeighted::new);
    public static final RegistryObject<ItemGunAmmoArmorPiercing> GUN_AMMO_AP = register("gun_ammo_ap",
            ItemGunAmmoArmorPiercing::new);
    public static final RegistryObject<ItemGunAmmoExplosive> GUN_AMMO_EXPLOSIVE = register("gun_ammo_explosive",
            ItemGunAmmoExplosive::new);
    public static final RegistryObject<ItemGunAmmoFreezing> GUN_AMMO_FREEZING = register("gun_ammo_freezing",
            ItemGunAmmoFreezing::new);

    public static final RegistryObject<ItemTubeModule> SAFETY_TUBE_MODULE = register("safety_tube_module",
            () -> new ItemTubeModule(ModuleSafetyValve::new));
    public static final RegistryObject<ItemTubeModule> PRESSURE_GAUGE_MODULE = register("pressure_gauge_module",
            () -> new ItemTubeModule(ModulePressureGauge::new));
    public static final RegistryObject<ItemTubeModule> FLOW_DETECTOR_MODULE = register("flow_detector_module",
            () -> new ItemTubeModule(ModuleFlowDetector::new));
    public static final RegistryObject<ItemTubeModule> AIR_GRATE_MODULE = register("air_grate_module",
            () -> new ItemTubeModule(ModuleAirGrate::new));
    public static final RegistryObject<ItemTubeModule> REGULATOR_TUBE_MODULE = register("regulator_tube_module",
            () -> new ItemTubeModule(ModuleRegulatorTube::new));
    public static final RegistryObject<ItemTubeModule> CHARGING_MODULE = register("charging_module",
            () -> new ItemTubeModule(ModuleCharging::new));
    public static final RegistryObject<ItemTubeModule> LOGISTICS_MODULE = register("logistics_module",
            () -> new ItemTubeModule(ModuleLogistics::new));
    public static final RegistryObject<ItemTubeModule> REDSTONE_MODULE = register("redstone_module",
            () -> new ItemTubeModule(ModuleRedstone::new));

    public static final RegistryObject<ItemBucketPneumaticCraft> OIL_BUCKET = registerBucket("oil_bucket",
            ModFluids.OIL);
    public static final RegistryObject<ItemBucketPneumaticCraft> ETCHING_ACID_BUCKET = registerBucket("etching_acid_bucket",
            ModFluids.ETCHING_ACID);
    public static final RegistryObject<ItemBucketPneumaticCraft> PLASTIC_BUCKET = registerBucket("plastic_bucket",
            ModFluids.PLASTIC);
    public static final RegistryObject<ItemBucketPneumaticCraft> DIESEL_BUCKET = registerBucket("diesel_bucket",
            ModFluids.DIESEL);
    public static final RegistryObject<ItemBucketPneumaticCraft> KEROSENE_BUCKET = registerBucket("kerosene_bucket",
            ModFluids.KEROSENE);
    public static final RegistryObject<ItemBucketPneumaticCraft> GASOLINE_BUCKET = registerBucket("gasoline_bucket",
            ModFluids.GASOLINE);
    public static final RegistryObject<ItemBucketPneumaticCraft> LPG_BUCKET = registerBucket("lpg_bucket",
            ModFluids.LPG);
    public static final RegistryObject<ItemBucketPneumaticCraft> LUBRICANT_BUCKET = registerBucket("lubricant_bucket",
            ModFluids.LUBRICANT);
    public static final RegistryObject<ItemBucketPneumaticCraft> MEMORY_ESSENCE_BUCKET = registerBucket("memory_essence_bucket",
            ModFluids.MEMORY_ESSENCE);
    public static final RegistryObject<ItemBucketPneumaticCraft> YEAST_CULTURE_BUCKET = registerBucket("yeast_culture_bucket",
            ModFluids.YEAST_CULTURE);
    public static final RegistryObject<ItemBucketPneumaticCraft> ETHANOL_BUCKET = registerBucket("ethanol_bucket",
            ModFluids.ETHANOL);
    public static final RegistryObject<ItemBucketPneumaticCraft> VEGETABLE_OIL_BUCKET = registerBucket("vegetable_oil_bucket",
            ModFluids.VEGETABLE_OIL);
    public static final RegistryObject<ItemBucketPneumaticCraft> BIODIESEL_BUCKET = registerBucket("biodiesel_bucket",
            ModFluids.BIODIESEL);

    static {
        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
            register(upgrade);
        }
    }

   /* -----------------------*/

    public static Item.Properties defaultProps() {
        return new Item.Properties().group(ItemGroups.PNC_CREATIVE_TAB);
    }

    public static Item.Properties bucketProps() {
        return defaultProps().maxStackSize(1).containerItem(Items.BUCKET);
    }

    private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return ITEMS.register(name, sup);
    }

    private static RegistryObject<ItemNetworkComponent> register(final NetworkComponentType type) {
        return register(type.getRegistryName(), () -> new ItemNetworkComponent(type));
    }

    private static RegistryObject<ItemAssemblyProgram> register(final AssemblyProgramType type) {
        return register(type.getRegistryName(), () -> new ItemAssemblyProgram(type));
    }

    private static RegistryObject<Item> register(final String name) {
        return register(name, () -> new Item(ModItems.defaultProps()));
    }

    private static RegistryObject<ItemBucketPneumaticCraft> registerBucket(String name, Supplier<? extends Fluid> sup) {
        return register(name, () -> new ItemBucketPneumaticCraft(sup));
    }

    private static void register(EnumUpgrade upgrade) {
        IntStream.range(1, upgrade.getMaxTier() + 1).forEach(
                tier -> register(upgrade.getItemName(tier), () -> new ItemMachineUpgrade(upgrade, tier))
        );
    }

    static class ItemGroups {
        static final ItemGroup PNC_CREATIVE_TAB = new ItemGroup(Names.MOD_ID) {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ModBlocks.AIR_COMPRESSOR.get());
            }
        };
    }
}
