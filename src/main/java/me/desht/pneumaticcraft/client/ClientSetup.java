package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiHelmetMainScreen;
import me.desht.pneumaticcraft.client.gui.programmer.*;
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
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModParticleTypes;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.common.entity.semiblock.*;
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
        Minecraft.getInstance().particles.registerFactory(ModParticleTypes.AIR_PARTICLE.get(), AirParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(ModParticleTypes.AIR_PARTICLE_2.get(), AirParticle.Factory::new);
    }

    private static void registerEntityRenderers() {
        // drones
        RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, RenderDrone.REGULAR_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityAmadrone.class, RenderDrone.AMADRONE_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLogisticsDrone.class, RenderDrone.LOGISTICS_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityHarvestingDrone.class, RenderDrone.HARVESTING_FACTORY);

        // semiblocks
        RenderingRegistry.registerEntityRenderingHandler(EntityCropSupport.class, RenderCropSupport.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpawnerAgitator.class, RenderSpawnerAgitator.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityHeatFrame.class, RenderHeatFrame.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTransferGadget.class, RenderTransferGadget.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLogisticsFrame.class, RenderLogisticsFrame.FACTORY);

        // misc
        RenderingRegistry.registerEntityRenderingHandler(EntityVortex.class, RenderEntityVortex.FACTORY);
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
        ScreenManager.registerFactory(ModContainers.ADVANCED_AIR_COMPRESSOR.get(), GuiAdvancedAirCompressor::new);
        ScreenManager.registerFactory(ModContainers.ADVANCED_LIQUID_COMPRESSOR.get(), GuiAdvancedLiquidCompressor::new);
        ScreenManager.registerFactory(ModContainers.AERIAL_INTERFACE.get(), GuiAerialInterface::new);
        ScreenManager.registerFactory(ModContainers.AIR_CANNON.get(), GuiAirCannon::new);
        ScreenManager.registerFactory(ModContainers.AIR_COMPRESSOR.get(), GuiAirCompressor::new);
        ScreenManager.registerFactory(ModContainers.AMADRON.get(), GuiAmadron::new);
        ScreenManager.registerFactory(ModContainers.AMADRON_ADD_TRADE.get(), GuiAmadronAddTrade::new);
        ScreenManager.registerFactory(ModContainers.ASSEMBLY_CONTROLLER.get(), GuiAssemblyController::new);
        ScreenManager.registerFactory(ModContainers.CHARGING_STATION.get(), GuiChargingStation::new);
        ScreenManager.registerFactory(ModContainers.CHARGING_ARMOR.get(), GuiPneumaticArmor::new);
        ScreenManager.registerFactory(ModContainers.CHARGING_DRONE.get(), GuiDrone::new);
        ScreenManager.registerFactory(ModContainers.CHARGING_MINIGUN.get(), GuiMinigun::new);
        ScreenManager.registerFactory(ModContainers.CREATIVE_COMPRESSOR.get(), GuiCreativeCompressor::new);
        ScreenManager.registerFactory(ModContainers.ELECTROSTATIC_COMPRESSOR.get(), GuiElectrostaticCompressor::new);
        ScreenManager.registerFactory(ModContainers.ELEVATOR.get(), GuiElevator::new);
        ScreenManager.registerFactory(ModContainers.FLUX_COMPRESSOR.get(), GuiFluxCompressor::new);
        ScreenManager.registerFactory(ModContainers.GAS_LIFT.get(), GuiGasLift::new);
        ScreenManager.registerFactory(ModContainers.INVENTORY_SEARCHER.get(), GuiInventorySearcher::new);
        ScreenManager.registerFactory(ModContainers.KEROSENE_LAMP.get(), GuiKeroseneLamp::new);
        ScreenManager.registerFactory(ModContainers.LIQUID_COMPRESSOR.get(), GuiLiquidCompressor::new);
        ScreenManager.registerFactory(ModContainers.LIQUID_HOPPER.get(), GuiLiquidHopper::new);
        ScreenManager.registerFactory(ModContainers.MINIGUN_MAGAZINE.get(), GuiMinigunMagazine::new);
        ScreenManager.registerFactory(ModContainers.OMNIDIRECTIONAL_HOPPER.get(), GuiOmnidirectionalHopper::new);
        ScreenManager.registerFactory(ModContainers.PNEUMATIC_DOOR_BASE.get(), GuiPneumaticDoor::new);
        ScreenManager.registerFactory(ModContainers.PNEUMATIC_DYNAMO.get(), GuiPneumaticDynamo::new);
        ScreenManager.registerFactory(ModContainers.PRESSURE_CHAMBER_VALVE.get(), GuiPressureChamber::new);
        ScreenManager.registerFactory(ModContainers.PRESSURE_CHAMBER_INTERFACE.get(), GuiPressureChamberInterface::new);
        ScreenManager.registerFactory(ModContainers.PROGRAMMER.get(), GuiProgrammer::new);
        ScreenManager.registerFactory(ModContainers.PROGRAMMABLE_CONTROLLER.get(), GuiProgrammableController::new);
        ScreenManager.registerFactory(ModContainers.REFINERY.get(), GuiRefineryController::new);
        ScreenManager.registerFactory(ModContainers.REMOTE.get(), GuiRemote::new);
        ScreenManager.registerFactory(ModContainers.REMOTE_EDITOR.get(), GuiRemoteEditor::new);
        ScreenManager.registerFactory(ModContainers.ITEM_SEARCHER.get(), GuiItemSearcher::new);
        ScreenManager.registerFactory(ModContainers.SECURITY_STATION_MAIN.get(), GuiSecurityStationInventory::new);
        ScreenManager.registerFactory(ModContainers.SECURITY_STATION_HACKING.get(), GuiSecurityStationHacking::new);
        ScreenManager.registerFactory(ModContainers.SENTRY_TURRET.get(), GuiSentryTurret::new);
        ScreenManager.registerFactory(ModContainers.THERMAL_COMPRESSOR.get(), GuiThermalCompressor::new);
        ScreenManager.registerFactory(ModContainers.THERMOPNEUMATIC_PROCESSING_PLANT.get(), GuiThermopneumaticProcessingPlant::new);
        ScreenManager.registerFactory(ModContainers.UNIVERSAL_SENSOR.get(), GuiUniversalSensor::new);
        ScreenManager.registerFactory(ModContainers.UV_LIGHT_BOX.get(), GuiUVLightBox::new);
        ScreenManager.registerFactory(ModContainers.VACUUM_PUMP.get(), GuiVacuumPump::new);
        ScreenManager.registerFactory(ModContainers.LOGISTICS_FRAME_PROVIDER.get(), GuiLogisticsProvider::new);
        ScreenManager.registerFactory(ModContainers.LOGISTICS_FRAME_REQUESTER.get(), GuiLogisticsRequester::new);
        ScreenManager.registerFactory(ModContainers.LOGISTICS_FRAME_STORAGE.get(), GuiLogisticsStorage::new);
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
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetText.class, GuiProgWidgetString::new);
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
                String s;
                while ((s = bufferedreader.readLine()) != null) {
                    String[] str = s.split(":");
                    if (str[0].startsWith("key_")) {
                        KeyModifier mod = str.length > 2 ? KeyModifier.valueFromString(str[2]) : KeyModifier.NONE;
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
