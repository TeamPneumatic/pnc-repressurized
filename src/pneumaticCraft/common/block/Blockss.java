package pneumaticCraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.oredict.OreDictionary;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.block.pneumaticPlants.BlockBurstPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockChopperPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockCreeperPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockEnderPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockFireFlower;
import pneumaticCraft.common.block.pneumaticPlants.BlockFlyingFlower;
import pneumaticCraft.common.block.pneumaticPlants.BlockHeliumPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockLightningPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockPotionPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockPropulsionPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockRainPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockRepulsionPlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockSlimePlant;
import pneumaticCraft.common.block.pneumaticPlants.BlockSquidPlant;
import pneumaticCraft.common.itemBlock.ItemBlockAdvancedPressureTube;
import pneumaticCraft.common.itemBlock.ItemBlockPressureChamberWall;
import pneumaticCraft.common.itemBlock.ItemBlockPressureTube;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.registry.GameRegistry;

public class Blockss{

    public static Block pressureTube;
    public static Block airCompressor;
    public static Block airCannon;
    public static Block pressureChamberWall;
    public static Block pressureChamberValve;
    public static Block pressureChamberInterface;
    public static Block squidPlant;
    public static Block fireFlower;
    public static Block creeperPlant;
    public static Block slimePlant;
    public static Block rainPlant;
    public static Block enderPlant;
    public static Block lightningPlant;
    public static Block adrenalinePlant;
    public static Block burstPlant;
    public static Block potionPlant;
    public static Block repulsionPlant;
    public static Block heliumPlant;
    public static Block flyingFlower;
    public static Block musicPlant;
    public static Block propulsionPlant;
    public static Block chopperPlant;
    public static Block chargingStation;
    public static Block elevatorBase;
    public static Block elevatorFrame;
    public static Block vacuumPump;
    public static Block pneumaticDoorBase;
    public static Block pneumaticDoor;
    public static Block assemblyPlatform;
    public static Block assemblyIOUnit;
    public static Block assemblyDrill;
    public static Block assemblyLaser;
    public static Block assemblyController;
    public static Block advancedPressureTube;
    public static Block compressedIron;
    public static Block uvLightBox;
    public static Block etchingAcid;
    public static Block securityStation;
    public static Block universalSensor;
    public static Block aerialInterface;
    public static Block electrostaticCompressor;
    public static Block aphorismTile;
    public static Block omnidirectionalHopper;
    public static Block elevatorCaller;
    public static Block programmer;
    public static Block creativeCompressor;
    public static Block plasticMixer;
    public static Block liquidCompressor;
    public static Block advancedLiquidCompressor;
    public static Block advancedAirCompressor;
    public static Block liquidHopper;
    public static Block droneRedstoneEmitter;

