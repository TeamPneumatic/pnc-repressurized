package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.semiblock.*;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Block;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
@ObjectHolder(Names.MOD_ID)
public class Itemss {
    @ObjectHolder("gps_tool")
    public static final Item GPS_TOOL = null;
    @ObjectHolder("ingot_iron_compressed")
    public static final Item INGOT_IRON_COMPRESSED = null;
    @ObjectHolder("pressure_gauge")
    public static final Item PRESSURE_GAUGE = null;
    @ObjectHolder("stone_base")
    public static final Item STONE_BASE = null;
    @ObjectHolder("cannon_barrel")
    public static final Item cannonBarrel = null;
    @ObjectHolder("turbine_blade")
    public static final Item TURBINE_BLADE = null;
    @ObjectHolder("plastic")
    public static final Item PLASTIC = null;
    @ObjectHolder("air_canister")
    public static final Item AIR_CANISTER = null;
    @ObjectHolder("vortex_cannon")
    public static final Item VORTEX_CANNON = null;
    @ObjectHolder("pneumatic_cylinder")
    public static final Item PNEUMATIC_CYLINDER = null;
    @ObjectHolder("pneumatic_helmet")
    public static final Item PNEUMATIC_HELMET = null;
    @ObjectHolder("manometer")
    public static final Item MANOMETER = null;
    @ObjectHolder("turbine_rotor")
    public static final Item TURBINE_ROTOR = null;
    @ObjectHolder("assembly_program")
    public static final Item ASSEMBLY_PROGRAM = null;
    @ObjectHolder("empty_pcb")
    public static final Item EMPTY_PCB = null;
    @ObjectHolder("unassembled_pcb")
    public static final Item UNASSEMBLED_PCB = null;
    @ObjectHolder("pcb_blueprint")
    public static final Item PCB_BLUEPRINT = null;
    @ObjectHolder("transistor")
    public static final Item TRANSISTOR = null;
    @ObjectHolder("capacitor")
    public static final Item CAPACITOR = null;
    @ObjectHolder("printed_circuit_board")
    public static final Item PRINTED_CIRCUIT_BOARD = null;
    @ObjectHolder("failed_pcb")
    public static final Item FAILED_PCB = null;
    @ObjectHolder("network_component")
    public static final Item NETWORK_COMPONENT = null;
    @ObjectHolder("stop_worm")
    public static final Item STOP_WORM = null;
    @ObjectHolder("nuke_virus")
    public static final Item NUKE_VIRUS = null;
    @ObjectHolder("compressed_iron_gear")
    public static final Item COMPRESSED_IRON_GEAR = null;
    @ObjectHolder("pneumatic_wrench")
    public static final Item PNEUMATIC_WRENCH = null;
    @ObjectHolder("drone")
    public static final Item DRONE = null;
    @ObjectHolder("programming_puzzle")
    public static final Item PROGRAMMING_PUZZLE = null;
    @ObjectHolder("advanced_pcb")
    public static final Item ADVANCED_PCB = null;
    @ObjectHolder("remote")
    public static final Item REMOTE = null;
    @ObjectHolder("seismic_sensor")
    public static final Item SEISMIC_SENSOR = null;
    @ObjectHolder("logistics_configurator")
    public static final Item LOGISTICS_CONFIGURATOR = null;
    @ObjectHolder(SemiBlockRequester.ID)
    public static final Item LOGISTICS_FRAME_REQUESTER = null;
    @ObjectHolder(SemiBlockStorage.ID)
    public static final Item LOGISTICS_FRAME_STORAGE = null;
    @ObjectHolder(SemiBlockDefaultStorage.ID)
    public static final Item LOGISTICS_FRAME_DEFAULT_STORAGE = null;
    @ObjectHolder(SemiBlockPassiveProvider.ID)
    public static final Item LOGISTICS_FRAME_PASSIVE_PROVIDER = null;
    @ObjectHolder(SemiBlockActiveProvider.ID)
    public static final Item LOGISTICS_FRAME_ACTIVE_PROVIDER = null;
    @ObjectHolder(SemiBlockHeatFrame.ID)
    public static final Item HEAT_FRAME = null;
    @ObjectHolder("logistic_drone")
    public static final Item LOGISTICS_DRONE = null;
    @ObjectHolder("gun_ammo")
    public static final Item GUN_AMMO = null;
    @ObjectHolder("amadron_tablet")
    public static final Item AMADRON_TABLET = null;
    @ObjectHolder("minigun")
    public static final Item MINIGUN = null;

    public static List<Item> items = new ArrayList<>();
    public static List<ItemBlock> all_itemblocks = new ArrayList<>();
    public static Map<EnumUpgrade, Item> upgrades = new HashMap<>();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registerItem(registry, new ItemGPSTool());
        registerItem(registry, new ItemPneumatic("ingot_iron_compressed"));
        registerItem(registry, new ItemPneumatic("pressure_gauge"));
        registerItem(registry, new ItemPneumatic("stone_base"));
        registerItem(registry, new ItemPneumatic("cannon_barrel"));
        registerItem(registry, new ItemPneumatic("turbine_blade"));
        registerItem(registry, new ItemPlastic());
        registerItem(registry, new ItemPressurizable("air_canister", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME));
        registerItem(registry, new ItemVortexCannon());
        registerItem(registry, new ItemPneumatic("pneumatic_cylinder"));
        registerItem(registry, new ItemPneumaticArmor(ItemArmor.ArmorMaterial.IRON, PneumaticCraftRepressurized.proxy.getArmorRenderID(Textures.ARMOR_PNEUMATIC), EntityEquipmentSlot.HEAD, PneumaticValues.PNEUMATIC_HELMET_MAX_AIR));
        registerItem(registry, new ItemManometer());
        registerItem(registry, new ItemPneumatic("turbine_rotor"));
        registerItem(registry, new ItemAssemblyProgram());
        registerItem(registry, new ItemEmptyPCB());
        registerItem(registry, new ItemNonDespawning("unassembled_pcb"));
        registerItem(registry, new ItemPneumatic("pcb_blueprint"));
        registerItem(registry, new ItemPneumatic("transistor"));
        registerItem(registry, new ItemPneumatic("capacitor"));
        registerItem(registry, new ItemPneumatic("printed_circuit_board"));
        registerItem(registry, new ItemNonDespawning("failed_pcb"));
        registerItem(registry, new ItemNetworkComponents());
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
        registerItem(registry, new ItemLogisticsDrone());
        registerItem(registry, new ItemGunAmmo());
        registerItem(registry, new ItemAmadronTablet());
        registerItem(registry, new ItemMinigun());

        registerUpgrades(registry);

        for (Block b : Blockss.blocks) {
            registerItem(registry, new ItemBlock(b).setRegistryName(b.getRegistryName()));
        }
    }

    private static void registerUpgrades(IForgeRegistry<Item> registry) {
        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
            if (upgrade != EnumUpgrade.THAUMCRAFT || Loader.isModLoaded(ModIds.THAUMCRAFT)) {
                String upgradeName = upgrade.toString().toLowerCase() + "_upgrade";
                Item upgradeItem = new ItemMachineUpgrade(upgradeName);
                registerItem(registry, upgradeItem);
                upgrades.put(upgrade, upgradeItem);
            }
        }
    }

    public static void registerItem(IForgeRegistry<Item> registry, Item item) {
        registry.register(item);
        ThirdPartyManager.instance().onItemRegistry(item);
        if (item instanceof ItemBlock) {
            all_itemblocks.add((ItemBlock) item);
        } else {
            items.add(item);
        }
    }
}
