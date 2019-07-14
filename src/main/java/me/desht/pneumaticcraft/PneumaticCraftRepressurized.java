package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.ClientSetup;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.capabilities.Hacking;
import me.desht.pneumaticcraft.common.capabilities.Heat;
import me.desht.pneumaticcraft.common.capabilities.Pressure;
import me.desht.pneumaticcraft.common.commands.ModCommands;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.dispenser.BehaviorDispenseDrone;
import me.desht.pneumaticcraft.common.event.*;
import me.desht.pneumaticcraft.common.fluid.FluidFuelManager;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.harvesting.HarvestRegistry;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.common.network.PacketHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockInitializer;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.proxy.ClientProxy;
import me.desht.pneumaticcraft.proxy.IProxy;
import me.desht.pneumaticcraft.proxy.ServerProxy;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Names.MOD_ID)
public class PneumaticCraftRepressurized {
    public static final String MODVERSION = "@VERSION@";

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static final Logger LOGGER = LogManager.getLogger();

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public PneumaticCraftRepressurized() {
        ConfigHandler.init();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::clientSetup);
            MinecraftForge.EVENT_BUS.addListener(ClientHandler::registerRenders);
        });

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStarting);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStarted);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStopping);

        // things that can be init'ed right away (not dependent on item/block/etc. registration)
        Reflections.init();
        PneumaticRegistry.init(PneumaticCraftAPIHandler.getInstance());
        UpgradeRenderHandlerList.init();
        WidgetRegistrator.init();
        ThirdPartyManager.instance().preInit();
        Fluids.preInit();
        SemiBlockInitializer.preInit();
        proxy.preInit();
        AdvancementTriggers.registerTriggers();

        MinecraftForge.EVENT_BUS.register(new TickHandlerPneumaticCraft());
        MinecraftForge.EVENT_BUS.register(new EventHandlerPneumaticCraft());
        MinecraftForge.EVENT_BUS.register(new EventHandlerPneumaticArmor());
        MinecraftForge.EVENT_BUS.register(new EventHandlerUniversalSensor());
        MinecraftForge.EVENT_BUS.register(new DroneSpecialVariableHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigHandler());
        MinecraftForge.EVENT_BUS.register(ModItems.GPS_AREA_TOOL);
        MinecraftForge.EVENT_BUS.register(CommonArmorHandler.class);
        MinecraftForge.EVENT_BUS.register(proxy.getHackTickHandler());

        // TODO 1.14 oil lake gen?
//        if (ConfigHandler.general.oilGenerationChance > 0) {
//            GameRegistry.registerWorldGenerator(new WorldGeneratorPneumaticCraft(), 0);
//        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info(Names.MOD_NAME + " is loading!");

        registerCapabilities();
        PacketHandler.setupNetwork();
        HarvestRegistry.getInstance().init();
        Fluids.init();  // todo 1.14 will probably change
        CraftingRegistrator.init();  // todo 1.14 turn machine recipes into data packs
        HackableHandler.addDefaultEntries();
        SensorHandler.getInstance().init();

        PermissionAPI.registerNode(Names.AMADRON_ADD_PERIODIC_TRADE, DefaultPermissionLevel.OP,
                "Allow player to add a custom periodic offer via the Amadron Tablet");
        PermissionAPI.registerNode(Names.AMADRON_ADD_STATIC_TRADE, DefaultPermissionLevel.OP,
                "Allow player to add a custom static offer via the Amadron Tablet");

        DispenserBlock.registerDispenseBehavior(ModItems.DRONE, new BehaviorDispenseDrone());
        DispenserBlock.registerDispenseBehavior(ModItems.LOGISTIC_DRONE, new BehaviorDispenseDrone());
        DispenserBlock.registerDispenseBehavior(ModItems.HARVESTING_DRONE, new BehaviorDispenseDrone());

        proxy.init();

        SemiBlockManager.registerEventHandler(proxy.getClientWorld() != null);
        ThirdPartyManager.instance().init();

        // TODO 1.14 loot
//        if (ConfigHandler.general.enableDungeonLoot) {
//            LootTableList.register(RL("inject/simple_dungeon_loot"));
//        }

        // TODO 1.14 tags
//        OreDictionaryHelper.addOreDictEntries();

        // stuff to do after every other mod is done initialising
        DeferredWorkQueue.runLater(() -> {
            ConfigHandler.onPostInit();
            ModNameCache.init();
            AssemblyRecipe.calculateAssemblyChain();
            HeatBehaviourManager.getInstance().onPostInit();
            HeatExchangerManager.getInstance().onPostInit();
            FluidFuelManager.registerFuels();

            ThirdPartyManager.instance().postInit();
            proxy.postInit();
            AmadronOfferManager.getInstance().shufflePeriodicOffers();
            AmadronOfferManager.getInstance().recompileOffers();
            for (Block block : ModBlocks.Registration.ALL_BLOCKS) {
                if (block instanceof IUpgradeAcceptor) {
                    PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor((IUpgradeAcceptor) block);
                }
            }
            for (Item item : ModItems.Registration.ALL_ITEMS) {
                if (item instanceof IUpgradeAcceptor) {
                    PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor((IUpgradeAcceptor) item);
                }
            }
        });
    }

    private void registerCapabilities() {
        Hacking.register();
        Pressure.register();
        Heat.register();
    }

    private void serverStarting(FMLServerStartingEvent event) {
        ModCommands.register(event.getCommandDispatcher());
    }

    private void serverStopping(FMLServerStoppingEvent event) {
        AmadronOfferManager.getInstance().saveAll();
    }

    private void serverStarted(FMLServerStartedEvent event) {
        // TODO 1.14 fluids
//        if (ConfigHandler.general.oilGenerationChance > 0) {
//            Fluid oil = FluidRegistry.getFluid(Fluids.OIL.getName());
//            if (oil.getBlock() == null) {
//                String modName = FluidRegistry.getDefaultFluidName(oil).split(":")[0];
//                Log.error(String.format("Oil fluid does not have a block associated with it. The fluid is owned by [%s]. " +
//                        "This might be fixable by creating the world with having this mod loaded after PneumaticCraft.", modName));
//                Log.error(String.format("Now disabling PneumaticCraft oil world gen: setting 'D:oilGenerationChance=0.0' " +
//                        "in the config file pneumaticcraft.cfg.  PneumaticCraft machines should however accept the oil from [%s].", modName));
//                ConfigHandler.general.oilGenerationChance = 0;
//                ConfigHandler.sync();
//            }
//        }
    }

    static class ClientHandler {
        static void clientSetup(FMLClientSetupEvent event) {
            ClientSetup.init();
        }

        static void registerRenders(ModelRegistryEvent event) {
            // todo 1.14 what do we need here?
        }
    }

}
