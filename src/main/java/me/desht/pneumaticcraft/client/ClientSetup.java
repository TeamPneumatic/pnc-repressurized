package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IClientArmorRegistry;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.programmer.*;
import me.desht.pneumaticcraft.client.gui.semiblock.LogisticsProviderScreen;
import me.desht.pneumaticcraft.client.gui.semiblock.LogisticsRequesterScreen;
import me.desht.pneumaticcraft.client.gui.semiblock.LogisticsStorageScreen;
import me.desht.pneumaticcraft.client.gui.tubemodule.AirGrateModuleScreen;
import me.desht.pneumaticcraft.client.gui.tubemodule.LogisticsModuleScreen;
import me.desht.pneumaticcraft.client.gui.tubemodule.PressureGaugeModuleScreen;
import me.desht.pneumaticcraft.client.gui.tubemodule.RedstoneModuleScreen;
import me.desht.pneumaticcraft.client.gui.tubemodule.ThermostatModuleScreen;
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
import me.desht.pneumaticcraft.common.core.*;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.item.DrillBitItem;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ClientSetup {
    public static void onModConstruction() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerGuiOverlays);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerTooltipComponentFactories);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerParticleFactories);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerRenderLayers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerLayerDefinitions);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerKeyMappings);

        MinecraftForge.EVENT_BUS.register(HUDHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(AreaRenderManager.getInstance());
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());
    }

    static void onClientSetup(FMLClientSetupEvent event) {
        EntityTrackHandler.getInstance().registerDefaultEntries();
        BlockTrackHandler.getInstance().registerDefaultEntries();
        ThirdPartyManager.instance().clientInit();

        registerProgWidgetScreenFactories();
        registerTubeModuleFactories();
        registerArmorClientUpgradeHandlers();

        event.enqueueWork(ClientSetup::initLate);
    }

    public static void registerTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MicromissilesItem.Tooltip.class, MicromissileClientTooltip::new);
    }

    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "jackhammer", new JackhammerOverlay());
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "minigun", new MinigunOverlay());
        event.registerAboveAll("pneumatic_armor", new PneumaticArmorHUDOverlay());
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

        registerScreenFactories();
        registerProgWidgetExtraRenderers();
    }

    public static void registerRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityRenderer<?> entityRenderer : Minecraft.getInstance().getEntityRenderDispatcher().renderers.values()) {
            if (entityRenderer instanceof HumanoidMobRenderer<?, ?> hmr) {
                addRenderLayer(hmr, event.getEntityModels());
            } else if (entityRenderer instanceof ArmorStandRenderer asr) {
                addRenderLayer(asr, event.getEntityModels());
            }
        }
        for (String skin : event.getSkins()) {
            LivingEntityRenderer<?, ?> render = event.getSkin(skin);
            if (render instanceof PlayerRenderer pr) {
                addRenderLayer(pr, event.getEntityModels());
                addElytraRenderLayer(pr, event.getEntityModels());
            }
        }
    }

    private static <T extends LivingEntity, M extends HumanoidModel<T>> void addElytraRenderLayer(LivingEntityRenderer<T, M> render, EntityModelSet models) {
        render.addLayer(new PneumaticElytraLayer<>(render, models));
    }

    private static <T extends LivingEntity, M extends HumanoidModel<T>> void addRenderLayer(LivingEntityRenderer<T, M> render, EntityModelSet models) {
        render.addLayer(new PneumaticArmorLayer<>(render, models));
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
        ProgWidgetRenderer.registerItemRenderer(ModProgWidgets.CRAFTING.get(), ProgWidgetRenderer::renderCraftingItem);
        ProgWidgetRenderer.registerItemRenderer(ModProgWidgets.ITEM_FILTER.get(), ProgWidgetRenderer::renderItemFilterItem);
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
        event.registerBlockEntityRenderer(ModBlockEntities.ADVANCED_LIQUID_COMPRESSOR.get(), RenderAdvancedLiquidCompressor::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ADVANCED_PRESSURE_TUBE.get(), PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AERIAL_INTERFACE.get(), AerialInterfaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AIR_CANNON.get(), AirCannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.APHORISM_TILE.get(), AphorismTileRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_CONTROLLER.get(), AssemblyControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_DRILL.get(), AssemblyDrillRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_IO_UNIT.get(), AssemblyIOUnitRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_LASER.get(), AssemblyLaserRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_PLATFORM.get(), AssemblyPlatformRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHARGING_STATION.get(), ChargingStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY_TABLE.get(), DisplayTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ELEVATOR_BASE.get(), ElevatorBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ELEVATOR_CALLER.get(), ElevatorCallerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ETCHING_TANK.get(), RenderEtchingTank::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_MIXER.get(), RenderFluidMixer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KEROSENE_LAMP.get(), RenderKeroseneLamp::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAS_LIFT.get(), RenderGasLift::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIQUID_HOPPER.get(), RenderLiquidHopper::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIQUID_COMPRESSOR.get(), RenderLiquidCompressor::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MANUAL_COMPRESSOR.get(), ManualCompressorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PNEUMATIC_DOOR.get(), PneumaticDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PNEUMATIC_DOOR_BASE.get(), PneumaticDoorBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_CHAMBER_VALVE.get(), PressureChamberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_CHAMBER_INTERFACE.get(), PressureChamberInterfaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_TUBE.get(), PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PROGRAMMER.get(), ProgrammerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFINERY.get(), RenderRefineryController::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFINERY_OUTPUT.get(), RenderRefineryOutput::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REINFORCED_PRESSURE_TUBE.get(), PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SENTRY_TURRET.get(), SentryTurretRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SPAWNER_EXTRACTOR.get(), SpawnerExtractorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TANK_SMALL.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TANK_MEDIUM.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TANK_LARGE.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TANK_HUGE.get(), RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TAG_WORKBENCH.get(), TagWorkbenchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.THERMOPNEUMATIC_PROCESSING_PLANT.get(), RenderThermopneumaticProcessingPlant::new);
        event.registerBlockEntityRenderer(ModBlockEntities.UNIVERSAL_SENSOR.get(), UniversalSensorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VACUUM_PUMP.get(), VacuumPumpRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_COMPRESSOR.get(), SolarCompressorRenderer::new);
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModMenuTypes.ADVANCED_AIR_COMPRESSOR.get(), AdvancedAirCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_LIQUID_COMPRESSOR.get(), AdvancedLiquidCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.AERIAL_INTERFACE.get(), AerialInterfaceScreen::new);
        MenuScreens.register(ModMenuTypes.AIR_CANNON.get(), AirCannonScreen::new);
        MenuScreens.register(ModMenuTypes.AIR_COMPRESSOR.get(), AirCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.AMADRON.get(), AmadronScreen::new);
        MenuScreens.register(ModMenuTypes.AMADRON_ADD_TRADE.get(), AmadronAddTradeScreen::new);
        MenuScreens.register(ModMenuTypes.ASSEMBLY_CONTROLLER.get(), AssemblyControllerScreen::new);
        MenuScreens.register(ModMenuTypes.CHARGING_STATION.get(), ChargingStationScreen::new);
        MenuScreens.register(ModMenuTypes.CHARGING_AMADRON.get(), AmadronUpgradeScreen::new);
        MenuScreens.register(ModMenuTypes.CHARGING_ARMOR.get(), PneumaticArmorUpgradeScreen::new);
        MenuScreens.register(ModMenuTypes.CHARGING_DRONE.get(), DroneUpgradeScreen::new);
        MenuScreens.register(ModMenuTypes.CHARGING_MINIGUN.get(), MinigunUpgradeScreen::new);
        MenuScreens.register(ModMenuTypes.CHARGING_JACKHAMMER.get(), JackhammerUpgradeScreen::new);
        MenuScreens.register(ModMenuTypes.CREATIVE_COMPRESSOR.get(), CreativeCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.CREATIVE_COMPRESSED_IRON_BLOCK.get(), CreativeCompressedIronBlockScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTROSTATIC_COMPRESSOR.get(), ElectrostaticCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.ELEVATOR.get(), ElevatorScreen::new);
        MenuScreens.register(ModMenuTypes.ETCHING_TANK.get(), EtchingTankScreen::new);
        MenuScreens.register(ModMenuTypes.FLUID_TANK.get(), FluidTankScreen::new);
        MenuScreens.register(ModMenuTypes.FLUID_MIXER.get(), FluidMixerScreen::new);
        MenuScreens.register(ModMenuTypes.FLUX_COMPRESSOR.get(), FluxCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.GAS_LIFT.get(), GasLiftScreen::new);
        MenuScreens.register(ModMenuTypes.INVENTORY_SEARCHER.get(), InventorySearcherScreen::new);
        MenuScreens.register(ModMenuTypes.JACKHAMMER_SETUP.get(), JackHammerSetupScreen::new);
        MenuScreens.register(ModMenuTypes.KEROSENE_LAMP.get(), KeroseneLampScreen::new);
        MenuScreens.register(ModMenuTypes.LIQUID_COMPRESSOR.get(), LiquidCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.LIQUID_HOPPER.get(), LiquidHopperScreen::new);
        MenuScreens.register(ModMenuTypes.LOGISTICS_FRAME_PROVIDER.get(), LogisticsProviderScreen::new);
        MenuScreens.register(ModMenuTypes.LOGISTICS_FRAME_REQUESTER.get(), LogisticsRequesterScreen::new);
        MenuScreens.register(ModMenuTypes.LOGISTICS_FRAME_STORAGE.get(), LogisticsStorageScreen::new);
        MenuScreens.register(ModMenuTypes.MINIGUN_MAGAZINE.get(), MinigunMagazineScreen::new);
        MenuScreens.register(ModMenuTypes.OMNIDIRECTIONAL_HOPPER.get(), OmnidirectionalHopperScreen::new);
        MenuScreens.register(ModMenuTypes.PNEUMATIC_DOOR_BASE.get(), PneumaticDoorBaseScreen::new);
        MenuScreens.register(ModMenuTypes.PNEUMATIC_DYNAMO.get(), PneumaticDynamoScreen::new);
        MenuScreens.register(ModMenuTypes.PRESSURE_CHAMBER_VALVE.get(), PressureChamberScreen::new);
        MenuScreens.register(ModMenuTypes.PRESSURE_CHAMBER_INTERFACE.get(), PressureChamberInterfaceScreen::new);
        MenuScreens.register(ModMenuTypes.PRESSURIZED_SPAWNER.get(), PressurizedSpawnerScreen::new);
        MenuScreens.register(ModMenuTypes.PROGRAMMER.get(), ProgrammerScreen::new);
        MenuScreens.register(ModMenuTypes.PROGRAMMABLE_CONTROLLER.get(), ProgrammableControllerScreen::new);
        MenuScreens.register(ModMenuTypes.REFINERY.get(), RefineryControllerScreen::new);
        MenuScreens.register(ModMenuTypes.REINFORCED_CHEST.get(), ReinforcedChestScreen::new);
        MenuScreens.register(ModMenuTypes.REMOTE.get(), RemoteScreen::new);
        MenuScreens.register(ModMenuTypes.REMOTE_EDITOR.get(), RemoteEditorScreen::new);
        MenuScreens.register(ModMenuTypes.ITEM_SEARCHER.get(), ItemSearcherScreen::new);
        MenuScreens.register(ModMenuTypes.SECURITY_STATION_MAIN.get(), SecurityStationInventoryScreen::new);
        MenuScreens.register(ModMenuTypes.SECURITY_STATION_HACKING.get(), SecurityStationHackingScreen::new);
        MenuScreens.register(ModMenuTypes.SENTRY_TURRET.get(), SentryTurretScreen::new);
        MenuScreens.register(ModMenuTypes.SMART_CHEST.get(), SmartChestScreen::new);
        MenuScreens.register(ModMenuTypes.SOLAR_COMPRESSOR.get(), SolarCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.SPAWNER_EXTRACTOR.get(), SpawnerExtractorScreen::new);
        MenuScreens.register(ModMenuTypes.TAG_MATCHER.get(), TagWorkbenchScreen::new);
        MenuScreens.register(ModMenuTypes.THERMAL_COMPRESSOR.get(), ThermalCompressorScreen::new);
        MenuScreens.register(ModMenuTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), ThermopneumaticProcessingPlantScreen::new);
        MenuScreens.register(ModMenuTypes.UNIVERSAL_SENSOR.get(), UniversalSensorScreen::new);
        MenuScreens.register(ModMenuTypes.UV_LIGHT_BOX.get(), UVLightBoxScreen::new);
        MenuScreens.register(ModMenuTypes.VACUUM_PUMP.get(), VacuumPumpScreen::new);
        MenuScreens.register(ModMenuTypes.VACUUM_TRAP.get(), VacuumTrapScreen::new);
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
}
