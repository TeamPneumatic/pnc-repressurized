package pneumaticCraft.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.oredict.OreDictionary;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.semiblock.SemiBlockActiveProvider;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.registry.GameRegistry;

public class Itemss{

    public static Item GPSTool;
    public static Item machineUpgrade;
    public static Item ingotIronCompressed;
    public static Item pressureGauge;
    public static Item stoneBase;
    public static Item cannonBarrel;
    public static Item turbineBlade;
    public static Item plasticPlant;
    public static Item plastic;
    public static Item airCanister;
    public static Item vortexCannon;
    public static Item pneumaticCylinder;
    public static Item pneumaticHelmet;
    public static Item manometer;
    public static Item turbineRotor;
    public static Item assemblyProgram;
    public static Item emptyPCB;
    public static Item unassembledPCB;
    public static Item PCBBlueprint;
    public static Item transistor;
    public static Item capacitor;
    public static Item printedCircuitBoard;
    public static Item failedPCB;
    public static Item networkComponent;
    public static Item stopWorm;
    public static Item nukeVirus;
    public static Item compressedIronGear;
    public static Item pneumaticWrench;
    public static Item drone;
    public static Item programmingPuzzle;
    public static Item advancedPCB;
    public static Item remote;
    public static Item seismicSensor;
    public static Item logisticsConfigurator;
    public static Item logisticsFrameRequester;
    public static Item logisticsFrameStorage;
    public static Item logisticsFrameDefaultStorage;
    public static Item logisticsFramePassiveProvider;
    public static Item logisticsFrameActiveProvider;
    public static Item logisticsDrone;
    public static Item gunAmmo;
    public static Item amadronTablet;
    public static Item minigun;

    public static void init(){
        GPSTool = new ItemGPSTool().setUnlocalizedName("gpsTool");
        machineUpgrade = new ItemMachineUpgrade().setUnlocalizedName("machineUpgrade");
        ingotIronCompressed = new ItemPneumatic(Textures.ITEM_COMPRESSED_IRON_INGOT).setUnlocalizedName("ingotIronCompressed");
        pressureGauge = new ItemPneumatic(Textures.ITEM_PRESSURE_GAUGE).setUnlocalizedName("pressureGauge");
        stoneBase = new ItemPneumatic().setUnlocalizedName("stoneBase");
        cannonBarrel = new ItemPneumatic().setUnlocalizedName("cannonBarrel");
        turbineBlade = new ItemPneumatic(Textures.ITEM_TURBINE_BLADE).setUnlocalizedName("turbineBlade");
        plasticPlant = new ItemPlasticPlants().setUnlocalizedName("plasticPlant");
        plastic = new ItemPlastic().setUnlocalizedName("plastic");
        airCanister = new ItemPressurizable(Textures.ITEM_AIR_CANISTER, PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME).setUnlocalizedName("airCanister");
        vortexCannon = new ItemVortexCannon(Textures.ITEM_VORTEX).setUnlocalizedName("vortexCannon");
        pneumaticCylinder = new ItemPneumatic(Textures.ITEM_CANNON_BARREL).setUnlocalizedName("pneumaticCilinder");
        pneumaticHelmet = new ItemPneumaticArmor(Textures.ITEM_PNEUMATIC_HELMET, ItemArmor.ArmorMaterial.IRON, PneumaticCraft.proxy.getArmorRenderID(Textures.ARMOR_PNEUMATIC), 0, PneumaticValues.PNEUMATIC_HELMET_VOLUME, PneumaticValues.PNEUMATIC_HELMET_MAX_AIR).setUnlocalizedName("pneumaticHelmet");
        manometer = new pneumaticCraft.common.item.ItemManometer(Textures.ITEM_MANOMETER).setUnlocalizedName("manometer");
        turbineRotor = new pneumaticCraft.common.item.ItemPneumatic(Textures.ITEM_TURBINE_ROTOR).setUnlocalizedName("turbineRotor");
        assemblyProgram = new ItemAssemblyProgram().setUnlocalizedName("assemblyProgram");
        emptyPCB = new ItemEmptyPCB().setUnlocalizedName("emptyPCB");
        unassembledPCB = new ItemNonDespawning(Textures.ITEM_UNASSEMBLED_PCB).setUnlocalizedName("unassembledPCB");
        PCBBlueprint = new ItemPneumatic(Textures.ITEM_PCB_BLUEPRINT).setUnlocalizedName("pcbBlueprint");
        transistor = new ItemPneumatic(Textures.ITEM_TRANSISTOR).setUnlocalizedName("transistor");
        capacitor = new ItemPneumatic(Textures.ITEM_CAPACITOR).setUnlocalizedName("capacitor");
        printedCircuitBoard = new ItemPneumatic(Textures.ITEM_PRINTED_CIRCUIT_BOARD).setUnlocalizedName("printedCircuitBoard");
        failedPCB = new ItemNonDespawning(Textures.ITEM_FAILED_PCB).setUnlocalizedName("failedPCB");
        networkComponent = new ItemNetworkComponents().setUnlocalizedName("networkComponent");
        stopWorm = new ItemPneumatic(Textures.ITEM_STOP_WORM).setUnlocalizedName("stopWorm");
        nukeVirus = new ItemPneumatic(Textures.ITEM_NUKE_VIRUS).setUnlocalizedName("nukeVirus");
        compressedIronGear = new ItemPneumatic(Textures.ITEM_COMPRESSED_IRON_GEAR).setUnlocalizedName("compressedIronGear");
        pneumaticWrench = new ItemPneumaticWrench(Textures.ITEM_PNEUMATIC_WRENCH, PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME).setUnlocalizedName("pneumaticWrench");
        drone = new ItemDrone().setUnlocalizedName("drone");
        programmingPuzzle = new ItemProgrammingPuzzle().setUnlocalizedName("programmingPuzzle")/*.setTextureName("pneumaticcraft:programmingPuzzles/entityAttack")*/;
        advancedPCB = new ItemPneumatic(Textures.ITEM_ADVANCED_PCB).setUnlocalizedName("advancedPCB");
        remote = new ItemRemote("remote").setUnlocalizedName("remote");
        seismicSensor = new ItemSeismicSensor();
        logisticsConfigurator = new ItemLogisticsConfigurator("logisticsConfigurator", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME).setUnlocalizedName("logisticsConfigurator");
        logisticsFrameRequester = new ItemLogisticsFrameRequester().setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        logisticsFrameStorage = new ItemLogisticsFrameStorage().setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        logisticsFrameDefaultStorage = new ItemLogisticsFrameDefaultStorage().setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        logisticsFramePassiveProvider = new ItemLogisticsFramePassiveProvider().setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        logisticsFrameActiveProvider = new ItemLogisticsFrame(SemiBlockActiveProvider.ID).setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        logisticsDrone = new ItemLogisticsDrone().setUnlocalizedName("logisticDrone");
        gunAmmo = new ItemGunAmmo().setUnlocalizedName("gunAmmo");
        amadronTablet = new ItemAmadronTablet("amadronTablet", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME).setUnlocalizedName("amadronTablet");
        minigun = new ItemMinigun(PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME).setUnlocalizedName("minigun");

        registerItems();

        OreDictionary.registerOre(Names.INGOT_IRON_COMPRESSED, ingotIronCompressed);
    }