    public static void init(){
        pressureTube = new BlockPressureTube(Material.iron, PneumaticValues.DANGER_PRESSURE_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_PRESSURE_TUBE, PneumaticValues.VOLUME_PRESSURE_TUBE).setHardness(3.0F).setResistance(3.0F).setBlockName("pressureTube");
        advancedPressureTube = new BlockPressureTube(Material.iron, PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.VOLUME_ADVANCED_PRESSURE_TUBE).setHardness(3.0F).setResistance(3.0F).setBlockName("advancedPressureTube");
        airCompressor = new BlockAirCompressor(Material.iron).setHardness(3.0F).setResistance(3.0F).setBlockName("airCompressor");
        advancedAirCompressor = new BlockAdvancedAirCompressor(Material.iron).setHardness(3.0F).setResistance(3.0F).setBlockName("advancedAirCompressor");
        airCannon = new BlockAirCannon(Material.iron).setHardness(3.0F).setResistance(3.0F).setBlockName("airCannon");
        pressureChamberWall = new BlockPressureChamberWall(Material.iron).setHardness(10.0F).setResistance(2000.0F).setBlockName("pressureChamberWall");
        pressureChamberValve = new BlockPressureChamberValve(Material.iron).setHardness(10.0F).setResistance(2000.0F).setBlockName("pressureChamberValve");
        slimePlant = new BlockSlimePlant().setBlockName("slimePlant");
        creeperPlant = new BlockCreeperPlant().setBlockName("creeperPlant");
        squidPlant = new BlockSquidPlant().setBlockName("squidPlant");
        fireFlower = new BlockFireFlower().setBlockName("fireFlower");
        rainPlant = new BlockRainPlant().setBlockName("rainPlant");
        enderPlant = new BlockEnderPlant().setBlockName("enderPlant");
        lightningPlant = new BlockLightningPlant().setBlockName("lightningPlant");
        burstPlant = new BlockBurstPlant().setBlockName("burstPlant");
        potionPlant = new BlockPotionPlant().setBlockName("potionPlant");
        heliumPlant = new BlockHeliumPlant().setBlockName("heliumPlant");
        propulsionPlant = new BlockPropulsionPlant().setBlockName("propulsionPlant");
        repulsionPlant = new BlockRepulsionPlant().setBlockName("repulsionPlant");
        flyingFlower = new BlockFlyingFlower().setBlockName("flyingFlower");
        chopperPlant = new BlockChopperPlant().setBlockName("chopperPlant");
        chargingStation = new BlockChargingStation(Material.iron).setBlockName("chargingStation").setHardness(3.0F).setResistance(3.0F);
        elevatorBase = new BlockElevatorBase(Material.iron).setBlockName("elevatorBase").setHardness(3.0F).setResistance(3.0F);
        elevatorFrame = new BlockElevatorFrame(Material.iron).setBlockName("elevatorFrame").setHardness(3.0F).setResistance(3.0F);
        pressureChamberInterface = new BlockPressureChamberInterface(Material.iron).setHardness(10.0F).setResistance(2000.0F).setBlockName("pressureChamberInterface");
        vacuumPump = new BlockVacuumPump(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("vacuumPump");
        pneumaticDoorBase = new BlockPneumaticDoorBase(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("pneumaticDoorBase");
        pneumaticDoor = new BlockPneumaticDoor(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("pneumaticDoor");
        assemblyIOUnit = new BlockAssemblyIOUnit(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("assemblyIOUnit");
        assemblyPlatform = new BlockAssemblyPlatform(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("assemblyPlatform");
        assemblyDrill = new BlockAssemblyDrill(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("assemblyDrill");
        assemblyLaser = new BlockAssemblyLaser(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("assemblyLaser");
        assemblyController = new BlockAssemblyController(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("assemblyController");
        compressedIron = new HelperBlock(Material.iron).setBlockTextureName(Textures.BLOCK_COMPRESSED_IRON).setStepSound(Block.soundTypeMetal).setHardness(3.0F).setResistance(10.0F).setBlockName("compressedIronBlock");
        uvLightBox = new BlockUVLightBox(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("uvLightBox");
        etchingAcid = new BlockFluidEtchingAcid().setBlockName("etchingAcid");
        securityStation = new BlockSecurityStation(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("securityStation");
        universalSensor = new BlockUniversalSensor(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("universalSensor");
        aerialInterface = new BlockAerialInterface(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("aerialInterface");
        electrostaticCompressor = new BlockElectrostaticCompressor(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("electrostaticCompressor");
        aphorismTile = new BlockAphorismTile(Material.rock).setHardness(1.5F).setResistance(4.0F).setBlockName("aphorismTile");
        omnidirectionalHopper = new BlockOmnidirectionalHopper(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("omnidirectionalHopper");
        liquidHopper = new BlockLiquidHopper(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("liquidHopper");
        elevatorCaller = new BlockElevatorCaller(Material.iron).setHardness(3.0F).setResistance(10F).setBlockName("elevatorCaller");
        programmer = new BlockProgrammer(Material.iron).setHardness(3.0F).setResistance(10F).setBlockName("programmer");
        creativeCompressor = new BlockCreativeCompressor(Material.iron).setHardness(3.0F).setResistance(10F).setBlockName("creativeCompressor");
        plasticMixer = new BlockPlasticMixer(Material.iron).setHardness(3.0F).setResistance(10F).setBlockName("plasticMixer");
        liquidCompressor = new BlockLiquidCompressor(Material.iron).setHardness(3.0F).setResistance(10F).setBlockName("liquidCompressor");
        advancedLiquidCompressor = new BlockAdvancedLiquidCompressor(Material.iron).setHardness(3.0F).setResistance(10F).setBlockName("advancedLiquidCompressor");
        droneRedstoneEmitter = new BlockDroneRedstoneEmitter().setBlockName("droneRedstoneEmitter");

        registerBlocks();

        OreDictionary.registerOre(Names.BLOCK_IRON_COMPRESSED, compressedIron);
    }

    private static class HelperBlock extends Block{//helper class, as the constructor of Block is protected.
        public HelperBlock(Material material){
            super(material);
            setCreativeTab(PneumaticCraft.tabPneumaticCraft);
        }
    }

    private static void registerBlocks(){
        registerBlock(pressureTube, ItemBlockPressureTube.class);
        registerBlock(airCompressor);
        registerBlock(advancedAirCompressor);
        registerBlock(airCannon);
        registerBlock(pressureChamberWall, ItemBlockPressureChamberWall.class);//TODO legacy remove item block.
        registerBlock(pressureChamberValve);
        registerBlock(slimePlant);
        registerBlock(creeperPlant);
        registerBlock(squidPlant);
        registerBlock(fireFlower);
        registerBlock(rainPlant);
        registerBlock(enderPlant);
        registerBlock(lightningPlant);
        registerBlock(burstPlant);
        registerBlock(potionPlant);
        registerBlock(heliumPlant);
        registerBlock(propulsionPlant);
        registerBlock(repulsionPlant);
        registerBlock(flyingFlower);
        registerBlock(chopperPlant);
        registerBlock(chargingStation);
        registerBlock(elevatorBase);
        registerBlock(elevatorFrame);
        registerBlock(pressureChamberInterface);
        registerBlock(vacuumPump);
        registerBlock(pneumaticDoorBase);
        registerBlock(pneumaticDoor);
        registerBlock(assemblyIOUnit);
        registerBlock(assemblyPlatform);
        registerBlock(assemblyDrill);
        registerBlock(assemblyLaser);
        registerBlock(assemblyController);
        registerBlock(advancedPressureTube, ItemBlockAdvancedPressureTube.class);//TODO legacy remove item block.
        registerBlock(compressedIron);
        registerBlock(uvLightBox);
        registerBlock(etchingAcid);
        registerBlock(securityStation);
        registerBlock(universalSensor);
        registerBlock(aerialInterface);
        registerBlock(electrostaticCompressor);
        registerBlock(aphorismTile);
        registerBlock(omnidirectionalHopper);
        registerBlock(liquidHopper);
        registerBlock(elevatorCaller);
        registerBlock(programmer);
        registerBlock(creativeCompressor);
        registerBlock(plasticMixer);
        registerBlock(liquidCompressor);
        registerBlock(advancedLiquidCompressor);
        registerBlock(droneRedstoneEmitter);
    }

    public static void registerBlock(Block block){
        registerBlock(block, ItemBlock.class);
    }

    private static void registerBlock(Block block, Class<? extends ItemBlock> itemBlockClass){
        GameRegistry.registerBlock(block, itemBlockClass, block.getUnlocalizedName().substring("tile.".length()));
        ThirdPartyManager.instance().onBlockRegistry(block);
    }
}
