package pneumaticCraft.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import pneumaticCraft.client.gui.GuiAdvancedAirCompressor;
import pneumaticCraft.client.gui.GuiAdvancedLiquidCompressor;
import pneumaticCraft.client.gui.GuiAerialInterface;
import pneumaticCraft.client.gui.GuiAirCannon;
import pneumaticCraft.client.gui.GuiAirCompressor;
import pneumaticCraft.client.gui.GuiAphorismTile;
import pneumaticCraft.client.gui.GuiAssemblyController;
import pneumaticCraft.client.gui.GuiChargingStation;
import pneumaticCraft.client.gui.GuiDrone;
import pneumaticCraft.client.gui.GuiElectrostaticCompressor;
import pneumaticCraft.client.gui.GuiElevator;
import pneumaticCraft.client.gui.GuiGasLift;
import pneumaticCraft.client.gui.GuiLiquidCompressor;
import pneumaticCraft.client.gui.GuiLiquidHopper;
import pneumaticCraft.client.gui.GuiOmnidirectionalHopper;
import pneumaticCraft.client.gui.GuiPlasticMixer;
import pneumaticCraft.client.gui.GuiPneumaticDoor;
import pneumaticCraft.client.gui.GuiPneumaticHelmet;
import pneumaticCraft.client.gui.GuiPressureChamber;
import pneumaticCraft.client.gui.GuiPressureChamberInterface;
import pneumaticCraft.client.gui.GuiProgrammableController;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.GuiRefinery;
import pneumaticCraft.client.gui.GuiRemote;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.GuiSecurityStationHacking;
import pneumaticCraft.client.gui.GuiSecurityStationInventory;
import pneumaticCraft.client.gui.GuiThermopneumaticProcessingPlant;
import pneumaticCraft.client.gui.GuiUVLightBox;
import pneumaticCraft.client.gui.GuiUniversalSensor;
import pneumaticCraft.client.gui.GuiVacuumPump;
import pneumaticCraft.client.gui.semiblock.GuiLogisticsProvider;
import pneumaticCraft.client.gui.semiblock.GuiLogisticsRequester;
import pneumaticCraft.client.gui.semiblock.GuiLogisticsStorage;
import pneumaticCraft.client.gui.tubemodule.GuiAirGrateModule;
import pneumaticCraft.client.gui.tubemodule.GuiPressureModule;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.HackTickHandler;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.inventory.ContainerAdvancedAirCompressor;
import pneumaticCraft.common.inventory.ContainerAdvancedLiquidCompressor;
import pneumaticCraft.common.inventory.ContainerAirCannon;
import pneumaticCraft.common.inventory.ContainerAirCompressor;
import pneumaticCraft.common.inventory.ContainerAssemblyController;
import pneumaticCraft.common.inventory.ContainerChargingStation;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.inventory.ContainerElevator;
import pneumaticCraft.common.inventory.ContainerGasLift;
import pneumaticCraft.common.inventory.ContainerLiquidCompressor;
import pneumaticCraft.common.inventory.ContainerLiquidHopper;
import pneumaticCraft.common.inventory.ContainerLogistics;
import pneumaticCraft.common.inventory.ContainerOmnidirectionalHopper;
import pneumaticCraft.common.inventory.ContainerPlasticMixer;
import pneumaticCraft.common.inventory.ContainerPneumaticDoor;
import pneumaticCraft.common.inventory.ContainerPressureChamber;
import pneumaticCraft.common.inventory.ContainerPressureChamberInterface;
import pneumaticCraft.common.inventory.ContainerProgrammableController;
import pneumaticCraft.common.inventory.ContainerProgrammer;
import pneumaticCraft.common.inventory.ContainerRefinery;
import pneumaticCraft.common.inventory.ContainerRemote;
import pneumaticCraft.common.inventory.ContainerSecurityStationHacking;
import pneumaticCraft.common.inventory.ContainerSecurityStationInventory;
import pneumaticCraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import pneumaticCraft.common.inventory.ContainerUVLightBox;
import pneumaticCraft.common.inventory.ContainerUniversalSensor;
import pneumaticCraft.common.inventory.ContainerVacuumPump;
import pneumaticCraft.common.semiblock.ItemSemiBlockBase;
import pneumaticCraft.common.semiblock.SemiBlockActiveProvider;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.common.semiblock.SemiBlockRequester;
import pneumaticCraft.common.semiblock.SemiBlockStorage;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.common.tileentity.TileEntityAdvancedAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import pneumaticCraft.common.tileentity.TileEntityAerialInterface;
import pneumaticCraft.common.tileentity.TileEntityAirCannon;
import pneumaticCraft.common.tileentity.TileEntityAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityAphorismTile;
import pneumaticCraft.common.tileentity.TileEntityAssemblyController;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.common.tileentity.TileEntityElectrostaticCompressor;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.common.tileentity.TileEntityGasLift;
import pneumaticCraft.common.tileentity.TileEntityLiquidCompressor;
import pneumaticCraft.common.tileentity.TileEntityLiquidHopper;
import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberInterface;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberValve;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.common.tileentity.TileEntityRefinery;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import pneumaticCraft.common.tileentity.TileEntityUVLightBox;
import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import pneumaticCraft.common.tileentity.TileEntityVacuumPump;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler{
    protected CommonHUDHandler clientHudHandler;
    private CommonHUDHandler serverHudHandler;

    public int SPECIAL_RENDER_TYPE_VALUE;

    public int PneumaticHelmetRenderID = 0;
    public static final int GUI_ID_AIR_COMPRESSOR = 0;
    public static final int GUI_ID_AIR_CANNON = 1;
    public static final int GUI_ID_PRESSURE_CHAMBER = 2;
    public static final int GUI_ID_CHARGING_STATION = 3;
    public static final int GUI_ID_ELEVATOR = 4;
    public static final int GUI_ID_PNEUMATIC_HELMET = 5;
    public static final int GUI_ID_PRESSURE_CHAMBER_INTERFACE = 6;
    public static final int GUI_ID_VACUUM_PUMP = 7;
    public static final int GUI_ID_PNEUMATIC_DOOR = 9;
    public static final int GUI_ID_ASSEMBLY_CONTROLLER = 10;
    public static final int GUI_ID_UV_LIGHT_BOX = 11;
    public static final int GUI_ID_SECURITY_STATION_INVENTORY = 12;
    public static final int GUI_ID_HACKING = 13;
    public static final int GUI_ID_UNIVERSAL_SENSOR = 14;
    public static final int GUI_ID_PNEUMATIC_GENERATOR = 15;
    public static final int GUI_ID_ELECTRIC_COMPRESSOR = 16;
    public static final int GUI_ID_PNEUMATIC_ENGINE = 17;
    public static final int GUI_ID_KINETIC_COMPRESSOR = 18;
    public static final int GUI_ID_AERIAL_INTERFACE = 19;
    public static final int GUI_ID_ELECTROSTATIC_COMPRESSOR = 20;
    public static final int GUI_ID_APHORISM_TILE = 21;
    public static final int GUI_ID_OMNIDIRECTIONAL_HOPPER = 22;
    public static final int GUI_ID_PROGRAMMER = 23;
    public static final int GUI_ID_DRONE = 24;
    public static final int GUI_ID_PRESSURE_MODULE = 25;
    public static final int GUI_ID_AIR_GRATE_MODULE = 26;
    public static final int GUI_ID_PNEUMATIC_DYNAMO = 27;
    public static final int GUI_ID_FLUX_COMPRESSOR = 28;
    public static final int GUI_ID_PLASTIC_MIXER = 29;
    public static final int GUI_ID_LIQUID_COMPRESSOR = 30;
    public static final int GUI_ID_ADVANCED_AIR_COMPRESSOR = 31;
    public static final int GUI_ID_LIQUID_HOPPER = 32;
    public static final int GUI_ID_ADVANCED_LIQUID_COMPRESSOR = 33;
    public static final int GUI_ID_REMOTE = 34;
    public static final int GUI_ID_REMOTE_EDITOR = 35;
    public static final int GUI_ID_PROGRAMMABLE_CONTROLLER = 36;
    public static final int GUI_ID_GAS_LIFT = 37;
    public static final int GUI_ID_REFINERY = 38;
    public static final int GUI_ID_THERMOPNEUMATIC_PROCESSING_PLANT = 39;
    public static final int GUI_ID_LOGISTICS_REQUESTER = 40;
    public static final int GUI_ID_LOGISTICS_STORAGE = 41;
    public static final int GUI_ID_LOGISTICS_PASSIVE_PROVIDER = 42;

    private final HackTickHandler serverHackTickHandler = new HackTickHandler();

    public void registerRenders(){

    }

    public void initConfig(Configuration config){}

    public World getClientWorld(){
        return null;
    }

    public EntityPlayer getPlayer(){
        return null;
    }

    public CommonHUDHandler getCommonHudHandler(){
        if(getSide() == Side.CLIENT) {
            return clientHudHandler;
        } else {
            return serverHudHandler;
        }
    }

    public Side getSide(){
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public void registerHandlers(){
        FMLCommonHandler.instance().bus().register(serverHudHandler = new CommonHUDHandler());
        FMLCommonHandler.instance().bus().register(getHackTickHandler());
    }

    public void postInit(){}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        switch(ID){
            case GUI_ID_AIR_COMPRESSOR:
                return new ContainerAirCompressor(player.inventory, (TileEntityAirCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_ADVANCED_AIR_COMPRESSOR:
                return new ContainerAdvancedAirCompressor(player.inventory, (TileEntityAdvancedAirCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_AIR_CANNON:
                return new ContainerAirCannon(player.inventory, (TileEntityAirCannon)world.getTileEntity(x, y, z));
            case GUI_ID_PRESSURE_CHAMBER:
                return new ContainerPressureChamber(player.inventory, (TileEntityPressureChamberValve)world.getTileEntity(x, y, z));
            case GUI_ID_CHARGING_STATION:
                return new ContainerChargingStation(player.inventory, (TileEntityChargingStation)world.getTileEntity(x, y, z));
            case GUI_ID_ELEVATOR:
                return new ContainerElevator(player.inventory, (TileEntityElevatorBase)world.getTileEntity(x, y, z));
            case GUI_ID_PNEUMATIC_HELMET:
            case GUI_ID_DRONE:
                return new ContainerChargingStationItemInventory(player.inventory, (TileEntityChargingStation)world.getTileEntity(x, y, z));
            case GUI_ID_PRESSURE_CHAMBER_INTERFACE:
                return new ContainerPressureChamberInterface(player.inventory, (TileEntityPressureChamberInterface)world.getTileEntity(x, y, z));
            case GUI_ID_VACUUM_PUMP:
                return new ContainerVacuumPump(player.inventory, (TileEntityVacuumPump)world.getTileEntity(x, y, z));
            case GUI_ID_PNEUMATIC_DOOR:
                return new ContainerPneumaticDoor(player.inventory, (TileEntityPneumaticDoorBase)world.getTileEntity(x, y, z));
            case GUI_ID_ASSEMBLY_CONTROLLER:
                return new ContainerAssemblyController(player.inventory, (TileEntityAssemblyController)world.getTileEntity(x, y, z));
            case GUI_ID_UV_LIGHT_BOX:
                return new ContainerUVLightBox(player.inventory, (TileEntityUVLightBox)world.getTileEntity(x, y, z));
            case GUI_ID_SECURITY_STATION_INVENTORY:
                return new ContainerSecurityStationInventory(player.inventory, (TileEntitySecurityStation)world.getTileEntity(x, y, z));
            case GUI_ID_HACKING:
                return new ContainerSecurityStationHacking(player.inventory, (TileEntitySecurityStation)world.getTileEntity(x, y, z));
            case GUI_ID_UNIVERSAL_SENSOR:
                return new ContainerUniversalSensor(player.inventory, (TileEntityUniversalSensor)world.getTileEntity(x, y, z));
            case GUI_ID_AERIAL_INTERFACE:
                return new Container4UpgradeSlots(player.inventory, (TileEntityAerialInterface)world.getTileEntity(x, y, z));
            case GUI_ID_ELECTROSTATIC_COMPRESSOR:
                return new Container4UpgradeSlots(player.inventory, (TileEntityElectrostaticCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_OMNIDIRECTIONAL_HOPPER:
                return new ContainerOmnidirectionalHopper(player.inventory, (TileEntityOmnidirectionalHopper)world.getTileEntity(x, y, z));
            case GUI_ID_PROGRAMMER:
                return new ContainerProgrammer(player.inventory, (TileEntityProgrammer)world.getTileEntity(x, y, z));
            case GUI_ID_PLASTIC_MIXER:
                return new ContainerPlasticMixer(player.inventory, (TileEntityPlasticMixer)world.getTileEntity(x, y, z));
            case GUI_ID_LIQUID_COMPRESSOR:
                return new ContainerLiquidCompressor(player.inventory, (TileEntityLiquidCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_ADVANCED_LIQUID_COMPRESSOR:
                return new ContainerAdvancedLiquidCompressor(player.inventory, (TileEntityAdvancedLiquidCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_LIQUID_HOPPER:
                return new ContainerLiquidHopper(player.inventory, (TileEntityLiquidHopper)world.getTileEntity(x, y, z));
            case GUI_ID_REMOTE:
            case GUI_ID_REMOTE_EDITOR:
                return new ContainerRemote(player.getCurrentEquippedItem());
            case GUI_ID_PROGRAMMABLE_CONTROLLER:
                return new ContainerProgrammableController(player.inventory, (TileEntityProgrammableController)world.getTileEntity(x, y, z));
            case GUI_ID_GAS_LIFT:
                return new ContainerGasLift(player.inventory, (TileEntityGasLift)world.getTileEntity(x, y, z));
            case GUI_ID_REFINERY:
                return new ContainerRefinery(player.inventory, (TileEntityRefinery)world.getTileEntity(x, y, z));
            case GUI_ID_THERMOPNEUMATIC_PROCESSING_PLANT:
                return new ContainerThermopneumaticProcessingPlant(player.inventory, (TileEntityThermopneumaticProcessingPlant)world.getTileEntity(x, y, z));
            case GUI_ID_LOGISTICS_REQUESTER:
            case GUI_ID_LOGISTICS_STORAGE:
            case GUI_ID_LOGISTICS_PASSIVE_PROVIDER:
                return new ContainerLogistics(player.inventory, (SemiBlockLogistics)SemiBlockManager.getInstance().getSemiBlock(world, x, y, z));
        }
        return ThirdPartyManager.instance().getServerGuiElement(ID, player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        switch(ID){
            case GUI_ID_AIR_COMPRESSOR:
                return new GuiAirCompressor(player.inventory, (TileEntityAirCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_ADVANCED_AIR_COMPRESSOR:
                return new GuiAdvancedAirCompressor(player.inventory, (TileEntityAdvancedAirCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_AIR_CANNON:
                return new GuiAirCannon(player.inventory, (TileEntityAirCannon)world.getTileEntity(x, y, z));
            case GUI_ID_PRESSURE_CHAMBER:
                return new GuiPressureChamber(player.inventory, (TileEntityPressureChamberValve)world.getTileEntity(x, y, z));
            case GUI_ID_CHARGING_STATION:
                return new GuiChargingStation(player.inventory, (TileEntityChargingStation)world.getTileEntity(x, y, z));
            case GUI_ID_ELEVATOR:
                return new GuiElevator(player.inventory, (TileEntityElevatorBase)world.getTileEntity(x, y, z));
            case GUI_ID_PNEUMATIC_HELMET:
                return new GuiPneumaticHelmet(new ContainerChargingStationItemInventory(player.inventory, (TileEntityChargingStation)world.getTileEntity(x, y, z)), (TileEntityChargingStation)world.getTileEntity(x, y, z));
            case GUI_ID_PRESSURE_CHAMBER_INTERFACE:
                return new GuiPressureChamberInterface(player.inventory, (TileEntityPressureChamberInterface)world.getTileEntity(x, y, z));
            case GUI_ID_VACUUM_PUMP:
                return new GuiVacuumPump(player.inventory, (TileEntityVacuumPump)world.getTileEntity(x, y, z));
            case GUI_ID_PNEUMATIC_DOOR:
                return new GuiPneumaticDoor(player.inventory, (TileEntityPneumaticDoorBase)world.getTileEntity(x, y, z));
            case GUI_ID_ASSEMBLY_CONTROLLER:
                return new GuiAssemblyController(player.inventory, (TileEntityAssemblyController)world.getTileEntity(x, y, z));
            case GUI_ID_UV_LIGHT_BOX:
                return new GuiUVLightBox(player.inventory, (TileEntityUVLightBox)world.getTileEntity(x, y, z));
            case GUI_ID_SECURITY_STATION_INVENTORY:
                return new GuiSecurityStationInventory(player.inventory, (TileEntitySecurityStation)world.getTileEntity(x, y, z));
            case GUI_ID_HACKING:
                return new GuiSecurityStationHacking(player.inventory, (TileEntitySecurityStation)world.getTileEntity(x, y, z));
            case GUI_ID_UNIVERSAL_SENSOR:
                return new GuiUniversalSensor(player.inventory, (TileEntityUniversalSensor)world.getTileEntity(x, y, z));
            case GUI_ID_AERIAL_INTERFACE:
                return new GuiAerialInterface(player.inventory, (TileEntityAerialInterface)world.getTileEntity(x, y, z));
            case GUI_ID_ELECTROSTATIC_COMPRESSOR:
                return new GuiElectrostaticCompressor(player.inventory, (TileEntityElectrostaticCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_APHORISM_TILE:
                return new GuiAphorismTile((TileEntityAphorismTile)world.getTileEntity(x, y, z));
            case GUI_ID_OMNIDIRECTIONAL_HOPPER:
                return new GuiOmnidirectionalHopper(player.inventory, (TileEntityOmnidirectionalHopper)world.getTileEntity(x, y, z));
            case GUI_ID_PROGRAMMER:
                return new GuiProgrammer(player.inventory, (TileEntityProgrammer)world.getTileEntity(x, y, z));
            case GUI_ID_DRONE:
                return new GuiDrone(new ContainerChargingStationItemInventory(player.inventory, (TileEntityChargingStation)world.getTileEntity(x, y, z)), (TileEntityChargingStation)world.getTileEntity(x, y, z));
            case GUI_ID_PRESSURE_MODULE:
                return new GuiPressureModule(player, x, y, z);
            case GUI_ID_AIR_GRATE_MODULE:
                return new GuiAirGrateModule(player, x, y, z);
            case GUI_ID_PLASTIC_MIXER:
                return new GuiPlasticMixer(player.inventory, (TileEntityPlasticMixer)world.getTileEntity(x, y, z));
            case GUI_ID_LIQUID_COMPRESSOR:
                return new GuiLiquidCompressor(player.inventory, (TileEntityLiquidCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_ADVANCED_LIQUID_COMPRESSOR:
                return new GuiAdvancedLiquidCompressor(player.inventory, (TileEntityAdvancedLiquidCompressor)world.getTileEntity(x, y, z));
            case GUI_ID_LIQUID_HOPPER:
                return new GuiLiquidHopper(player.inventory, (TileEntityLiquidHopper)world.getTileEntity(x, y, z));
            case GUI_ID_REMOTE:
                return new GuiRemote(player.getCurrentEquippedItem());
            case GUI_ID_REMOTE_EDITOR:
                return new GuiRemoteEditor(player.getCurrentEquippedItem());
            case GUI_ID_PROGRAMMABLE_CONTROLLER:
                return new GuiProgrammableController(player.inventory, (TileEntityProgrammableController)world.getTileEntity(x, y, z));
            case GUI_ID_GAS_LIFT:
                return new GuiGasLift(player.inventory, (TileEntityGasLift)world.getTileEntity(x, y, z));
            case GUI_ID_REFINERY:
                return new GuiRefinery(player.inventory, (TileEntityRefinery)world.getTileEntity(x, y, z));
            case GUI_ID_THERMOPNEUMATIC_PROCESSING_PLANT:
                return new GuiThermopneumaticProcessingPlant(player.inventory, (TileEntityThermopneumaticProcessingPlant)world.getTileEntity(x, y, z));
            case GUI_ID_LOGISTICS_REQUESTER:
                return new GuiLogisticsRequester(player.inventory, (SemiBlockRequester)SemiBlockManager.getClientInstance().getSemiBlock(world, x, y, z));
            case GUI_ID_LOGISTICS_STORAGE:
                return new GuiLogisticsStorage(player.inventory, (SemiBlockStorage)SemiBlockManager.getClientInstance().getSemiBlock(world, x, y, z));
            case GUI_ID_LOGISTICS_PASSIVE_PROVIDER:
                return new GuiLogisticsProvider(player.inventory, (SemiBlockActiveProvider)SemiBlockManager.getClientInstance().getSemiBlock(world, x, y, z));
        }
        return ThirdPartyManager.instance().getClientGuiElement(ID, player, world, x, y, z);
    }

    public int getArmorRenderID(String armorName){
        return 0;
    }

    public int getRenderIdForRenderer(Class clazz){
        return 0;
    }

    public void registerVillagerSkins(){}

    public HackTickHandler getHackTickHandler(){
        return serverHackTickHandler;
    }

    public boolean isSneakingInGui(){
        return false;
    }

    public void init(){}

    public void registerSemiBlockRenderer(ItemSemiBlockBase semiBlock){

    }
}
