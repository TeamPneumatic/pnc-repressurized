package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.ICustomItemBlock;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.item.*;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent.NetworkComponentType;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.AirBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.List;

@ObjectHolder(Names.MOD_ID)
public class ModItems {
    public static final Item GPS_TOOL = null;
    public static final Item GPS_AREA_TOOL = null;
    public static final Item INGOT_IRON_COMPRESSED = null;
    public static final Item PRESSURE_GAUGE = null;
    public static final Item STONE_BASE = null;
    public static final Item CANNON_BARREL = null;
    public static final Item TURBINE_BLADE = null;
    public static final Item PLASTIC = null;
    public static final Item AIR_CANISTER = null;
    public static final Item REINFORCED_AIR_CANISTER = null;
    public static final Item VORTEX_CANNON = null;
    public static final Item PNEUMATIC_CYLINDER = null;
    public static final Item PNEUMATIC_HELMET = null;
    public static final Item PNEUMATIC_CHESTPLATE = null;
    public static final Item PNEUMATIC_LEGGINGS = null;
    public static final Item PNEUMATIC_BOOTS = null;
    public static final Item MANOMETER = null;
    public static final Item TURBINE_ROTOR = null;
    public static final Item ASSEMBLY_PROGRAM_LASER = null;
    public static final Item ASSEMBLY_PROGRAM_DRILL = null;
    public static final Item ASSEMBLY_PROGRAM_DRILL_LASER = null;
    public static final Item EMPTY_PCB = null;
    public static final Item UNASSEMBLED_PCB = null;
    public static final Item PCB_BLUEPRINT = null;
    public static final Item TRANSISTOR = null;
    public static final Item CAPACITOR = null;
    public static final Item PRINTED_CIRCUIT_BOARD = null;
    public static final Item FAILED_PCB = null;
    public static final Item DIAGNOSTIC_SUBROUTINE = null;
    public static final Item NETWORK_API = null;
    public static final Item NETWORK_DATA_STORAGE = null;
    public static final Item NETWORK_IO_PORT = null;
    public static final Item NETWORK_REGISTRY = null;
    public static final Item NETWORK_NODE = null;
    public static final Item STOP_WORM = null;
    public static final Item NUKE_VIRUS = null;
    public static final Item COMPRESSED_IRON_GEAR = null;
    public static final Item PNEUMATIC_WRENCH = null;
    public static final Item DRONE = null;
    public static final Item PROGRAMMING_PUZZLE = null;
    public static final Item ADVANCED_PCB = null;
    public static final Item REMOTE = null;
    public static final Item SEISMIC_SENSOR = null;
    public static final Item LOGISTICS_CONFIGURATOR = null;
    public static final Item LOGISTICS_FRAME_REQUESTER = null;
    public static final Item LOGISTICS_FRAME_STORAGE = null;
    public static final Item LOGISTICS_FRAME_DEFAULT_STORAGE = null;
    public static final Item LOGISTICS_FRAME_PASSIVE_PROVIDER = null;
    public static final Item LOGISTICS_FRAME_ACTIVE_PROVIDER = null;
    public static final Item HEAT_FRAME = null;
    public static final Item SPAWNER_AGITATOR = null;
    public static final Item CROP_SUPPORT = null;
    public static final Item TRANSFER_GADGET = null;
    public static final Item LOGISTIC_DRONE = null;
    public static final Item HARVESTING_DRONE = null;
    public static final Item GUN_AMMO = null;
    public static final Item GUN_AMMO_INCENDIARY = null;
    public static final Item GUN_AMMO_WEIGHTED = null;
    public static final Item GUN_AMMO_AP = null;
    public static final Item GUN_AMMO_EXPLOSIVE = null;
    public static final Item GUN_AMMO_FREEZING = null;
    public static final Item AMADRON_TABLET = null;
    public static final Item MINIGUN = null;
    public static final Item CAMO_APPLICATOR = null;
    public static final Item MICROMISSILES = null;
    public static final Item OIL_BUCKET = null;
    public static final Item ETCHING_ACID_BUCKET = null;
    public static final Item PLASTIC_BUCKET = null;
    public static final Item DIESEL_BUCKET = null;
    public static final Item KEROSENE_BUCKET = null;
    public static final Item GASOLINE_BUCKET = null;
    public static final Item LPG_BUCKET = null;
    public static final Item LUBRICANT_BUCKET = null;

