package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.*;
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
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticArmorLayer;
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
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ClientSetup {
    public static void initEarly() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerParticleFactories);
    }

    static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(HUDHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(HackTickHandler.instance());
        MinecraftForge.EVENT_BUS.register(AreaRenderManager.getInstance());
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());

        registerEntityRenderers();

        EntityTrackHandler.registerDefaultEntries();
        ThirdPartyManager.instance().clientInit();

        event.enqueueWork(ClientSetup::initLate);
    }

    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticleTypes.AIR_PARTICLE.get(), AirParticle.Factory::new);
        Minecraft.getInstance().particleEngine.register(ModParticleTypes.AIR_PARTICLE_2.get(), AirParticle.Factory::new);
    }

    public static void initLate() {
        // stuff to do on the main thread
        setBlockRenderLayers();
        addCustomArmorLayer();
        registerItemModelProperties();
        registerArmorClientUpgradeHandlers();
        registerTileEntityRenderers();
        registerScreenFactories();
        registerProgWidgetScreenFactories();
        registerProgWidgetExtraRenderers();
        registerTubeModuleFactories();

        EntityTrackHandler.init();
    }

    private static void addCustomArmorLayer() {
        Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
        PlayerRenderer render;
        render = skinMap.get("default");
        render.addLayer(new PneumaticArmorLayer<>(render, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        render = skinMap.get("slim");
        render.addLayer(new PneumaticArmorLayer<>(render, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));

        EntityRenderer<?> r = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(EntityType.ARMOR_STAND);
        if (r instanceof ArmorStandRenderer) {
            ArmorStandRenderer ar = (ArmorStandRenderer) r;
            ar.addLayer(new PneumaticArmorLayer<>(ar, new ArmorStandArmorModel(0.5F), new ArmorStandArmorModel(1.0F)));
        }
    }

    private static void registerItemModelProperties() {
        ItemModelsProperties.register(ModItems.JACKHAMMER.get(), RL("drill_bit"), (stack, world, entity) -> {
            ItemDrillBit.DrillBitType type = ((ItemJackHammer) stack.getItem()).getDrillBit(stack);
            if (type == ItemDrillBit.DrillBitType.NONE) return 0f;
            if (world == null || !(entity instanceof PlayerEntity)) return 0.99f;
            long l = MovingSoundJackhammer.lastJackHammerTime((PlayerEntity) entity);
            if (l <= 20) return MathHelper.sin((world.getGameTime() % 4 / 4f) * 3.141529f);
            else return 0.99f;
        });
    }

    private static void registerProgWidgetExtraRenderers() {
        ProgWidgetRenderer.registerExtraRenderer(ModProgWidgets.CRAFTING.get(), ProgWidgetRenderer::renderCraftingExtras);
        ProgWidgetRenderer.registerExtraRenderer(ModProgWidgets.ITEM_FILTER.get(), ProgWidgetRenderer::renderItemFilterExtras);
    }

    private static void setBlockRenderLayers() {
        RenderTypeLookup.setRenderLayer(ModBlocks.APHORISM_TILE.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.ELEVATOR_FRAME.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.EMPTY_SPAWNER.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.ETCHING_TANK.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.FLUID_MIXER.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.KEROSENE_LAMP.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.LIQUID_HOPPER.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.PRESSURE_CHAMBER_GLASS.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.PRESSURE_TUBE.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.PRESSURIZED_SPAWNER.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.REFINERY.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.REFINERY_OUTPUT.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.TANK_SMALL.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.TANK_MEDIUM.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.TANK_LARGE.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.TANK_HUGE.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.VACUUM_PUMP.get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(ModBlocks.UV_LIGHT_BOX.get(), layer -> layer == RenderType.cutoutMipped() || layer == RenderType.translucent());
        RenderTypeLookup.setRenderLayer(ModBlocks.THERMAL_LAGGING.get(), RenderType.translucent());

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
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.AIR_CANNON.get(), RenderAirCannon::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.AERIAL_INTERFACE.get(), RenderAerialInterface::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.APHORISM_TILE.get(), RenderAphorismTile::new);
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
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.REFINERY.get(), RenderRefineryController::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.REFINERY_OUTPUT.get(), RenderRefineryOutput::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.SENTRY_TURRET.get(), RenderSentryTurret::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.SPAWNER_EXTRACTOR.get(), RenderSpawnerExtractor::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TANK_SMALL.get(), RenderFluidTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TANK_MEDIUM.get(), RenderFluidTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TANK_LARGE.get(), RenderFluidTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TANK_HUGE.get(), RenderFluidTank::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.TAG_WORKBENCH.get(), RenderTagWorkbench::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.THERMOPNEUMATIC_PROCESSING_PLANT.get(), RenderThermopneumaticProcessingPlant::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.UNIVERSAL_SENSOR.get(), RenderUniversalSensor::new);
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.VACUUM_PUMP.get(), RenderVacuumPump::new);
    }

    private static void registerScreenFactories() {
        ScreenManager.register(ModContainers.ADVANCED_AIR_COMPRESSOR.get(), GuiAdvancedAirCompressor::new);
        ScreenManager.register(ModContainers.ADVANCED_LIQUID_COMPRESSOR.get(), GuiAdvancedLiquidCompressor::new);
        ScreenManager.register(ModContainers.AERIAL_INTERFACE.get(), GuiAerialInterface::new);
        ScreenManager.register(ModContainers.AIR_CANNON.get(), GuiAirCannon::new);
        ScreenManager.register(ModContainers.AIR_COMPRESSOR.get(), GuiAirCompressor::new);
        ScreenManager.register(ModContainers.AMADRON.get(), GuiAmadron::new);
        ScreenManager.register(ModContainers.AMADRON_ADD_TRADE.get(), GuiAmadronAddTrade::new);
        ScreenManager.register(ModContainers.ASSEMBLY_CONTROLLER.get(), GuiAssemblyController::new);
        ScreenManager.register(ModContainers.CHARGING_STATION.get(), GuiChargingStation::new);
        ScreenManager.register(ModContainers.CHARGING_ARMOR.get(), GuiPneumaticArmor::new);
        ScreenManager.register(ModContainers.CHARGING_DRONE.get(), GuiDrone::new);
        ScreenManager.register(ModContainers.CHARGING_MINIGUN.get(), GuiMinigun::new);
        ScreenManager.register(ModContainers.CHARGING_JACKHAMMER.get(), GuiJackhammer::new);
        ScreenManager.register(ModContainers.CREATIVE_COMPRESSOR.get(), GuiCreativeCompressor::new);
        ScreenManager.register(ModContainers.CREATIVE_COMPRESSED_IRON_BLOCK.get(), GuiCreativeCompressedIronBlock::new);
        ScreenManager.register(ModContainers.ELECTROSTATIC_COMPRESSOR.get(), GuiElectrostaticCompressor::new);
        ScreenManager.register(ModContainers.ELEVATOR.get(), GuiElevator::new);
        ScreenManager.register(ModContainers.ETCHING_TANK.get(), GuiEtchingTank::new);
        ScreenManager.register(ModContainers.FLUID_TANK.get(), GuiFluidTank::new);
        ScreenManager.register(ModContainers.FLUID_MIXER.get(), GuiFluidMixer::new);
        ScreenManager.register(ModContainers.FLUX_COMPRESSOR.get(), GuiFluxCompressor::new);
        ScreenManager.register(ModContainers.GAS_LIFT.get(), GuiGasLift::new);
        ScreenManager.register(ModContainers.INVENTORY_SEARCHER.get(), GuiInventorySearcher::new);
        ScreenManager.register(ModContainers.JACKHAMMER_SETUP.get(), GuiJackHammerSetup::new);
        ScreenManager.register(ModContainers.KEROSENE_LAMP.get(), GuiKeroseneLamp::new);
        ScreenManager.register(ModContainers.LIQUID_COMPRESSOR.get(), GuiLiquidCompressor::new);
        ScreenManager.register(ModContainers.LIQUID_HOPPER.get(), GuiLiquidHopper::new);
        ScreenManager.register(ModContainers.LOGISTICS_FRAME_PROVIDER.get(), GuiLogisticsProvider::new);
        ScreenManager.register(ModContainers.LOGISTICS_FRAME_REQUESTER.get(), GuiLogisticsRequester::new);
        ScreenManager.register(ModContainers.LOGISTICS_FRAME_STORAGE.get(), GuiLogisticsStorage::new);
        ScreenManager.register(ModContainers.MINIGUN_MAGAZINE.get(), GuiMinigunMagazine::new);
        ScreenManager.register(ModContainers.OMNIDIRECTIONAL_HOPPER.get(), GuiOmnidirectionalHopper::new);
        ScreenManager.register(ModContainers.PNEUMATIC_DOOR_BASE.get(), GuiPneumaticDoorBase::new);
        ScreenManager.register(ModContainers.PNEUMATIC_DYNAMO.get(), GuiPneumaticDynamo::new);
        ScreenManager.register(ModContainers.PRESSURE_CHAMBER_VALVE.get(), GuiPressureChamber::new);
        ScreenManager.register(ModContainers.PRESSURE_CHAMBER_INTERFACE.get(), GuiPressureChamberInterface::new);
        ScreenManager.register(ModContainers.PRESSURIZED_SPAWNER.get(), GuiPressurizedSpawner::new);
        ScreenManager.register(ModContainers.PROGRAMMER.get(), GuiProgrammer::new);
        ScreenManager.register(ModContainers.PROGRAMMABLE_CONTROLLER.get(), GuiProgrammableController::new);
        ScreenManager.register(ModContainers.REFINERY.get(), GuiRefineryController::new);
        ScreenManager.register(ModContainers.REINFORCED_CHEST.get(), GuiReinforcedChest::new);
        ScreenManager.register(ModContainers.REMOTE.get(), GuiRemote::new);
        ScreenManager.register(ModContainers.REMOTE_EDITOR.get(), GuiRemoteEditor::new);
        ScreenManager.register(ModContainers.ITEM_SEARCHER.get(), GuiItemSearcher::new);
        ScreenManager.register(ModContainers.SECURITY_STATION_MAIN.get(), GuiSecurityStationInventory::new);
        ScreenManager.register(ModContainers.SECURITY_STATION_HACKING.get(), GuiSecurityStationHacking::new);
        ScreenManager.register(ModContainers.SENTRY_TURRET.get(), GuiSentryTurret::new);
        ScreenManager.register(ModContainers.SMART_CHEST.get(), GuiSmartChest::new);
        ScreenManager.register(ModContainers.SPAWNER_EXTRACTOR.get(), GuiSpawnerExtractor::new);
        ScreenManager.register(ModContainers.TAG_MATCHER.get(), GuiTagWorkbench::new);
        ScreenManager.register(ModContainers.THERMAL_COMPRESSOR.get(), GuiThermalCompressor::new);
        ScreenManager.register(ModContainers.THERMOPNEUMATIC_PROCESSING_PLANT.get(), GuiThermopneumaticProcessingPlant::new);
        ScreenManager.register(ModContainers.UNIVERSAL_SENSOR.get(), GuiUniversalSensor::new);
        ScreenManager.register(ModContainers.UV_LIGHT_BOX.get(), GuiUVLightBox::new);
        ScreenManager.register(ModContainers.VACUUM_PUMP.get(), GuiVacuumPump::new);
        ScreenManager.register(ModContainers.VACUUM_TRAP.get(), GuiVacuumTrap::new);
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
}
