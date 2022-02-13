package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.*;
import me.desht.pneumaticcraft.client.gui.charging.*;
import me.desht.pneumaticcraft.client.gui.programmer.*;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsProvider;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsRequester;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsStorage;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiAirGrateModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiLogisticsModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiPressureModule;
import me.desht.pneumaticcraft.client.gui.tubemodule.GuiRedstoneModule;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDroneCore;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.*;
import me.desht.pneumaticcraft.client.particle.AirParticle;
import me.desht.pneumaticcraft.client.particle.BulletParticle;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
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
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.*;
import me.desht.pneumaticcraft.client.render.tube_module.*;
import me.desht.pneumaticcraft.client.sound.MovingSoundJackhammer;
import me.desht.pneumaticcraft.client.util.ProgWidgetRenderer;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.core.*;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.item.ItemDrillBit;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ClientSetup {
    public static void initEarly() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerParticleFactories);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerRenderLayers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerLayerDefinitions);
    }

    static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(HUDHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(HackTickHandler.instance());
        MinecraftForge.EVENT_BUS.register(AreaRenderManager.getInstance());
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());

        EntityTrackHandler.registerDefaultEntries();
        ThirdPartyManager.instance().clientInit();

        registerProgWidgetScreenFactories();
        registerTubeModuleFactories();

        OverlayRegistry.registerOverlayTop("jackhammer", new JackhammerOverlay());
        OverlayRegistry.registerOverlayTop("pneumatic_armor", new PneumaticArmorHUDOverlay());
        OverlayRegistry.registerOverlayTop("minigun", new MinigunOverlay());

        MinecraftForgeClient.registerTooltipComponentFactory(ItemMicromissiles.Tooltip.class, MicromissileClientTooltip::new);

        event.enqueueWork(ClientSetup::initLate);
    }

    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticleTypes.AIR_PARTICLE.get(), AirParticle.Factory::new);
        Minecraft.getInstance().particleEngine.register(ModParticleTypes.AIR_PARTICLE_2.get(), AirParticle.Factory::new);
        Minecraft.getInstance().particleEngine.register(ModParticleTypes.BULLET_PARTICLE.get(), BulletParticle.Factory::new);
    }

    public static void initLate() {
        // stuff to do on the main thread
        setBlockRenderLayers();
        registerItemModelProperties();
        registerArmorClientUpgradeHandlers();
        registerScreenFactories();
        registerProgWidgetExtraRenderers();

        EntityTrackHandler.init();
    }

    public static void registerRenderLayers(EntityRenderersEvent.AddLayers event) {
        for(EntityRenderer<?> render : Minecraft.getInstance().getEntityRenderDispatcher().renderers.values()) {
            if (render instanceof HumanoidMobRenderer<?, ?> hmr) {
                addRenderLayer(hmr, event.getEntityModels());
            } else if (render instanceof ArmorStandRenderer asr) {
                addRenderLayer(asr, event.getEntityModels());
            }
        }
        for (String skin : event.getSkins()) {
            LivingEntityRenderer<?, ?> render = event.getSkin(skin);
            if (render instanceof PlayerRenderer pr) addRenderLayer(pr, event.getEntityModels());
        }
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
        event.registerLayerDefinition(PNCModelLayers.PRESSURE_CHAMBER_INTERFACE, PressureChamberInterfaceRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SPAWNER_EXTRACTOR, SpawnerExtractorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SPAWNER_EXTRACTOR, SpawnerExtractorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.UNIVERSAL_SENSOR, UniversalSensorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.VACUUM_PUMP, VacuumPumpRenderer::createBodyLayer);

        // tube modules
        event.registerLayerDefinition(PNCModelLayers.AIR_GRATE_MODULE, AirGrateRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.CHARGING_MODULE, ChargingRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.FLOW_DETECTOR_MODULE, FlowDetectorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.LOGISTICS_MODULE, LogisticsRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PRESSURE_GAUGE_MODULE, PressureGaugeRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.REDSTONE_MODULE, RedstoneRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.REGULATOR_MODULE, RegulatorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SAFETY_VALVE_MODULE, SafetyValveRenderer::createBodyLayer);
    }

    private static void registerItemModelProperties() {
        ItemProperties.register(ModItems.JACKHAMMER.get(), RL("drill_bit"), (stack, world, entity, seed) -> {
            ItemDrillBit.DrillBitType type = ((ItemJackHammer) stack.getItem()).getDrillBit(stack);
            if (type == ItemDrillBit.DrillBitType.NONE) return 0f;
            if (world == null || !(entity instanceof Player)) return 0.99f;
            long l = MovingSoundJackhammer.lastJackHammerTime((Player) entity);
            if (l <= 20) return Mth.sin((world.getGameTime() % 4 / 4f) * 3.141529f);
            else return 0.99f;
        });
    }

    private static void registerProgWidgetExtraRenderers() {
        ProgWidgetRenderer.registerExtraRenderer(ModProgWidgets.CRAFTING.get(), ProgWidgetRenderer::renderCraftingExtras);
        ProgWidgetRenderer.registerExtraRenderer(ModProgWidgets.ITEM_FILTER.get(), ProgWidgetRenderer::renderItemFilterExtras);
    }

    private static void setBlockRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ADVANCED_LIQUID_COMPRESSOR.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.APHORISM_TILE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELEVATOR_FRAME.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.EMPTY_SPAWNER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ETCHING_TANK.get(), layer -> layer == RenderType.solid() || layer == RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_MIXER.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GAS_LIFT.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.KEROSENE_LAMP.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIQUID_HOPPER.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIQUID_COMPRESSOR.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PRESSURE_TUBE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PRESSURIZED_SPAWNER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.REFINERY.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.REFINERY_OUTPUT.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TANK_SMALL.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TANK_MEDIUM.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TANK_LARGE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TANK_HUGE.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.VACUUM_PUMP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.UV_LIGHT_BOX.get(), layer -> layer == RenderType.cutoutMipped() || layer == RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.THERMAL_LAGGING.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPAWNER_EXTRACTOR.get(), RenderType.cutoutMipped());

        // camouflageable blocks need to render in all layers, since their camo could render in any layer
        for (RegistryObject<Block> ro: ModBlocks.BLOCKS.getEntries()) {
            if (ro.get() instanceof AbstractCamouflageBlock) {
                ItemBlockRenderTypes.setRenderLayer(ro.get(), r -> true);
            }
        }
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
        event.registerBlockEntityRenderer(ModBlockEntities.PNEUMATIC_DOOR.get(), PneumaticDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PNEUMATIC_DOOR_BASE.get(), PneumaticDoorBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_CHAMBER_VALVE.get(), PressureChamberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_CHAMBER_INTERFACE.get(), PressureChamberInterfaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESSURE_TUBE.get(), PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PROGRAMMER.get(), ProgrammerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFINERY.get(), RenderRefineryController::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFINERY_OUTPUT.get(), RenderRefineryOutput::new);
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
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModMenuTypes.ADVANCED_AIR_COMPRESSOR.get(), GuiAdvancedAirCompressor::new);
        MenuScreens.register(ModMenuTypes.ADVANCED_LIQUID_COMPRESSOR.get(), GuiAdvancedLiquidCompressor::new);
        MenuScreens.register(ModMenuTypes.AERIAL_INTERFACE.get(), GuiAerialInterface::new);
        MenuScreens.register(ModMenuTypes.AIR_CANNON.get(), GuiAirCannon::new);
        MenuScreens.register(ModMenuTypes.AIR_COMPRESSOR.get(), GuiAirCompressor::new);
        MenuScreens.register(ModMenuTypes.AMADRON.get(), GuiAmadron::new);
        MenuScreens.register(ModMenuTypes.AMADRON_ADD_TRADE.get(), GuiAmadronAddTrade::new);
        MenuScreens.register(ModMenuTypes.ASSEMBLY_CONTROLLER.get(), GuiAssemblyController::new);
        MenuScreens.register(ModMenuTypes.CHARGING_STATION.get(), GuiChargingStation::new);
        MenuScreens.register(ModMenuTypes.CHARGING_AMADRON.get(), GuiAmadronCharging::new);
        MenuScreens.register(ModMenuTypes.CHARGING_ARMOR.get(), GuiPneumaticArmor::new);
        MenuScreens.register(ModMenuTypes.CHARGING_DRONE.get(), GuiDroneCharging::new);
        MenuScreens.register(ModMenuTypes.CHARGING_MINIGUN.get(), GuiMinigunCharging::new);
        MenuScreens.register(ModMenuTypes.CHARGING_JACKHAMMER.get(), GuiJackhammerCharging::new);
        MenuScreens.register(ModMenuTypes.CREATIVE_COMPRESSOR.get(), GuiCreativeCompressor::new);
        MenuScreens.register(ModMenuTypes.CREATIVE_COMPRESSED_IRON_BLOCK.get(), GuiCreativeCompressedIronBlock::new);
        MenuScreens.register(ModMenuTypes.ELECTROSTATIC_COMPRESSOR.get(), GuiElectrostaticCompressor::new);
        MenuScreens.register(ModMenuTypes.ELEVATOR.get(), GuiElevator::new);
        MenuScreens.register(ModMenuTypes.ETCHING_TANK.get(), GuiEtchingTank::new);
        MenuScreens.register(ModMenuTypes.FLUID_TANK.get(), GuiFluidTank::new);
        MenuScreens.register(ModMenuTypes.FLUID_MIXER.get(), GuiFluidMixer::new);
        MenuScreens.register(ModMenuTypes.FLUX_COMPRESSOR.get(), GuiFluxCompressor::new);
        MenuScreens.register(ModMenuTypes.GAS_LIFT.get(), GuiGasLift::new);
        MenuScreens.register(ModMenuTypes.INVENTORY_SEARCHER.get(), GuiInventorySearcher::new);
        MenuScreens.register(ModMenuTypes.JACKHAMMER_SETUP.get(), GuiJackHammerSetup::new);
        MenuScreens.register(ModMenuTypes.KEROSENE_LAMP.get(), GuiKeroseneLamp::new);
        MenuScreens.register(ModMenuTypes.LIQUID_COMPRESSOR.get(), GuiLiquidCompressor::new);
        MenuScreens.register(ModMenuTypes.LIQUID_HOPPER.get(), GuiLiquidHopper::new);
        MenuScreens.register(ModMenuTypes.LOGISTICS_FRAME_PROVIDER.get(), GuiLogisticsProvider::new);
        MenuScreens.register(ModMenuTypes.LOGISTICS_FRAME_REQUESTER.get(), GuiLogisticsRequester::new);
        MenuScreens.register(ModMenuTypes.LOGISTICS_FRAME_STORAGE.get(), GuiLogisticsStorage::new);
        MenuScreens.register(ModMenuTypes.MINIGUN_MAGAZINE.get(), GuiMinigunMagazine::new);
        MenuScreens.register(ModMenuTypes.OMNIDIRECTIONAL_HOPPER.get(), GuiOmnidirectionalHopper::new);
        MenuScreens.register(ModMenuTypes.PNEUMATIC_DOOR_BASE.get(), GuiPneumaticDoorBase::new);
        MenuScreens.register(ModMenuTypes.PNEUMATIC_DYNAMO.get(), GuiPneumaticDynamo::new);
        MenuScreens.register(ModMenuTypes.PRESSURE_CHAMBER_VALVE.get(), GuiPressureChamber::new);
        MenuScreens.register(ModMenuTypes.PRESSURE_CHAMBER_INTERFACE.get(), GuiPressureChamberInterface::new);
        MenuScreens.register(ModMenuTypes.PRESSURIZED_SPAWNER.get(), GuiPressurizedSpawner::new);
        MenuScreens.register(ModMenuTypes.PROGRAMMER.get(), GuiProgrammer::new);
        MenuScreens.register(ModMenuTypes.PROGRAMMABLE_CONTROLLER.get(), GuiProgrammableController::new);
        MenuScreens.register(ModMenuTypes.REFINERY.get(), GuiRefineryController::new);
        MenuScreens.register(ModMenuTypes.REINFORCED_CHEST.get(), GuiReinforcedChest::new);
        MenuScreens.register(ModMenuTypes.REMOTE.get(), GuiRemote::new);
        MenuScreens.register(ModMenuTypes.REMOTE_EDITOR.get(), GuiRemoteEditor::new);
        MenuScreens.register(ModMenuTypes.ITEM_SEARCHER.get(), GuiItemSearcher::new);
        MenuScreens.register(ModMenuTypes.SECURITY_STATION_MAIN.get(), GuiSecurityStationInventory::new);
        MenuScreens.register(ModMenuTypes.SECURITY_STATION_HACKING.get(), GuiSecurityStationHacking::new);
        MenuScreens.register(ModMenuTypes.SENTRY_TURRET.get(), GuiSentryTurret::new);
        MenuScreens.register(ModMenuTypes.SMART_CHEST.get(), GuiSmartChest::new);
        MenuScreens.register(ModMenuTypes.SPAWNER_EXTRACTOR.get(), GuiSpawnerExtractor::new);
        MenuScreens.register(ModMenuTypes.TAG_MATCHER.get(), GuiTagWorkbench::new);
        MenuScreens.register(ModMenuTypes.THERMAL_COMPRESSOR.get(), GuiThermalCompressor::new);
        MenuScreens.register(ModMenuTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), GuiThermopneumaticProcessingPlant::new);
        MenuScreens.register(ModMenuTypes.UNIVERSAL_SENSOR.get(), GuiUniversalSensor::new);
        MenuScreens.register(ModMenuTypes.UV_LIGHT_BOX.get(), GuiUVLightBox::new);
        MenuScreens.register(ModMenuTypes.VACUUM_PUMP.get(), GuiVacuumPump::new);
        MenuScreens.register(ModMenuTypes.VACUUM_TRAP.get(), GuiVacuumTrap::new);
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
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionUpgrades.class, GuiProgWidgetDroneCondition.Upgrades::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionFluid.class, GuiProgWidgetDroneCondition.Fluid::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionPressure.class, GuiProgWidgetDroneCondition.Pressure::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDroneConditionEnergy.class, GuiProgWidgetDroneCondition.Energy::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetDropItem.class, GuiProgWidgetDropItem::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEmitRedstone.class, GuiProgWidgetEmitRedstone::new);
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetEntityAttack.class, GuiProgWidgetEntityAttack::new);
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
        ProgWidgetGuiManager.registerProgWidgetGui(ProgWidgetLiquidImport.class, GuiProgWidgetLiquidImport::new);
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
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_GAUGE, GuiPressureModule::createGUI);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REGULATOR, GuiPressureModule::createGUI);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_SAFETY_VALVE, GuiPressureModule::createGUI);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_REDSTONE, GuiRedstoneModule::new);
        TubeModuleClientRegistry.registerTubeModuleGUI(Names.MODULE_LOGISTICS, GuiLogisticsModule::new);

        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_AIR_GRATE, AirGrateRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_REDSTONE, RedstoneRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_SAFETY_VALVE, SafetyValveRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_REGULATOR, RegulatorRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_GAUGE, PressureGaugeRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_CHARGING, ChargingRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_FLOW_DETECTOR, FlowDetectorRenderer::new);
        TubeModuleClientRegistry.registerTubeModuleRenderer(Names.MODULE_LOGISTICS, LogisticsRenderer::new);
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
        cr.registerHandler(r.enderVisorHandler, new EnderVisorClientHandler());

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
}
