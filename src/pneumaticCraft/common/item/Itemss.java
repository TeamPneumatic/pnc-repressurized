package pneumaticCraft.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBucket;
import net.minecraftforge.oredict.OreDictionary;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.block.Blockss;
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
    public static Item bucketEtchingAcid;
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

    public static void init(CreativeTabs tabPneumaticCraft){
        GPSTool = new ItemGPSTool().setCreativeTab(tabPneumaticCraft).setUnlocalizedName("gpsTool");
        machineUpgrade = new ItemMachineUpgrade().setCreativeTab(tabPneumaticCraft).setUnlocalizedName("machineUpgrade");
        ingotIronCompressed = new ItemPneumatic(Textures.ITEM_COMPRESSED_IRON_INGOT).setUnlocalizedName("ingotIronCompressed").setCreativeTab(tabPneumaticCraft);
        pressureGauge = new ItemPneumatic(Textures.ITEM_PRESSURE_GAUGE).setUnlocalizedName("pressureGauge").setCreativeTab(tabPneumaticCraft);
        stoneBase = new ItemPneumatic().setUnlocalizedName("stoneBase").setCreativeTab(tabPneumaticCraft);
        cannonBarrel = new ItemPneumatic().setUnlocalizedName("cannonBarrel").setCreativeTab(tabPneumaticCraft);
        turbineBlade = new ItemPneumatic(Textures.ITEM_TURBINE_BLADE).setUnlocalizedName("turbineBlade").setCreativeTab(tabPneumaticCraft);
        plasticPlant = new ItemPlasticPlants().setUnlocalizedName("plasticPlant").setCreativeTab(tabPneumaticCraft);
        plastic = new ItemPlastic().setUnlocalizedName("plastic").setCreativeTab(tabPneumaticCraft);
        airCanister = new ItemPressurizable(Textures.ITEM_AIR_CANISTER, PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME).setUnlocalizedName("airCanister").setCreativeTab(tabPneumaticCraft);
        vortexCannon = new ItemVortexCannon(Textures.ITEM_VORTEX).setUnlocalizedName("vortexCannon").setCreativeTab(tabPneumaticCraft);
        pneumaticCylinder = new ItemPneumatic(Textures.ITEM_CANNON_BARREL).setUnlocalizedName("pneumaticCilinder").setCreativeTab(tabPneumaticCraft);
        pneumaticHelmet = new ItemPneumaticArmor(Textures.ITEM_PNEUMATIC_HELMET, ItemArmor.ArmorMaterial.IRON, PneumaticCraft.proxy.getArmorRenderID(Textures.ARMOR_PNEUMATIC), 0, PneumaticValues.PNEUMATIC_HELMET_VOLUME, PneumaticValues.PNEUMATIC_HELMET_MAX_AIR).setUnlocalizedName("pneumaticHelmet").setCreativeTab(tabPneumaticCraft);
        manometer = new pneumaticCraft.common.item.ItemManometer(Textures.ITEM_MANOMETER).setUnlocalizedName("manometer").setCreativeTab(tabPneumaticCraft);
        turbineRotor = new pneumaticCraft.common.item.ItemPneumatic(Textures.ITEM_TURBINE_ROTOR).setUnlocalizedName("turbineRotor").setCreativeTab(tabPneumaticCraft);
        assemblyProgram = new ItemAssemblyProgram().setUnlocalizedName("assemblyProgram").setCreativeTab(tabPneumaticCraft);
        emptyPCB = new ItemEmptyPCB().setUnlocalizedName("emptyPCB").setCreativeTab(tabPneumaticCraft);
        unassembledPCB = new ItemPneumatic(Textures.ITEM_UNASSEMBLED_PCB).setUnlocalizedName("unassembledPCB").setCreativeTab(tabPneumaticCraft);
        PCBBlueprint = new ItemPneumatic(Textures.ITEM_PCB_BLUEPRINT).setUnlocalizedName("pcbBlueprint").setCreativeTab(tabPneumaticCraft);
        bucketEtchingAcid = new ItemBucket(Blockss.etchingAcid).setTextureName(Textures.ICON_LOCATION + Textures.ITEM_BUCKET_ETCHING_ACID).setUnlocalizedName("etchingAcidBucket").setCreativeTab(tabPneumaticCraft);
        transistor = new ItemPneumatic(Textures.ITEM_TRANSISTOR).setUnlocalizedName("transistor").setCreativeTab(tabPneumaticCraft);
        capacitor = new ItemPneumatic(Textures.ITEM_CAPACITOR).setUnlocalizedName("capacitor").setCreativeTab(tabPneumaticCraft);
        printedCircuitBoard = new ItemPneumatic(Textures.ITEM_PRINTED_CIRCUIT_BOARD).setUnlocalizedName("printedCircuitBoard").setCreativeTab(tabPneumaticCraft);
        failedPCB = new ItemPneumatic(Textures.ITEM_FAILED_PCB).setUnlocalizedName("failedPCB").setCreativeTab(tabPneumaticCraft);
        networkComponent = new ItemNetworkComponents().setUnlocalizedName("networkComponent").setCreativeTab(tabPneumaticCraft);
        stopWorm = new ItemPneumatic(Textures.ITEM_STOP_WORM).setUnlocalizedName("stopWorm").setCreativeTab(tabPneumaticCraft);
        nukeVirus = new ItemPneumatic(Textures.ITEM_NUKE_VIRUS).setUnlocalizedName("nukeVirus").setCreativeTab(tabPneumaticCraft);
        compressedIronGear = new ItemPneumatic(Textures.ITEM_COMPRESSED_IRON_GEAR).setUnlocalizedName("compressedIronGear").setCreativeTab(tabPneumaticCraft);
        pneumaticWrench = new ItemPneumaticWrench(Textures.ITEM_PNEUMATIC_WRENCH, PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME).setUnlocalizedName("pneumaticWrench").setCreativeTab(tabPneumaticCraft);
        drone = new ItemDrone().setUnlocalizedName("drone").setCreativeTab(tabPneumaticCraft);
        programmingPuzzle = new ItemProgrammingPuzzle().setUnlocalizedName("programmingPuzzle").setCreativeTab(tabPneumaticCraft)/*.setTextureName("pneumaticcraft:programmingPuzzles/entityAttack")*/;
        advancedPCB = new ItemPneumatic(Textures.ITEM_ADVANCED_PCB).setUnlocalizedName("advancedPCB").setCreativeTab(tabPneumaticCraft);

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
        registerItem(bucketEtchingAcid);
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
