package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiHelmetMainScreen;
import me.desht.pneumaticcraft.client.gui.programmer.*;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsDefaultStorage;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsProvider;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsRequester;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsStorage;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiAirGrateModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiPressureModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiRedstoneModule;
import me.desht.pneumaticcraft.client.model.module.*;
import me.desht.pneumaticcraft.client.particle.AirParticle;
import me.desht.pneumaticcraft.client.render.entity.*;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.tileentity.*;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModParticleTypes;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.util.DramaSplash;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    public static final Map<String, Pair<Integer,KeyModifier>> keybindToKeyCodes = new HashMap<>();

    public static void init() {
        registerEntityRenderers();
        registerTESRs();
        registerScreenFactories();
        registerProgWidgetScreenFactories();
        registerTubeModuleFactories();

        getAllKeybindsFromOptionsFile();
        EntityTrackHandler.init();
        GuiHelmetMainScreen.initHelmetMainScreen();
        DramaSplash.getInstance();
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(ModParticleTypes.AIR_PARTICLE, AirParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(ModParticleTypes.AIR_PARTICLE_2, AirParticle.Factory::new);
    }

    private static void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityVortex.class, RenderEntityVortex.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, RenderDrone.REGULAR_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLogisticsDrone.class, RenderDrone.LOGISTICS_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityHarvestingDrone.class, RenderDrone.HARVESTING_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityProgrammableController.class, RenderDrone.REGULAR_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRing.class, RenderEntityRing.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMicromissile.class, RenderMicromissile.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTumblingBlock.class, RenderTumblingBlock.FACTORY);
    }

    private static void registerTESRs() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressureTube.class, new RenderPressureTubeModule());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAphorismTile.class, new RenderAphorismTile());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAirCannon.class, new RenderAirCannon());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPneumaticDoor.class, new RenderPneumaticDoor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPneumaticDoorBase.class, new RenderPneumaticDoorBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyController.class, new RenderAssemblyController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyIOUnit.class, new RenderAssemblyIOUnit());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyPlatform.class, new RenderAssemblyPlatform());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyLaser.class, new RenderAssemblyLaser());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssemblyDrill.class, new RenderAssemblyDrill());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChargingStation.class, new RenderChargingStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElevatorBase.class, new RenderElevatorBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElevatorCaller.class, new RenderElevatorCaller());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalSensor.class, new RenderUniversalSensor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVacuumPump.class, new RenderVacuumPump());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRefineryController.class, new RenderRefineryController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRefineryOutput.class, new RenderRefineryOutput());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiquidHopper.class, new RenderLiquidHopper());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeroseneLamp.class, new RenderKeroseneLamp());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermopneumaticProcessingPlant.class, new RenderThermopneumaticProcessingPlant());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySentryTurret.class, new RenderSentryTurret());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityStation.class, new RenderSecurityStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressureChamberValve.class, new RenderPressureChamber());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressureChamberInterface.class, new RenderPressureChamberInterface());
    }

    private static void registerScreenFactories() {
        ScreenManager.registerFactory(ModContainerTypes.ADVANCED_AIR_COMPRESSOR, GuiAdvancedAirCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.ADVANCED_LIQUID_COMPRESSOR, GuiAdvancedLiquidCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.AERIAL_INTERFACE, GuiAerialInterface::new);
        ScreenManager.registerFactory(ModContainerTypes.AIR_CANNON, GuiAirCannon::new);
        ScreenManager.registerFactory(ModContainerTypes.AIR_COMPRESSOR, GuiAirCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.AMADRON, GuiAmadron::new);
        ScreenManager.registerFactory(ModContainerTypes.AMADRON_ADD_TRADE, GuiAmadronAddTrade::new);
        ScreenManager.registerFactory(ModContainerTypes.ASSEMBLY_CONTROLLER, GuiAssemblyController::new);
        ScreenManager.registerFactory(ModContainerTypes.CHARGING_STATION, GuiChargingStation::new);
        ScreenManager.registerFactory(ModContainerTypes.CHARGING_ARMOR, GuiPneumaticArmor::new);
        ScreenManager.registerFactory(ModContainerTypes.CHARGING_DRONE, GuiDrone::new);
        ScreenManager.registerFactory(ModContainerTypes.CHARGING_MINIGUN, GuiMinigun::new);
        ScreenManager.registerFactory(ModContainerTypes.CREATIVE_COMPRESSOR, GuiCreativeCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.ELECTROSTATIC_COMPRESSOR, GuiElectrostaticCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.ELEVATOR, GuiElevator::new);
        ScreenManager.registerFactory(ModContainerTypes.FLUX_COMPRESSOR, GuiFluxCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.GAS_LIFT, GuiGasLift::new);
        ScreenManager.registerFactory(ModContainerTypes.INVENTORY_SEARCHER, GuiInventorySearcher::new);
        ScreenManager.registerFactory(ModContainerTypes.KEROSENE_LAMP, GuiKeroseneLamp::new);
        ScreenManager.registerFactory(ModContainerTypes.LIQUID_COMPRESSOR, GuiLiquidCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.LIQUID_HOPPER, GuiLiquidHopper::new);
        ScreenManager.registerFactory(ModContainerTypes.MINIGUN_MAGAZINE, GuiMinigunMagazine::new);
        ScreenManager.registerFactory(ModContainerTypes.OMNIDIRECTIONAL_HOPPER, GuiOmnidirectionalHopper::new);
        ScreenManager.registerFactory(ModContainerTypes.PNEUMATIC_DOOR_BASE, GuiPneumaticDoor::new);
        ScreenManager.registerFactory(ModContainerTypes.PNEUMATIC_DYNAMO, GuiPneumaticDynamo::new);
        ScreenManager.registerFactory(ModContainerTypes.PRESSURE_CHAMBER_VALVE, GuiPressureChamber::new);
        ScreenManager.registerFactory(ModContainerTypes.PRESSURE_CHAMBER_INTERFACE, GuiPressureChamberInterface::new);
        ScreenManager.registerFactory(ModContainerTypes.PROGRAMMER, GuiProgrammer::new);
        ScreenManager.registerFactory(ModContainerTypes.PROGRAMMABLE_CONTROLLER, GuiProgrammableController::new);
        ScreenManager.registerFactory(ModContainerTypes.REFINERY, GuiRefineryController::new);
        ScreenManager.registerFactory(ModContainerTypes.REMOTE, GuiRemote::new);
        ScreenManager.registerFactory(ModContainerTypes.REMOTE_EDITOR, GuiRemoteEditor::new);
        ScreenManager.registerFactory(ModContainerTypes.SEARCHER, GuiItemSearcher::new);
        ScreenManager.registerFactory(ModContainerTypes.SECURITY_STATION_MAIN, GuiSecurityStationInventory::new);
        ScreenManager.registerFactory(ModContainerTypes.SECURITY_STATION_HACKING, GuiSecurityStationHacking::new);
        ScreenManager.registerFactory(ModContainerTypes.SENTRY_TURRET, GuiSentryTurret::new);
        ScreenManager.registerFactory(ModContainerTypes.THERMAL_COMPRESSOR, GuiThermalCompressor::new);
        ScreenManager.registerFactory(ModContainerTypes.THERMOPNEUMATIC_PROCESSING_PLANT, GuiThermopneumaticProcessingPlant::new);
        ScreenManager.registerFactory(ModContainerTypes.UNIVERSAL_SENSOR, GuiUniversalSensor::new);
        ScreenManager.registerFactory(ModContainerTypes.UV_LIGHT_BOX, GuiUVLightBox::new);
        ScreenManager.registerFactory(ModContainerTypes.VACUUM_PUMP, GuiVacuumPump::new);
        ScreenManager.registerFactory(ModContainerTypes.LOGISTICS_FRAME_DEFAULT_STORAGE, GuiLogisticsDefaultStorage::new);
        ScreenManager.registerFactory(ModContainerTypes.LOGISTICS_FRAME_PASSIVE_PROVIDER, GuiLogisticsProvider::new);
        ScreenManager.registerFactory(ModContainerTypes.LOGISTICS_FRAME_REQUESTER, GuiLogisticsRequester::new);
        ScreenManager.registerFactory(ModContainerTypes.LOGISTICS_FRAME_STORAGE, GuiLogisticsStorage::new);
    }

    private static void registerProgWidgetScreenFactories() {
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetArea.class, GuiProgWidgetArea::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetBlockCondition.class, GuiProgWidgetBlockCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetBlockRightClick.class, GuiProgWidgetBlockRightClick::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCoordinate.class, GuiProgWidgetCoordinate::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCoordinateCondition.class, GuiProgWidgetCoordinateCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCoordinateOperator.class, GuiProgWidgetCoordinateOperator::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCrafting.class, GuiProgWidgetCrafting::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDig.class, GuiProgWidgetDig::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionItem.class, GuiProgWidgetDroneCondition.Item::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionFluid.class, GuiProgWidgetDroneCondition.Fluid::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionPressure.class, GuiProgWidgetDroneCondition.Pressure::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionEnergy.class, GuiProgWidgetDroneCondition.Energy::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDropItem.class, GuiProgWidgetDropItem::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEmitRedstone.class, GuiProgWidgetEmitRedstone::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityAttack.class, GuiProgWidgetAreaShow::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityCondition.class, GuiProgWidgetCondition.Entity::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityImport.class, GuiProgWidgetAreaShow::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityRightClick.class, GuiProgWidgetAreaShow::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetExternalProgram.class, GuiProgWidgetExternalProgram::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetForEachCoordinate.class, GuiProgWidgetForEach::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetForEachItem.class, GuiProgWidgetForEach::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetGoToLocation.class, GuiProgWidgetGoToLocation::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetHarvest.class, GuiProgWidgetHarvest::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetInventoryExport.class, GuiProgWidgetImportExport::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetInventoryImport.class, GuiProgWidgetImportExport::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetItemAssign.class, GuiProgWidgetItemAssign::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetItemFilter.class, GuiProgWidgetItemFilter::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetItemInventoryCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLightCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidExport.class, GuiProgWidgetLiquidExport::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidFilter.class, GuiProgWidgetLiquidFilter::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidImport.class, GuiProgWidgetImportExport::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidInventoryCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLogistics.class, GuiProgWidgetAreaShow::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPickupItem.class, GuiProgWidgetAreaShow::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPlace.class, GuiProgWidgetPlace::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPressureCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetRedstoneCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEnergyCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetString.class, GuiProgWidgetString::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetTeleport.class, GuiProgWidgetGoToLocation::new);
    }

    private static void registerTubeModuleFactories() {
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_AIR_GRATE, GuiAirGrateModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_GAUGE, GuiPressureModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REGULATOR, GuiPressureModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_SAFETY_VALVE, GuiPressureModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REDSTONE, GuiRedstoneModule::new);

        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_AIR_GRATE, ModelAirGrate::new);
        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_REDSTONE, ModelRedstone::new);
        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_SAFETY_VALVE, ModelSafetyValve::new);
        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_REGULATOR, ModelPressureRegulator::new);
        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_GAUGE, ModelPressureGauge::new);
        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_CHARGING, ModelCharging::new);
        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_FLOW_DETECTOR, ModelFlowDetector::new);
        TubeModuleClientRegistry.registerTubeModuleModel(Names.MODULE_LOGISTICS, ModelLogistics::new);
    }

    private static void getAllKeybindsFromOptionsFile() {
        File optionsFile = new File(Minecraft.getInstance().gameDir, "options.txt");
        if (optionsFile.exists()) {
            try (BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile))) {
                String s = "";
                while ((s = bufferedreader.readLine()) != null) {
                    String[] str = s.split(":");
                    if (str[0].startsWith("key_")) {
                        KeyModifier mod = str.length > 2 ? KeyModifier.valueFromString(str[2]) : KeyModifier.NONE;
//                        keybindToKeyCodes.put(str[0].substring(4), Pair.of(Integer.parseInt(str[1]), mod));
                        InputMappings.Input i = InputMappings.getInputByName(str[1]);
                        keybindToKeyCodes.put(str[0].substring(4), Pair.of(i.getKeyCode(), mod));
                    }
                }
            } catch (Exception exception1) {
                Log.error("Failed to process options.txt:");
                exception1.printStackTrace();
            }
        }
    }

}
