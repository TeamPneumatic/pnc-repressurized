package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.event.ClientTickHandler;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorMainScreen;
import me.desht.pneumaticcraft.client.gui.programmer.*;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsProvider;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsRequester;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsStorage;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiAirGrateModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiLogisticsModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiPressureModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiRedstoneModule;
import me.desht.pneumaticcraft.client.particle.AirParticle;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.render.entity.*;
import me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone;
import me.desht.pneumaticcraft.client.render.fluid.*;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.*;
import me.desht.pneumaticcraft.client.render.tileentity.*;
import me.desht.pneumaticcraft.client.render.tube_module.*;
import me.desht.pneumaticcraft.client.sound.MovingSoundJackhammer;
import me.desht.pneumaticcraft.client.util.ProgWidgetRenderer;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.core.*;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.item.ItemDrillBit;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.DramaSplash;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ClientSetup {
    public static final Map<String, Pair<Integer,KeyModifier>> keybindToKeyCodes = new HashMap<>();

    public static void initEarly() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerParticleFactories);
    }

    static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(HUDHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(ClientTickHandler.instance());
        MinecraftForge.EVENT_BUS.register(HackTickHandler.instance());
        MinecraftForge.EVENT_BUS.register(AreaRenderManager.getInstance());
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());

        registerEntityRenderers();

        EntityTrackHandler.registerDefaultEntries();
        ThirdPartyManager.instance().clientInit();

        event.enqueueWork(ClientSetup::initLate);
    }

    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(ModParticleTypes.AIR_PARTICLE.get(), AirParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(ModParticleTypes.AIR_PARTICLE_2.get(), AirParticle.Factory::new);
    }

    public static void initLate() {
        // stuff to do on the main thread
        setBlockRenderLayers();
        registerItemModelProperties();
        registerArmorClientUpgradeHandlers();
        registerTileEntityRenderers();
        registerScreenFactories();
        registerProgWidgetScreenFactories();
        registerProgWidgetExtraRenderers();
        registerTubeModuleFactories();

        getAllKeybindsFromOptionsFile();
        EntityTrackHandler.init();
        GuiArmorMainScreen.initHelmetCoreComponents();
        DramaSplash.getInstance();
    }

    private static void registerItemModelProperties() {
        ItemModelsProperties.registerProperty(ModItems.JACKHAMMER.get(), RL("drill_bit"), (stack, world, entity) -> {
            ItemDrillBit.DrillBitType type = ((ItemJackHammer) stack.getItem()).getDrillBit(stack);
            if (type == ItemDrillBit.DrillBitType.NONE) return 0f;
            if (world == null || !(entity instanceof PlayerEntity)) return 0.99f;
            long l = MovingSoundJackhammer.lastJackHammerTime((PlayerEntity) entity);
            if (l <= 20) return MathHelper.sin((world.getGameTime() % 4 / 4f) * 3.141529f);
            else return 0.99f;
        });
    }

    private static void registerProgWidgetExtraRenderers() {
        ProgWidgetRenderer.registerExtraRenderer(ModProgWidgets.CRAFTING, ProgWidgetRenderer::renderCraftingExtras);
        ProgWidgetRenderer.registerExtraRenderer(ModProgWidgets.ITEM_FILTER, ProgWidgetRenderer::renderItemFilterExtras);
    }

    private static void setBlockRenderLayers() {
        RenderTypeLookup.setRenderLayer(ModBlocks.APHORISM_TILE.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.ELEVATOR_FRAME.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.EMPTY_SPAWNER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.ETCHING_TANK.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.FLUID_MIXER.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.KEROSENE_LAMP.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.LIQUID_HOPPER.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.PRESSURE_TUBE.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.PRESSURIZED_SPAWNER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.REFINERY.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.REFINERY_OUTPUT.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.TANK_SMALL.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.TANK_MEDIUM.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.TANK_LARGE.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.UV_LIGHT_BOX.get(), RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.THERMAL_LAGGING.get(), RenderType.getTranslucent());

        // camouflageable blocks need to render in all layers, since their camo could render in any layer
        for (RegistryObject<Block> ro: ModBlocks.BLOCKS.getEntries()) {
            if (ro.get() instanceof BlockPneumaticCraftCamo) {
                RenderTypeLookup.setRenderLayer(ro.get(), r -> true);
            }
        }
    }

    private static void registerEntityRenderers() {
        // drones
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DRONE.get(), RenderDrone.REGULAR_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.AMADRONE.get(), RenderDrone.AMADRONE_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LOGISTICS_DRONE.get(), RenderDrone.LOGISTICS_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.HARVESTING_DRONE.get(), RenderDrone.HARVESTING_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.GUARD_DRONE.get(), RenderDrone.GUARD_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.COLLECTOR_DRONE.get(), RenderDrone.COLLECTOR_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PROGRAMMABLE_CONTROLLER.get(), RenderDrone.PROGRAMMABLE_CONTROLLER_FACTORY);

        // semiblocks
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CROP_SUPPORT.get(), RenderCropSupport.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SPAWNER_AGITATOR.get(), RenderSpawnerAgitator.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.HEAT_FRAME.get(), RenderHeatFrame.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.TRANSFER_GADGET.get(), RenderTransferGadget.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LOGISTICS_FRAME_ACTIVE_PROVIDER.get(), RenderLogisticsFrame.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), RenderLogisticsFrame.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LOGISTICS_FRAME_STORAGE.get(), RenderLogisticsFrame.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), RenderLogisticsFrame.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.LOGISTICS_FRAME_REQUESTER.get(), RenderLogisticsFrame.FACTORY);

        // misc
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.VORTEX.get(), RenderEntityVortex.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.RING.get(), RenderEntityRing.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.MICROMISSILE.get(), RenderMicromissile.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.TUMBLING_BLOCK.get(), RenderTumblingBlock.FACTORY);
    }

    private static void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ADVANCED_PRESSURE_TUBE.get(), RenderPressureTubeModule::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.APHORISM_TILE.get(), RenderAphorismTile::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.AIR_CANNON.get(), RenderAirCannon::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.PNEUMATIC_DOOR.get(), RenderPneumaticDoor::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.PNEUMATIC_DOOR_BASE.get(), RenderPneumaticDoorBase::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ASSEMBLY_CONTROLLER.get(), RenderAssemblyController::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ASSEMBLY_IO_UNIT.get(), RenderAssemblyIOUnit::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ASSEMBLY_PLATFORM.get(), RenderAssemblyPlatform::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ASSEMBLY_LASER.get(), RenderAssemblyLaser::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ASSEMBLY_DRILL.get(), RenderAssemblyDrill::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.CHARGING_STATION.get(), RenderChargingStation::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.DISPLAY_TABLE.get(), RenderDisplayTable::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ELEVATOR_BASE.get(), RenderElevatorBase::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ELEVATOR_CALLER.get(), RenderElevatorCaller::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.ETCHING_TANK.get(), RenderEtchingTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.FLUID_MIXER.get(), RenderFluidMixer::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.KEROSENE_LAMP.get(), RenderKeroseneLamp::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.LIQUID_HOPPER.get(), RenderLiquidHopper::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.PRESSURE_CHAMBER_VALVE.get(), RenderPressureChamber::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.PRESSURE_CHAMBER_INTERFACE.get(), RenderPressureChamberInterface::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.PRESSURE_TUBE.get(), RenderPressureTubeModule::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.PRESSURIZED_SPAWNER.get(), RenderPressurizedSpawner::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.REFINERY.get(), RenderRefineryController::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.REFINERY_OUTPUT.get(), RenderRefineryOutput::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.SECURITY_STATION.get(), RenderSecurityStation::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.SENTRY_TURRET.get(), RenderSentryTurret::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.SPAWNER_EXTRACTOR.get(), RenderSpawnerExtractor::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TANK_SMALL.get(), RenderFluidTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TANK_MEDIUM.get(), RenderFluidTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TANK_LARGE.get(), RenderFluidTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TAG_WORKBENCH.get(), RenderTagWorkbench::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.THERMOPNEUMATIC_PROCESSING_PLANT.get(), RenderThermopneumaticProcessingPlant::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.UNIVERSAL_SENSOR.get(), RenderUniversalSensor::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.VACUUM_PUMP.get(), RenderVacuumPump::new);
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
        ScreenManager.registerFactory(ModContainers.CHARGING_JACKHAMMER.get(), GuiJackhammer::new);
        ScreenManager.registerFactory(ModContainers.CREATIVE_COMPRESSOR.get(), GuiCreativeCompressor::new);
        ScreenManager.registerFactory(ModContainers.ELECTROSTATIC_COMPRESSOR.get(), GuiElectrostaticCompressor::new);
        ScreenManager.registerFactory(ModContainers.ELEVATOR.get(), GuiElevator::new);
        ScreenManager.registerFactory(ModContainers.ETCHING_TANK.get(), GuiEtchingTank::new);
        ScreenManager.registerFactory(ModContainers.FLUID_TANK.get(), GuiFluidTank::new);
        ScreenManager.registerFactory(ModContainers.FLUID_MIXER.get(), GuiFluidMixer::new);
        ScreenManager.registerFactory(ModContainers.FLUX_COMPRESSOR.get(), GuiFluxCompressor::new);
        ScreenManager.registerFactory(ModContainers.GAS_LIFT.get(), GuiGasLift::new);
        ScreenManager.registerFactory(ModContainers.INVENTORY_SEARCHER.get(), GuiInventorySearcher::new);
        ScreenManager.registerFactory(ModContainers.JACKHAMMER_SETUP.get(), GuiJackHammerSetup::new);
        ScreenManager.registerFactory(ModContainers.KEROSENE_LAMP.get(), GuiKeroseneLamp::new);
        ScreenManager.registerFactory(ModContainers.LIQUID_COMPRESSOR.get(), GuiLiquidCompressor::new);
        ScreenManager.registerFactory(ModContainers.LIQUID_HOPPER.get(), GuiLiquidHopper::new);
        ScreenManager.registerFactory(ModContainers.LOGISTICS_FRAME_PROVIDER.get(), GuiLogisticsProvider::new);
        ScreenManager.registerFactory(ModContainers.LOGISTICS_FRAME_REQUESTER.get(), GuiLogisticsRequester::new);
        ScreenManager.registerFactory(ModContainers.LOGISTICS_FRAME_STORAGE.get(), GuiLogisticsStorage::new);
        ScreenManager.registerFactory(ModContainers.MINIGUN_MAGAZINE.get(), GuiMinigunMagazine::new);
        ScreenManager.registerFactory(ModContainers.OMNIDIRECTIONAL_HOPPER.get(), GuiOmnidirectionalHopper::new);
        ScreenManager.registerFactory(ModContainers.PNEUMATIC_DOOR_BASE.get(), GuiPneumaticDoorBase::new);
        ScreenManager.registerFactory(ModContainers.PNEUMATIC_DYNAMO.get(), GuiPneumaticDynamo::new);
        ScreenManager.registerFactory(ModContainers.PRESSURE_CHAMBER_VALVE.get(), GuiPressureChamber::new);
        ScreenManager.registerFactory(ModContainers.PRESSURE_CHAMBER_INTERFACE.get(), GuiPressureChamberInterface::new);
        ScreenManager.registerFactory(ModContainers.PRESSURIZED_SPAWNER.get(), GuiPressurizedSpawner::new);
        ScreenManager.registerFactory(ModContainers.PROGRAMMER.get(), GuiProgrammer::new);
        ScreenManager.registerFactory(ModContainers.PROGRAMMABLE_CONTROLLER.get(), GuiProgrammableController::new);
        ScreenManager.registerFactory(ModContainers.REFINERY.get(), GuiRefineryController::new);
        ScreenManager.registerFactory(ModContainers.REINFORCED_CHEST.get(), GuiReinforcedChest::new);
        ScreenManager.registerFactory(ModContainers.REMOTE.get(), GuiRemote::new);
        ScreenManager.registerFactory(ModContainers.REMOTE_EDITOR.get(), GuiRemoteEditor::new);
        ScreenManager.registerFactory(ModContainers.ITEM_SEARCHER.get(), GuiItemSearcher::new);
        ScreenManager.registerFactory(ModContainers.SECURITY_STATION_MAIN.get(), GuiSecurityStationInventory::new);
        ScreenManager.registerFactory(ModContainers.SECURITY_STATION_HACKING.get(), GuiSecurityStationHacking::new);
        ScreenManager.registerFactory(ModContainers.SENTRY_TURRET.get(), GuiSentryTurret::new);
        ScreenManager.registerFactory(ModContainers.SMART_CHEST.get(), GuiSmartChest::new);
        ScreenManager.registerFactory(ModContainers.SPAWNER_EXTRACTOR.get(), GuiSpawnerExtractor::new);
        ScreenManager.registerFactory(ModContainers.TAG_MATCHER.get(), GuiTagWorkbench::new);
        ScreenManager.registerFactory(ModContainers.THERMAL_COMPRESSOR.get(), GuiThermalCompressor::new);
        ScreenManager.registerFactory(ModContainers.THERMOPNEUMATIC_PROCESSING_PLANT.get(), GuiThermopneumaticProcessingPlant::new);
        ScreenManager.registerFactory(ModContainers.UNIVERSAL_SENSOR.get(), GuiUniversalSensor::new);
        ScreenManager.registerFactory(ModContainers.UV_LIGHT_BOX.get(), GuiUVLightBox::new);
        ScreenManager.registerFactory(ModContainers.VACUUM_PUMP.get(), GuiVacuumPump::new);
        ScreenManager.registerFactory(ModContainers.VACUUM_TRAP.get(), GuiVacuumTrap::new);
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
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPickupItem.class, GuiProgWidgetPickupItem::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPlace.class, GuiProgWidgetPlace::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPressureCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetRedstoneCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEnergyCondition.class, GuiProgWidgetCondition::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetText.class, GuiProgWidgetString::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetComment.class, GuiProgWidgetString::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetTeleport.class, GuiProgWidgetGoToLocation::new);
    }

    private static void registerTubeModuleFactories() {
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_AIR_GRATE, GuiAirGrateModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_GAUGE, GuiPressureModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REGULATOR, GuiPressureModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_SAFETY_VALVE, GuiPressureModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REDSTONE, GuiRedstoneModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_LOGISTICS, GuiLogisticsModule::new);

        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_AIR_GRATE, RenderAirGrateModule::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_REDSTONE, RenderRedstoneModule::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_SAFETY_VALVE, RenderSafetyValveModule::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_REGULATOR, RenderRegulatorModule::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_GAUGE, RenderPressureGaugeModule::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_CHARGING, RenderChargingModule::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_FLOW_DETECTOR, RenderFlowDetectorModule::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_LOGISTICS, RenderLogisticsModule::new);
    }

    private static void registerArmorClientUpgradeHandlers() {
        ArmorUpgradeRegistry r = ArmorUpgradeRegistry.getInstance();
        ArmorUpgradeClientRegistry cr = ArmorUpgradeClientRegistry.getInstance();

        cr.registerHandler(r.coreComponentsHandler, new CoreComponentsClientHandler());
        cr.registerHandler(r.blockTrackerHandler, new BlockTrackerClientHandler());
        cr.registerHandler(r.entityTrackerHandler, new EntityTrackerClientHandler());
        cr.registerHandler(r.searchHandler, new SearchClientHandler());
        cr.registerHandler(r.coordTrackerHandler, new CoordTrackClientHandler());
        cr.registerHandler(r.droneDebugHandler, new DroneDebugClientHandler());
        cr.registerHandler(r.nightVisionHandler, new NightVisionClientHandler());
        cr.registerHandler(r.scubaHandler, new ScubaClientHandler());
        cr.registerHandler(r.hackHandler, new HackClientHandler());

        cr.registerHandler(r.magnetHandler, new MagnetClientHandler());
        cr.registerHandler(r.chargingHandler, new ChargingClientHandler());
        cr.registerHandler(r.chestplateLauncherHandler, new ChestplateLauncherClientHandler());
        cr.registerHandler(r.airConHandler, new AirConClientHandler());
        cr.registerHandler(r.reachDistanceHandler, new ReachDistanceClientHandler());

        cr.registerHandler(r.runSpeedHandler, new SpeedBoostClientHandler());
        cr.registerHandler(r.jumpBoostHandler, new JumpBoostClientHandler());

        cr.registerHandler(r.jetBootsHandler, new JetBootsClientHandler());
        cr.registerHandler(r.stepAssistHandler, new StepAssistClientHandler());
        cr.registerHandler(r.kickHandler, new KickClientHandler());
    }

    private static void getAllKeybindsFromOptionsFile() {
        File optionsFile = new File(Minecraft.getInstance().gameDir, "options.txt");
        if (optionsFile.exists()) {
            try (BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile))) {
                String s;
                while ((s = bufferedreader.readLine()) != null) {
                    String[] str = s.split(":");
                    if (str[0].startsWith("key_")) {
                        // key_<keybind-name>:<keycode>:<modifiers>
                        KeyModifier modifier = str.length > 2 ? KeyModifier.valueFromString(str[2]) : KeyModifier.NONE;
                        InputMappings.Input i = InputMappings.getInputByName(str[1]);
                        keybindToKeyCodes.put(str[0].substring(4), Pair.of(i.getKeyCode(), modifier));
                    }
                }
            } catch (Exception exception1) {
                Log.error("Failed to process options.txt:");
                exception1.printStackTrace();
            }
        }
    }
}
