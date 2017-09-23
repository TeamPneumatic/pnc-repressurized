package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsProvider;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsRequester;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsStorage;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiAirGrateModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiPressureModule;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.CapabilityHackingProvider;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.HackTickHandler;
import me.desht.pneumaticcraft.common.inventory.*;
import me.desht.pneumaticcraft.common.semiblock.*;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler {
    protected CommonHUDHandler clientHudHandler;
    private CommonHUDHandler serverHudHandler;

    public int PneumaticHelmetRenderID = 0;

    public enum EnumGuiId {
        AIR_COMPRESSOR, AIR_CANNON, PRESSURE_CHAMBER, CHARGING_STATION, ELEVATOR, PNEUMATIC_HELMET, PRESSURE_CHAMBER_INTERFACE, VACUUM_PUMP, PNEUMATIC_DOOR, ASSEMBLY_CONTROLLER, UV_LIGHT_BOX, SECURITY_STATION_INVENTORY, HACKING, UNIVERSAL_SENSOR, PNEUMATIC_GENERATOR, ELECTRIC_COMPRESSOR, PNEUMATIC_ENGINE, KINETIC_COMPRESSOR, AERIAL_INTERFACE, ELECTROSTATIC_COMPRESSOR, APHORISM_TILE, OMNIDIRECTIONAL_HOPPER, PROGRAMMER, DRONE, PRESSURE_MODULE, AIR_GRATE_MODULE, PNEUMATIC_DYNAMO, FLUX_COMPRESSOR, PLASTIC_MIXER, LIQUID_COMPRESSOR, ADVANCED_AIR_COMPRESSOR, LIQUID_HOPPER, ADVANCED_LIQUID_COMPRESSOR, REMOTE, REMOTE_EDITOR, PROGRAMMABLE_CONTROLLER, GAS_LIFT, REFINERY, THERMOPNEUMATIC_PROCESSING_PLANT, LOGISTICS_REQUESTER, LOGISTICS_STORAGE, LOGISTICS_PASSIVE_PROVIDER, AMADRON, AMADRON_ADD_TRADE, CREATIVE_COMPRESSOR, KEROSENE_LAMP, SENTRY_TURRET
    }

    private final HackTickHandler serverHackTickHandler = new HackTickHandler();

    public void initConfig() {
    }

    public World getClientWorld() {
        return null;
    }

    public EntityPlayer getPlayer() {
        return null;
    }

    public CommonHUDHandler getCommonHudHandler() {
        if (getSide() == Side.CLIENT) {
            return clientHudHandler;
        } else {
            return serverHudHandler;
        }
    }

    public Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public void postInit() {
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.isBlockLoaded(pos) ? world.getTileEntity(pos) : null;
        switch (EnumGuiId.values()[ID]) {
            case AIR_COMPRESSOR:
                return new ContainerAirCompressor(player.inventory, (TileEntityAirCompressor) te);
            case ADVANCED_AIR_COMPRESSOR:
                return new ContainerAdvancedAirCompressor(player.inventory, (TileEntityAdvancedAirCompressor) te);
            case AIR_CANNON:
                return new ContainerAirCannon(player.inventory, (TileEntityAirCannon) te);
            case PRESSURE_CHAMBER:
                return new ContainerPressureChamber(player.inventory, (TileEntityPressureChamberValve) te);
            case CHARGING_STATION:
                return new ContainerChargingStation(player.inventory, (TileEntityChargingStation) te);
            case ELEVATOR:
                return new ContainerElevator(player.inventory, (TileEntityElevatorBase) te);
            case PNEUMATIC_HELMET:
            case DRONE:
                return new ContainerChargingStationItemInventory(player.inventory, (TileEntityChargingStation) te);
            case PRESSURE_CHAMBER_INTERFACE:
                return new ContainerPressureChamberInterface(player.inventory, (TileEntityPressureChamberInterface) te);
            case VACUUM_PUMP:
                return new ContainerVacuumPump(player.inventory, (TileEntityVacuumPump) te);
            case PNEUMATIC_DOOR:
                return new ContainerPneumaticDoor(player.inventory, (TileEntityPneumaticDoorBase) te);
            case ASSEMBLY_CONTROLLER:
                return new ContainerAssemblyController(player.inventory, (TileEntityAssemblyController) te);
            case UV_LIGHT_BOX:
                return new ContainerUVLightBox(player.inventory, (TileEntityUVLightBox) te);
            case SECURITY_STATION_INVENTORY:
                return new ContainerSecurityStationInventory(player.inventory, (TileEntitySecurityStation) te);
            case HACKING:
                return new ContainerSecurityStationHacking(player.inventory, (TileEntitySecurityStation) te);
            case UNIVERSAL_SENSOR:
                return new ContainerUniversalSensor(player.inventory, (TileEntityUniversalSensor) te);
            case AERIAL_INTERFACE:
                return new Container4UpgradeSlots(player.inventory, (TileEntityAerialInterface) te);
            case ELECTROSTATIC_COMPRESSOR:
                return new Container4UpgradeSlots(player.inventory, (TileEntityElectrostaticCompressor) te);
            case OMNIDIRECTIONAL_HOPPER:
                return new ContainerOmnidirectionalHopper(player.inventory, (TileEntityOmnidirectionalHopper) te);
            case PROGRAMMER:
                return new ContainerProgrammer(player.inventory, (TileEntityProgrammer) te);
            case PLASTIC_MIXER:
                return new ContainerPlasticMixer(player.inventory, (TileEntityPlasticMixer) te);
            case LIQUID_COMPRESSOR:
                return new ContainerLiquidCompressor(player.inventory, (TileEntityLiquidCompressor) te);
            case ADVANCED_LIQUID_COMPRESSOR:
                return new ContainerAdvancedLiquidCompressor(player.inventory, (TileEntityAdvancedLiquidCompressor) te);
            case LIQUID_HOPPER:
                return new ContainerLiquidHopper(player.inventory, (TileEntityLiquidHopper) te);
            case REMOTE:
            case REMOTE_EDITOR:
                return new ContainerRemote(player.getHeldItemMainhand());
            case PROGRAMMABLE_CONTROLLER:
                return new ContainerProgrammableController(player.inventory, (TileEntityProgrammableController) te);
            case GAS_LIFT:
                return new ContainerGasLift(player.inventory, (TileEntityGasLift) te);
            case REFINERY:
                return new ContainerRefinery(player.inventory, (TileEntityRefinery) te);
            case THERMOPNEUMATIC_PROCESSING_PLANT:
                return new ContainerThermopneumaticProcessingPlant(player.inventory, (TileEntityThermopneumaticProcessingPlant) te);
            case LOGISTICS_REQUESTER:
            case LOGISTICS_STORAGE:
            case LOGISTICS_PASSIVE_PROVIDER:
                return new ContainerLogistics(player.inventory, (SemiBlockLogistics) SemiBlockManager.getInstance(world).getSemiBlock(world, pos));
            case AMADRON:
                return new ContainerAmadron(player);
            case AMADRON_ADD_TRADE:
                return new ContainerAmadronAddTrade();
            case CREATIVE_COMPRESSOR:
                return new ContainerPneumaticBase((TileEntityBase) te);
            case KEROSENE_LAMP:
                return new ContainerKeroseneLamp(player.inventory, (TileEntityKeroseneLamp) te);
            case SENTRY_TURRET:
                return new ContainerSentryTurret(player.inventory, (TileEntitySentryTurret) te);
        }
        return ThirdPartyManager.instance().getServerGuiElement(ID, player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.isBlockLoaded(pos) ? world.getTileEntity(pos) : null;
        switch (EnumGuiId.values()[ID]) {
            case AIR_COMPRESSOR:
                return new GuiAirCompressor(player.inventory, (TileEntityAirCompressor) te);
            case ADVANCED_AIR_COMPRESSOR:
                return new GuiAdvancedAirCompressor(player.inventory, (TileEntityAdvancedAirCompressor) te);
            case AIR_CANNON:
                return new GuiAirCannon(player.inventory, (TileEntityAirCannon) te);
            case PRESSURE_CHAMBER:
                return new GuiPressureChamber(player.inventory, (TileEntityPressureChamberValve) te);
            case CHARGING_STATION:
                return new GuiChargingStation(player.inventory, (TileEntityChargingStation) te);
            case ELEVATOR:
                return new GuiElevator(player.inventory, (TileEntityElevatorBase) te);
            case PNEUMATIC_HELMET:
                return new GuiPneumaticHelmet(new ContainerChargingStationItemInventory(player.inventory, (TileEntityChargingStation) te), (TileEntityChargingStation) te);
            case PRESSURE_CHAMBER_INTERFACE:
                return new GuiPressureChamberInterface(player.inventory, (TileEntityPressureChamberInterface) te);
            case VACUUM_PUMP:
                return new GuiVacuumPump(player.inventory, (TileEntityVacuumPump) te);
            case PNEUMATIC_DOOR:
                return new GuiPneumaticDoor(player.inventory, (TileEntityPneumaticDoorBase) te);
            case ASSEMBLY_CONTROLLER:
                return new GuiAssemblyController(player.inventory, (TileEntityAssemblyController) te);
            case UV_LIGHT_BOX:
                return new GuiUVLightBox(player.inventory, (TileEntityUVLightBox) te);
            case SECURITY_STATION_INVENTORY:
                return new GuiSecurityStationInventory(player.inventory, (TileEntitySecurityStation) te);
            case HACKING:
                return new GuiSecurityStationHacking(player.inventory, (TileEntitySecurityStation) te);
            case UNIVERSAL_SENSOR:
                return new GuiUniversalSensor(player.inventory, (TileEntityUniversalSensor) te);
            case AERIAL_INTERFACE:
                return new GuiAerialInterface(player.inventory, (TileEntityAerialInterface) te);
            case ELECTROSTATIC_COMPRESSOR:
                return new GuiElectrostaticCompressor(player.inventory, (TileEntityElectrostaticCompressor) te);
            case APHORISM_TILE:
                return new GuiAphorismTile((TileEntityAphorismTile) te);
            case OMNIDIRECTIONAL_HOPPER:
                return new GuiOmnidirectionalHopper(player.inventory, (TileEntityOmnidirectionalHopper) te);
            case PROGRAMMER:
                return new GuiProgrammer(player.inventory, (TileEntityProgrammer) te);
            case DRONE:
                return new GuiDrone(new ContainerChargingStationItemInventory(player.inventory, (TileEntityChargingStation) te), (TileEntityChargingStation) te);
            case PRESSURE_MODULE:
                return new GuiPressureModule(player, x, y, z);
            case AIR_GRATE_MODULE:
                return new GuiAirGrateModule(player, x, y, z);
            case PLASTIC_MIXER:
                return new GuiPlasticMixer(player.inventory, (TileEntityPlasticMixer) te);
            case LIQUID_COMPRESSOR:
                return new GuiLiquidCompressor(player.inventory, (TileEntityLiquidCompressor) te);
            case ADVANCED_LIQUID_COMPRESSOR:
                return new GuiAdvancedLiquidCompressor(player.inventory, (TileEntityAdvancedLiquidCompressor) te);
            case LIQUID_HOPPER:
                return new GuiLiquidHopper(player.inventory, (TileEntityLiquidHopper) te);
            case REMOTE:
                return new GuiRemote(player.getHeldItemMainhand());
            case REMOTE_EDITOR:
                return new GuiRemoteEditor(player.getHeldItemMainhand());
            case PROGRAMMABLE_CONTROLLER:
                return new GuiProgrammableController(player.inventory, (TileEntityProgrammableController) te);
            case GAS_LIFT:
                return new GuiGasLift(player.inventory, (TileEntityGasLift) te);
            case REFINERY:
                return new GuiRefinery(player.inventory, (TileEntityRefinery) te);
            case THERMOPNEUMATIC_PROCESSING_PLANT:
                return new GuiThermopneumaticProcessingPlant(player.inventory, (TileEntityThermopneumaticProcessingPlant) te);
            case LOGISTICS_REQUESTER:
                return new GuiLogisticsRequester(player.inventory, (SemiBlockRequester) SemiBlockManager.getInstance(world).getSemiBlock(world, pos));
            case LOGISTICS_STORAGE:
                return new GuiLogisticsStorage(player.inventory, (SemiBlockStorage) SemiBlockManager.getInstance(world).getSemiBlock(world, pos));
            case LOGISTICS_PASSIVE_PROVIDER:
                return new GuiLogisticsProvider(player.inventory, (SemiBlockActiveProvider) SemiBlockManager.getInstance(world).getSemiBlock(world, pos));
            case AMADRON:
                return new GuiAmadron(player.inventory);
            case AMADRON_ADD_TRADE:
                return new GuiAmadronAddTrade();
            case CREATIVE_COMPRESSOR:
                return new GuiCreativeCompressor((TileEntityCreativeCompressor) te);
            case KEROSENE_LAMP:
                return new GuiKeroseneLamp(player.inventory, (TileEntityKeroseneLamp) te);
            case SENTRY_TURRET:
                return new GuiSentryTurret(player.inventory, (TileEntitySentryTurret) te);
        }
        return ThirdPartyManager.instance().getClientGuiElement(ID, player, world, x, y, z);
    }

    public int getArmorRenderID(String armorName) {
        return 0;
    }

    public int getRenderIdForRenderer(Class clazz) {
        return 0;
    }

    public void registerVillagerSkins() {
    }

    public HackTickHandler getHackTickHandler() {
        return serverHackTickHandler;
    }

    public boolean isSneakingInGui() {
        return false;
    }

    public void preInit() {
        CapabilityHackingProvider.register();
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(serverHudHandler = new CommonHUDHandler());
        MinecraftForge.EVENT_BUS.register(getHackTickHandler());
    }

    public void registerSemiBlockRenderer(Item semiBlock) {

    }

    public void addScheduledTask(Runnable runnable, boolean serverSide) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
    }
}