    private static void registerItems(){
        registerItem(GPSTool);
        registerItem(machineUpgrade);
        registerItem(ingotIronCompressed);
        registerItem(pressureGauge);
        registerItem(stoneBase);
        registerItem(cannonBarrel);
        registerItem(turbineBlade);
        registerItem(plasticPlant);
        registerItem(plastic);
        registerItem(airCanister);
        registerItem(vortexCannon);
        registerItem(pneumaticCylinder);
        registerItem(pneumaticHelmet);
        registerItem(manometer);
        registerItem(turbineRotor);
        registerItem(assemblyProgram);
        registerItem(emptyPCB);
        registerItem(unassembledPCB);
        registerItem(PCBBlueprint);
        registerItem(transistor);
        registerItem(capacitor);
        registerItem(printedCircuitBoard);
        registerItem(failedPCB);
        registerItem(networkComponent);
        registerItem(stopWorm);
        registerItem(nukeVirus);
        registerItem(compressedIronGear);
        registerItem(pneumaticWrench);
        registerItem(drone);
        registerItem(programmingPuzzle);
        registerItem(advancedPCB);
        registerItem(remote);
        registerItem(seismicSensor);
        registerItem(logisticsConfigurator);
        registerItem(logisticsFrameRequester);
        registerItem(logisticsFrameDefaultStorage);
        registerItem(logisticsFrameStorage);
        registerItem(logisticsFramePassiveProvider);
        registerItem(logisticsFrameActiveProvider);
        registerItem(logisticsDrone);
        registerItem(gunAmmo);
        registerItem(amadronTablet);
        registerItem(minigun);
    }

    public static void registerItem(Item item){
        registerItem(item, item.getUnlocalizedName().substring("item.".length()));
    }

    public static void registerItem(Item item, String registerName){
        GameRegistry.registerItem(item, registerName, Names.MOD_ID);
        ThirdPartyManager.instance().onItemRegistry(item);
        //GameData.newItemAdded(item);
    }
}