    public static class Groups {
        public static final ItemGroup PNC_CREATIVE_TAB = new ItemGroup(Names.MOD_ID) {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ModBlocks.AIR_COMPRESSOR);
            }
        };
    }

    public static Item getUpgradeItem(EnumUpgrade upgrade) {
        return Registration.UPGRADES.get(upgrade);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        public static final List<Item> ALL_ITEMS = new ArrayList<>();
        private static List<BlockItem> all_itemblocks = new ArrayList<>();
        public static final UpgradeList UPGRADES = new UpgradeList();

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            IForgeRegistry<Item> registry = event.getRegistry();

            registerItem(registry, new ItemGPSTool());
            registerItem(registry, new ItemGPSAreaTool());
            registerItem(registry, new ItemPneumatic("ingot_iron_compressed"));
            registerItem(registry, new ItemPneumatic("pressure_gauge"));
            registerItem(registry, new ItemPneumatic("stone_base"));
            registerItem(registry, new ItemPneumatic("cannon_barrel"));
            registerItem(registry, new ItemPneumatic("turbine_blade"));
            registerItem(registry, new ItemPneumatic("plastic"));
            registerItem(registry, new ItemPressurizable("air_canister",
                    PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME));
            registerItem(registry, new ItemPressurizable("reinforced_air_canister",
                    PneumaticValues.REINFORCED_AIR_CANISTER_MAX_AIR, PneumaticValues.REINFORCED_AIR_CANISTER_VOLUME));
            registerItem(registry, new ItemVortexCannon());
            registerItem(registry, new ItemPneumatic("pneumatic_cylinder"));
            registerItem(registry, new ItemPneumaticArmor("pneumatic_helmet", EquipmentSlotType.HEAD));
            registerItem(registry, new ItemPneumaticArmor("pneumatic_chestplate", EquipmentSlotType.CHEST));
            registerItem(registry, new ItemPneumaticArmor("pneumatic_leggings", EquipmentSlotType.LEGS));
            registerItem(registry, new ItemPneumaticArmor("pneumatic_boots", EquipmentSlotType.FEET));
            registerItem(registry, new ItemManometer());
            registerItem(registry, new ItemPneumatic("turbine_rotor"));
            for (ItemAssemblyProgram.AssemblyProgramType type : ItemAssemblyProgram.AssemblyProgramType.values()) {
                registerItem(registry, new ItemAssemblyProgram(type));
            }
            registerItem(registry, new ItemEmptyPCB());
            registerItem(registry, new ItemNonDespawning("unassembled_pcb"));
            registerItem(registry, new ItemPneumatic("pcb_blueprint"));
            registerItem(registry, new ItemPneumatic("transistor"));
            registerItem(registry, new ItemPneumatic("capacitor"));
            registerItem(registry, new ItemPneumatic("printed_circuit_board"));
            registerItem(registry, new ItemNonDespawning("failed_pcb"));
            for (NetworkComponentType type : NetworkComponentType.values()) {
                registerItem(registry, new ItemNetworkComponent(type));
            }
            registerItem(registry, new ItemPneumatic("stop_worm"));
            registerItem(registry, new ItemPneumatic("nuke_virus"));
            registerItem(registry, new ItemPneumatic("compressed_iron_gear"));
            registerItem(registry, new ItemPneumaticWrench());
            registerItem(registry, new ItemDrone());
            registerItem(registry, new ItemProgrammingPuzzle());
            registerItem(registry, new ItemPneumatic("advanced_pcb"));
            registerItem(registry, new ItemRemote());
            registerItem(registry, new ItemSeismicSensor());
            registerItem(registry, new ItemLogisticsConfigurator());
            registerItem(registry, new ItemBasicDrone("logistic_drone", EntityLogisticsDrone::new));
            registerItem(registry, new ItemBasicDrone("harvesting_drone", EntityHarvestingDrone::new));
            registerItem(registry, new ItemGunAmmoStandard());
            registerItem(registry, new ItemGunAmmoIncendiary());
            registerItem(registry, new ItemGunAmmoWeighted());
            registerItem(registry, new ItemGunAmmoArmorPiercing());
            registerItem(registry, new ItemGunAmmoExplosive());
            registerItem(registry, new ItemGunAmmoFreezing());
            registerItem(registry, new ItemAmadronTablet());
            registerItem(registry, new ItemMinigun());
            registerItem(registry, new ItemCamoApplicator());
            registerItem(registry, new ItemMicromissiles());

            registerItem(registry, new ItemBucketPneumaticCraft("oil", () -> ModFluids.OIL));
            registerItem(registry, new ItemBucketPneumaticCraft("etching_acid", () -> ModFluids.ETCHING_ACID));
            registerItem(registry, new ItemBucketPneumaticCraft("plastic", () -> ModFluids.PLASTIC));
            registerItem(registry, new ItemBucketPneumaticCraft("diesel", () -> ModFluids.DIESEL));
            registerItem(registry, new ItemBucketPneumaticCraft("kerosene", () -> ModFluids.KEROSENE));
            registerItem(registry, new ItemBucketPneumaticCraft("gasoline", () -> ModFluids.GASOLINE));
            registerItem(registry, new ItemBucketPneumaticCraft("lpg", () -> ModFluids.LPG));
            registerItem(registry, new ItemBucketPneumaticCraft("lubricant", () -> ModFluids.LUBRICANT));

            registerUpgrades(registry);

            ItemPneumaticArmor.initApplicableUpgrades();

            ModBlocks.Registration.ALL_BLOCKS.stream()
                    .filter(b -> !(b instanceof AirBlock || b instanceof FlowingFluidBlock))
                    .forEach(b -> {
                        Item itemBlock = b instanceof ICustomItemBlock ?
                                ((ICustomItemBlock) b).getCustomItemBlock() :
                                new BlockItem(b, new Item.Properties().group(Groups.PNC_CREATIVE_TAB));
                        if (itemBlock != null) {
                            registerItem(registry, itemBlock.setRegistryName(b.getRegistryName()));
                        }
                    });
        }

        private static void registerUpgrades(IForgeRegistry<Item> registry) {
            for (EnumUpgrade upgrade : EnumUpgrade.values()) {
                if (upgrade.isDepLoaded()) {
                    String upgradeName = upgrade.toString().toLowerCase() + "_upgrade";
                    Item upgradeItem = new ItemMachineUpgrade(upgradeName, upgrade);
                    registerItem(registry, upgradeItem);
                    UPGRADES.add(upgradeItem);
                } else {
                    UPGRADES.add(null);
                }
            }
        }

        public static void registerItem(IForgeRegistry<Item> registry, Item item) {
            registry.register(item);
            ThirdPartyManager.instance().onItemRegistry(item);
            if (item instanceof BlockItem) {
                all_itemblocks.add((BlockItem) item);
            } else {
                ALL_ITEMS.add(item);
            }
        }

        public static class UpgradeList extends ArrayList<Item> {
            public Item get(EnumUpgrade upgrade) {
                return get(upgrade.ordinal());
            }
        }
    }
}
