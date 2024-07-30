package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IClientArmorRegistry;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.programmer.*;
import me.desht.pneumaticcraft.client.gui.remote.RemoteClientRegistry;
import me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteScreen;
import me.desht.pneumaticcraft.client.gui.semiblock.LogisticsProviderScreen;
import me.desht.pneumaticcraft.client.gui.semiblock.LogisticsRequesterScreen;
import me.desht.pneumaticcraft.client.gui.semiblock.LogisticsStorageScreen;
import me.desht.pneumaticcraft.client.gui.tubemodule.*;
import me.desht.pneumaticcraft.client.gui.upgrademanager.*;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDroneCore;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.*;
import me.desht.pneumaticcraft.client.particle.AirParticle;
import me.desht.pneumaticcraft.client.particle.BulletParticle;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker.BlockTrackHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.*;
import me.desht.pneumaticcraft.client.render.ProgWidgetRenderer;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.render.blockentity.*;
import me.desht.pneumaticcraft.client.render.entity.RenderEntityRing;
import me.desht.pneumaticcraft.client.render.entity.RenderEntityVortex;
import me.desht.pneumaticcraft.client.render.entity.RenderMicromissile;
import me.desht.pneumaticcraft.client.render.entity.RenderTumblingBlock;
import me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone;
import me.desht.pneumaticcraft.client.render.entity.semiblock.*;
import me.desht.pneumaticcraft.client.render.fluid.*;
import me.desht.pneumaticcraft.client.render.overlays.JackhammerOverlay;
import me.desht.pneumaticcraft.client.render.overlays.MinigunOverlay;
import me.desht.pneumaticcraft.client.render.overlays.PneumaticArmorHUDOverlay;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticArmorLayer;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticElytraLayer;
import me.desht.pneumaticcraft.client.render.tube_module.*;
import me.desht.pneumaticcraft.client.sound.MovingSoundJackhammer;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.item.DrillBitItem;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.registry.*;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ClientSetup {
    public static void onModConstruction(IEventBus modBus) {
        modBus.addListener(ClientSetup::onClientSetup);
        modBus.addListener(ClientSetup::registerGuiOverlays);
        modBus.addListener(ClientSetup::registerTooltipComponentFactories);
        modBus.addListener(ClientSetup::registerParticleFactories);
        modBus.addListener(ClientSetup::registerRenderers);
        modBus.addListener(ClientSetup::registerRenderLayers);
        modBus.addListener(ClientSetup::registerLayerDefinitions);
        modBus.addListener(ClientSetup::registerKeyMappings);
        modBus.addListener(ClientSetup::registerScreens);

        NeoForge.EVENT_BUS.register(HUDHandler.getInstance());
        NeoForge.EVENT_BUS.register(AreaRenderManager.getInstance());
        NeoForge.EVENT_BUS.register(KeyHandler.getInstance());
    }

    static void onClientSetup(FMLClientSetupEvent event) {
        EntityTrackHandler.getInstance().registerDefaultEntries();
        BlockTrackHandler.getInstance().registerDefaultEntries();
        ThirdPartyManager.instance().clientInit();

        registerProgWidgetScreenFactories();
        registerTubeModuleFactories();
        registerArmorClientUpgradeHandlers();
        RemoteClientRegistry.INSTANCE.registerClientFactories();

        event.enqueueWork(ClientSetup::initLate);
    }

    public static void registerTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MicromissilesItem.Tooltip.class, MicromissileClientTooltip::new);
    }

    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, RL("jackhammer"), new JackhammerOverlay());
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, RL("minigun"), new MinigunOverlay());
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, RL("ender_visor"), new EnderVisorClientHandler.PumpkinLayer());
        event.registerAboveAll(RL("pneumatic_armor"), new PneumaticArmorHUDOverlay());
    }

    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.AIR_PARTICLE.get(), AirParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.AIR_PARTICLE_2.get(), AirParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.BULLET_PARTICLE.get(), BulletParticle.Factory::new);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyHandler.getInstance().registerKeyMappings(event);
    }

    public static void initLate() {
        // stuff to do on the main thread
        registerItemModelProperties();

        // freeze entity & block track handlers before registering client upgrade handlers
        EntityTrackHandler.getInstance().freeze();
        BlockTrackHandler.getInstance().freeze();
        ClientArmorRegistry.getInstance().registerSubKeyBinds();
        ClientArmorRegistry.getInstance().registerKeybindsWithMinecraft();
        ClientArmorRegistry.getInstance().freeze();

        registerProgWidgetExtraRenderers();
    }

    public static void registerRenderLayers(EntityRenderersEvent.AddLayers event) {
        event.getEntityTypes().forEach(type -> {
            var entityRenderer = event.getRenderer(type);
            if (entityRenderer instanceof HumanoidMobRenderer<?, ?> hmr) {
                addPneumaticArmorRenderLayer(hmr, event.getEntityModels(), event.getContext());
            } else if (entityRenderer instanceof ArmorStandRenderer asr) {
                addPneumaticArmorRenderLayer(asr, event.getEntityModels(), event.getContext());
            }
        });

        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<?> render = event.getSkin(skin);
            if (render instanceof PlayerRenderer pr) {
                addPneumaticArmorRenderLayer(pr, event.getEntityModels(), event.getContext());
                addElytraRenderLayer(pr, event.getEntityModels());
            }
        }
    }

    private static <T extends LivingEntity, M extends HumanoidModel<T>> void addElytraRenderLayer(LivingEntityRenderer<T, M> render, EntityModelSet models) {
        render.addLayer(new PneumaticElytraLayer<>(render, models));
    }

    private static <T extends LivingEntity, M extends HumanoidModel<T>> void addPneumaticArmorRenderLayer(LivingEntityRenderer<T, M> render, EntityModelSet models, EntityRendererProvider.Context context) {
        render.addLayer(new PneumaticArmorLayer<>(render, models, context.getModelManager()));
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // armor
        event.registerLayerDefinition(PNCModelLayers.PNEUMATIC_LEGS, () -> LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.INNER_ARMOR_DEFORMATION, 0f), 64, 32));
        event.registerLayerDefinition(PNCModelLayers.PNEUMATIC_ARMOR, () -> LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.OUTER_ARMOR_DEFORMATION, 0f), 64, 32));

        // semiblocks
        event.registerLayerDefinition(PNCModelLayers.HEAT_FRAME, ModelHeatFrame::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.LOGISTICS_FRAME, ModelLogisticsFrame::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SPAWNER_AGITATOR, ModelSpawnerAgitator::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.TRANSFER_GADGET, ModelTransferGadget::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.CROP_SUPPORT, ModelCropSupport::createBodyLayer);

        // drones
        event.registerLayerDefinition(PNCModelLayers.DRONE, ModelDrone::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.DRONE_CORE, ModelDroneCore::createBodyLayer);

        // minigun
        event.registerLayerDefinition(PNCModelLayers.MINIGUN, ModelMinigun::createBodyLayer);

        // block entities
        event.registerLayerDefinition(PNCModelLayers.AIR_CANNON, AirCannonRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PNEUMATIC_DOOR, PneumaticDoorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PNEUMATIC_DOOR_BASE, PneumaticDoorBaseRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_CONTROLLER, AssemblyControllerRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_DRILL, AssemblyDrillRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_LASER, AssemblyLaserRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_IO_UNIT, AssemblyIOUnitRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_PLATFORM, AssemblyPlatformRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ELEVATOR_BASE, ElevatorBaseRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.MANUAL_COMPRESSOR, ManualCompressorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PRESSURE_CHAMBER_INTERFACE, PressureChamberInterfaceRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SPAWNER_EXTRACTOR, SpawnerExtractorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SPAWNER_EXTRACTOR, SpawnerExtractorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.UNIVERSAL_SENSOR, UniversalSensorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.VACUUM_PUMP, VacuumPumpRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SOLAR_COMPRESSOR, SolarCompressorRenderer::createBodyLayer);

        // tube modules
        event.registerLayerDefinition(PNCModelLayers.AIR_GRATE_MODULE, AirGrateRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.CHARGING_MODULE, ChargingRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.FLOW_DETECTOR_MODULE, FlowDetectorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.LOGISTICS_MODULE, LogisticsRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PRESSURE_GAUGE_MODULE, PressureGaugeRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.REDSTONE_MODULE, RedstoneRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.REGULATOR_MODULE, RegulatorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SAFETY_VALVE_MODULE, SafetyValveRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.VACUUM_MODULE, VacuumRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.THERMOSTAT_MODULE, ThermostatRenderer::createBodyLayer);
    }

    private static void registerItemModelProperties() {
        ItemProperties.register(ModItems.JACKHAMMER.get(), RL("drill_bit"), (stack, world, entity, seed) -> {
            DrillBitItem.DrillBitType type = ((JackHammerItem) stack.getItem()).getDrillBit(stack);
            if (type == DrillBitItem.DrillBitType.NONE) return 0f;
            if (world == null || !(entity instanceof Player)) return 0.99f;
            long l = MovingSoundJackhammer.lastJackHammerTime((Player) entity);
            if (l <= 20) return Mth.sin((world.getGameTime() % 4 / 4f) * 3.141529f);
            else return 0.99f;
        });
    }

    private static void registerProgWidgetExtraRenderers() {
        ProgWidgetRenderer.registerItemRenderer(ModProgWidgetTypes.CRAFTING.get(), ProgWidgetRenderer::renderCraftingItem);
        ProgWidgetRenderer.registerItemRenderer(ModProgWidgetTypes.ITEM_FILTER.get(), ProgWidgetRenderer::renderItemFilterItem);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerEntityRenderers(event);
        registerBlockEntityRenderers(event);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // drones
        event.registerEntityRenderer(ModEntityTypes.DRONE.get(), RenderDrone::standard);
        event.registerEntityRenderer(ModEntityTypes.AMADRONE.get(), RenderDrone::amadrone);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_DRONE.get(), RenderDrone::logistics);
        event.registerEntityRenderer(ModEntityTypes.HARVESTING_DRONE.get(), RenderDrone::harvesting);
        event.registerEntityRenderer(ModEntityTypes.GUARD_DRONE.get(), RenderDrone::guard);
        event.registerEntityRenderer(ModEntityTypes.COLLECTOR_DRONE.get(), RenderDrone::collector);
        event.registerEntityRenderer(ModEntityTypes.PROGRAMMABLE_CONTROLLER.get(), RenderDrone::programmableController);

        // semiblocks
        event.registerEntityRenderer(ModEntityTypes.CROP_SUPPORT.get(), RenderCropSupport::new);
        event.registerEntityRenderer(ModEntityTypes.SPAWNER_AGITATOR.get(), RenderSpawnerAgitator::new);
        event.registerEntityRenderer(ModEntityTypes.HEAT_FRAME.get(), RenderHeatFrame::new);
        event.registerEntityRenderer(ModEntityTypes.TRANSFER_GADGET.get(), RenderTransferGadget::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_ACTIVE_PROVIDER.get(), RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_STORAGE.get(), RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_REQUESTER.get(), RenderLogisticsFrame::new);

        // misc
        event.registerEntityRenderer(ModEntityTypes.VORTEX.get(), RenderEntityVortex::new);
        event.registerEntityRenderer(ModEntityTypes.RING.get(), RenderEntityRing::new);
        event.registerEntityRenderer(ModEntityTypes.MICROMISSILE.get(), RenderMicromissile::new);
        event.registerEntityRenderer(ModEntityTypes.TUMBLING_BLOCK.get(), RenderTumblingBlock::new);
    }

    private static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ADVANCED_LIQUID_COMPRESSOR.get(), RenderAdvancedLiquidCompressor::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ADVANCED_PRESSURE_TUBE.get(), PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.AERIAL_INTERFACE.get(), AerialInterfaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.AIR_CANNON.get(), AirCannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.APHORISM_TILE.get(), AphorismTileRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_CONTROLLER.get(), AssemblyControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_DRILL.get(), AssemblyDrillRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_IO_UNIT.get(), AssemblyIOUnitRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_LASER.get(), AssemblyLaserRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_PLATFORM.get(), AssemblyPlatformRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CHARGING_STATION.get(), ChargingStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.DISPLAY_TABLE.get(), DisplayTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ELEVATOR_BASE.get(), ElevatorBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ELEVATOR_CALLER.get(), ElevatorCallerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ETCHING_TANK.get(), RenderEtchingTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.FLUID_MIXER.get(), RenderFluidMixer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.KEROSENE_LAMP.get(), RenderKeroseneLamp::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.GAS_LIFT.get(), RenderGasLift::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.LIQUID_HOPPER.get(), RenderLiquidHopper::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.LIQUID_COMPRESSOR.get(), RenderLiquidCompressor::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.MANUAL_COMPRESSOR.get(), ManualCompressorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PNEUMATIC_DOOR.get(), PneumaticDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PNEUMATIC_DOOR_BASE.get(), PneumaticDoorBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PRESSURE_CHAMBER_VALVE.get(), PressureChamberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PRESSURE_CHAMBER_INTERFACE.get(), PressureChamberInterfaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PRESSURE_TUBE.get(), PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PROGRAMMER.get(), ProgrammerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.REFINERY.get(), RenderRefineryController::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.REFINERY_OUTPUT.get(), RenderRefineryOutput::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.REINFORCED_PRESSURE_TUBE.get(), PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SENTRY_TURRET.get(), SentryTurretRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SPAWNER_EXTRACTOR.get(), SpawnerExtractorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_SMALL.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_MEDIUM.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_LARGE.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_HUGE.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TAG_WORKBENCH.get(), TagWorkbenchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), RenderThermopneumaticProcessingPlant::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.UNIVERSAL_SENSOR.get(), UniversalSensorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.VACUUM_PUMP.get(), VacuumPumpRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SOLAR_COMPRESSOR.get(), SolarCompressorRenderer::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ADVANCED_AIR_COMPRESSOR.get(), AdvancedAirCompressorScreen::new);
        event.register(ModMenuTypes.ADVANCED_LIQUID_COMPRESSOR.get(), AdvancedLiquidCompressorScreen::new);
        event.register(ModMenuTypes.AERIAL_INTERFACE.get(), AerialInterfaceScreen::new);
        event.register(ModMenuTypes.AIR_CANNON.get(), AirCannonScreen::new);
        event.register(ModMenuTypes.AIR_COMPRESSOR.get(), AirCompressorScreen::new);
        event.register(ModMenuTypes.AMADRON.get(), AmadronScreen::new);
        event.register(ModMenuTypes.AMADRON_ADD_TRADE.get(), AmadronAddTradeScreen::new);
        event.register(ModMenuTypes.ASSEMBLY_CONTROLLER.get(), AssemblyControllerScreen::new);
        event.register(ModMenuTypes.CHARGING_STATION.get(), ChargingStationScreen::new);
        event.register(ModMenuTypes.CHARGING_AMADRON.get(), AmadronUpgradeScreen::new);
        event.register(ModMenuTypes.CHARGING_ARMOR.get(), PneumaticArmorUpgradeScreen::new);
        event.register(ModMenuTypes.CHARGING_DRONE.get(), DroneUpgradeScreen::new);
        event.register(ModMenuTypes.CHARGING_MINIGUN.get(), MinigunUpgradeScreen::new);
        event.register(ModMenuTypes.CHARGING_JACKHAMMER.get(), JackhammerUpgradeScreen::new);
        event.register(ModMenuTypes.CREATIVE_COMPRESSOR.get(), CreativeCompressorScreen::new);
        event.register(ModMenuTypes.CREATIVE_COMPRESSED_IRON_BLOCK.get(), CreativeCompressedIronBlockScreen::new);
        event.register(ModMenuTypes.ELECTROSTATIC_COMPRESSOR.get(), ElectrostaticCompressorScreen::new);
        event.register(ModMenuTypes.ELEVATOR.get(), ElevatorScreen::new);
        event.register(ModMenuTypes.ETCHING_TANK.get(), EtchingTankScreen::new);
        event.register(ModMenuTypes.FLUID_TANK.get(), FluidTankScreen::new);
        event.register(ModMenuTypes.FLUID_MIXER.get(), FluidMixerScreen::new);
        event.register(ModMenuTypes.FLUX_COMPRESSOR.get(), FluxCompressorScreen::new);
        event.register(ModMenuTypes.GAS_LIFT.get(), GasLiftScreen::new);
        event.register(ModMenuTypes.INVENTORY_SEARCHER.get(), InventorySearcherScreen::new);
        event.register(ModMenuTypes.JACKHAMMER_SETUP.get(), JackHammerSetupScreen::new);
        event.register(ModMenuTypes.KEROSENE_LAMP.get(), KeroseneLampScreen::new);
        event.register(ModMenuTypes.LIQUID_COMPRESSOR.get(), LiquidCompressorScreen::new);
        event.register(ModMenuTypes.LIQUID_HOPPER.get(), LiquidHopperScreen::new);
        event.register(ModMenuTypes.LOGISTICS_FRAME_PROVIDER.get(), LogisticsProviderScreen::new);
        event.register(ModMenuTypes.LOGISTICS_FRAME_REQUESTER.get(), LogisticsRequesterScreen::new);
        event.register(ModMenuTypes.LOGISTICS_FRAME_STORAGE.get(), LogisticsStorageScreen::new);
        event.register(ModMenuTypes.MINIGUN_MAGAZINE.get(), MinigunMagazineScreen::new);
        event.register(ModMenuTypes.OMNIDIRECTIONAL_HOPPER.get(), OmnidirectionalHopperScreen::new);
        event.register(ModMenuTypes.PNEUMATIC_DOOR_BASE.get(), PneumaticDoorBaseScreen::new);
        event.register(ModMenuTypes.PNEUMATIC_DYNAMO.get(), PneumaticDynamoScreen::new);
        event.register(ModMenuTypes.PRESSURE_CHAMBER_VALVE.get(), PressureChamberScreen::new);
        event.register(ModMenuTypes.PRESSURE_CHAMBER_INTERFACE.get(), PressureChamberInterfaceScreen::new);
        event.register(ModMenuTypes.PRESSURIZED_SPAWNER.get(), PressurizedSpawnerScreen::new);
        event.register(ModMenuTypes.PROGRAMMER.get(), ProgrammerScreen::new);
        event.register(ModMenuTypes.PROGRAMMABLE_CONTROLLER.get(), ProgrammableControllerScreen::new);
        event.register(ModMenuTypes.REFINERY.get(), RefineryControllerScreen::new);
        event.register(ModMenuTypes.REINFORCED_CHEST.get(), ReinforcedChestScreen::new);
        event.register(ModMenuTypes.REMOTE.get(), RemoteScreen::new);
        event.register(ModMenuTypes.REMOTE_EDITOR.get(), RemoteEditorScreen::new);
        event.register(ModMenuTypes.ITEM_SEARCHER.get(), ItemSearcherScreen::new);
        event.register(ModMenuTypes.SECURITY_STATION_MAIN.get(), SecurityStationInventoryScreen::new);
        event.register(ModMenuTypes.SECURITY_STATION_HACKING.get(), SecurityStationHackingScreen::new);
        event.register(ModMenuTypes.SENTRY_TURRET.get(), SentryTurretScreen::new);
        event.register(ModMenuTypes.SMART_CHEST.get(), SmartChestScreen::new);
        event.register(ModMenuTypes.SOLAR_COMPRESSOR.get(), SolarCompressorScreen::new);
        event.register(ModMenuTypes.SPAWNER_EXTRACTOR.get(), SpawnerExtractorScreen::new);
        event.register(ModMenuTypes.TAG_MATCHER.get(), TagWorkbenchScreen::new);
        event.register(ModMenuTypes.THERMAL_COMPRESSOR.get(), ThermalCompressorScreen::new);
        event.register(ModMenuTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), ThermoPlantScreen::new);
        event.register(ModMenuTypes.UNIVERSAL_SENSOR.get(), UniversalSensorScreen::new);
        event.register(ModMenuTypes.UV_LIGHT_BOX.get(), UVLightBoxScreen::new);
        event.register(ModMenuTypes.VACUUM_PUMP.get(), VacuumPumpScreen::new);
        event.register(ModMenuTypes.VACUUM_TRAP.get(), VacuumTrapScreen::new);
    }

    private static void registerProgWidgetScreenFactories() {
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetArea.class, ProgWidgetAreaScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetBlockCondition.class, ProgWidgetBlockConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetBlockRightClick.class, ProgWidgetBlockRightClickScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCoordinate.class, ProgWidgetCoordinateScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCoordinateCondition.class, ProgWidgetCoordinateConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCoordinateOperator.class, ProgWidgetCoordinateOperatorScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetCrafting.class, ProgWidgetCraftingScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDig.class, ProgWidgetDigScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionItem.class, ProgWidgetDroneConditionScreen.Item::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionUpgrades.class, ProgWidgetDroneConditionScreen.Upgrades::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionFluid.class, ProgWidgetDroneConditionScreen.Fluid::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionPressure.class, ProgWidgetDroneConditionScreen.Pressure::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionEnergy.class, ProgWidgetDroneConditionScreen.Energy::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDropItem.class, ProgWidgetDropItemScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEmitRedstone.class, ProgWidgetEmitRedstoneScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityAttack.class, ProgWidgetEntityAttackScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityCondition.class, ProgWidgetConditionScreen.Entity::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityImport.class, ProgWidgetAreaShowScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityRightClick.class, ProgWidgetAreaShowScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetExternalProgram.class, ProgWidgetExternalProgramScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetForEachCoordinate.class, ProgWidgetForEachScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetForEachItem.class, ProgWidgetForEachScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetGoToLocation.class, ProgWidgetGoToLocationScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetHarvest.class, ProgWidgetHarvestScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetInventoryExport.class, ProgWidgetImportExportScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetInventoryImport.class, ProgWidgetImportExportScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetItemAssign.class, ProgWidgetItemAssignScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetItemFilter.class, ProgWidgetItemFilterScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetItemInventoryCondition.class, ProgWidgetConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLightCondition.class, ProgWidgetConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidExport.class, ProgWidgetLiquidExportScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidFilter.class, ProgWidgetLiquidFilterScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidImport.class, ProgWidgetLiquidImportScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidInventoryCondition.class, ProgWidgetConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLogistics.class, ProgWidgetAreaShowScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPickupItem.class, ProgWidgetPickupItemScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPlace.class, ProgWidgetPlaceScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetPressureCondition.class, ProgWidgetConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetRedstoneCondition.class, ProgWidgetConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEnergyCondition.class, ProgWidgetConditionScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetStandby.class, ProgWidgetStandbyScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetText.class, ProgWidgetStringScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetComment.class, ProgWidgetStringScreen::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetTeleport.class, ProgWidgetGoToLocationScreen::new);
    }

    private static void registerTubeModuleFactories() {
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_AIR_GRATE, AirGrateModuleScreen::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_GAUGE, PressureGaugeModuleScreen::createGUI);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REGULATOR, PressureGaugeModuleScreen::createGUI);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_SAFETY_VALVE, PressureGaugeModuleScreen::createGUI);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REDSTONE, RedstoneModuleScreen::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_LOGISTICS, LogisticsModuleScreen::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_THERMOSTAT, ThermostatModuleScreen::createGUI);

        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_AIR_GRATE, AirGrateRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_REDSTONE, RedstoneRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_SAFETY_VALVE, SafetyValveRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_REGULATOR, RegulatorRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_GAUGE, PressureGaugeRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_CHARGING, ChargingRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_FLOW_DETECTOR, FlowDetectorRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_LOGISTICS, LogisticsRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_VACUUM, VacuumRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_THERMOSTAT, ThermostatRenderer::new);
    }

    private static void registerArmorClientUpgradeHandlers() {
        IClientArmorRegistry cr = PneumaticRegistry.getInstance().getClientArmorRegistry();

        cr.registerUpgradeHandler(CommonUpgradeHandlers.coreComponentsHandler, new CoreComponentsClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.blockTrackerHandler, new BlockTrackerClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.entityTrackerHandler, new EntityTrackerClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.searchHandler, new SearchClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.coordTrackerHandler, new CoordTrackClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.droneDebugHandler, new DroneDebugClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.nightVisionHandler, new NightVisionClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.scubaHandler, new ScubaClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.hackHandler, new HackClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.enderVisorHandler, new EnderVisorClientHandler());

        cr.registerUpgradeHandler(CommonUpgradeHandlers.magnetHandler, new MagnetClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.chargingHandler, new ChargingClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.chestplateLauncherHandler, new ChestplateLauncherClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.airConHandler, new AirConClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.reachDistanceHandler, new ReachDistanceClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.elytraHandler, new ElytraClientHandler());

        cr.registerUpgradeHandler(CommonUpgradeHandlers.runSpeedHandler, new SpeedBoostClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.jumpBoostHandler, new JumpBoostClientHandler());

        cr.registerUpgradeHandler(CommonUpgradeHandlers.jetBootsHandler, new JetBootsClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.stepAssistHandler, new StepAssistClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.kickHandler, new KickClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.stompHandler, new StompClientHandler());
        cr.registerUpgradeHandler(CommonUpgradeHandlers.fallProtectionHandler, new FallProtectionClientHandler());
    }

    public static final IClientBlockExtensions PARTICLE_HANDLER = new IClientBlockExtensions() {
        @Override
        public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
            //Copy of ParticleManager#addBlockDestroyEffects, but removes the minimum number of particles each voxel shape produces
            state.getShape(Level, pos).forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double xDif = Math.min(1, maxX - minX);
                double yDif = Math.min(1, maxY - minY);
                double zDif = Math.min(1, maxZ - minZ);
                //Don't force the counts to be at least two
                int xCount = Mth.ceil(xDif / 0.25);
                int yCount = Mth.ceil(yDif / 0.25);
                int zCount = Mth.ceil(zDif / 0.25);
                if (xCount > 0 && yCount > 0 && zCount > 0) {
                    for (int x = 0; x < xCount; x++) {
                        for (int y = 0; y < yCount; y++) {
                            for (int z = 0; z < zCount; z++) {
                                double d4 = (x + 0.5) / xCount;
                                double d5 = (y + 0.5) / yCount;
                                double d6 = (z + 0.5) / zCount;
                                double d7 = d4 * xDif + minX;
                                double d8 = d5 * yDif + minY;
                                double d9 = d6 * zDif + minZ;
                                manager.add(new TerrainParticle((ClientLevel) Level, pos.getX() + d7, pos.getY() + d8,
                                        pos.getZ() + d9, d4 - 0.5, d5 - 0.5, d6 - 0.5, state).updateSprite(state, pos));
                            }
                        }
                    }
                }
            });
            return true;
        }
    };
}
