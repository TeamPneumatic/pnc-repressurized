package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.CreativeTabPneumaticCraft;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.GuiHandler;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.capabilities.hacking.HackingImpl;
import me.desht.pneumaticcraft.common.commands.PCCommandManager;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.dispenser.BehaviorDispenseDrone;
import me.desht.pneumaticcraft.common.event.*;
import me.desht.pneumaticcraft.common.fluid.FluidFuelManager;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.harvesting.HarvestRegistry;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.CraftingHandler;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockInitializer;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtils;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRegistrator;
import me.desht.pneumaticcraft.common.util.OreDictionaryHelper;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.common.worldgen.WorldGeneratorPneumaticCraft;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Versions;
import me.desht.pneumaticcraft.proxy.IProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod(modid = Names.MOD_ID, name = Names.MOD_NAME, version = PneumaticCraftRepressurized.MODVERSION,
        dependencies = "required-after:forge@[14.23.5.2768,);after:forestry;after:igwmod@[1.4.2-11,);after:thaumcraft;after:computercraft;after:appliedenergistics2@[rv6,];after:jei@[4.12.0,)",
        updateJSON = "https://raw.github.com/TeamPneumatic/pnc-repressurized/master/release_info.json",
        acceptedMinecraftVersions = "1.12"
)
public class PneumaticCraftRepressurized {
    public static final String MODVERSION = "@VERSION@";

    @SidedProxy(clientSide = "me.desht.pneumaticcraft.proxy.ClientProxy", serverSide = "me.desht.pneumaticcraft.proxy.ServerProxy")
    public static IProxy proxy;

    @Instance(Names.MOD_ID)
    public static PneumaticCraftRepressurized instance;

    public static Logger logger;

    public static CreativeTabPneumaticCraft tabPneumaticCraft;
    public static GuiHandler guiHandler;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        event.getModMetadata().version = Versions.fullVersionString();

        Reflections.init();
        PneumaticRegistry.init(PneumaticCraftAPIHandler.getInstance());
        UpgradeRenderHandlerList.init();
        HarvestRegistry.getInstance().init();
        ConfigHandler.onPreInit(event.getSuggestedConfigurationFile());

        guiHandler = new GuiHandler();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, guiHandler);

        tabPneumaticCraft = new CreativeTabPneumaticCraft("tabPneumaticCraft");

        Fluids.preInit();

        ThirdPartyManager.instance().index();
        ThirdPartyManager.instance().preInit();

        WidgetRegistrator.init();
        TileEntityRegistrator.init();
        SemiBlockInitializer.preInit();
        if (ConfigHandler.general.oilGenerationChance > 0) {
            GameRegistry.registerWorldGenerator(new WorldGeneratorPneumaticCraft(), 0);
        }

        proxy.preInit();

        CapabilityManager.INSTANCE.register(IHacking.class, new HackingImpl.Storage(), HackingImpl::new);
        AdvancementTriggers.registerTriggers();

        MinecraftForge.EVENT_BUS.register(new TickHandlerPneumaticCraft());
        MinecraftForge.EVENT_BUS.register(new EventHandlerPneumaticCraft());
        MinecraftForge.EVENT_BUS.register(new EventHandlerPneumaticArmor());
        MinecraftForge.EVENT_BUS.register(new EventHandlerUniversalSensor());
        MinecraftForge.EVENT_BUS.register(new DroneSpecialVariableHandler());
        MinecraftForge.EVENT_BUS.register(new CraftingHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigHandler());
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        NetworkHandler.init();

        Fluids.init();
        CraftingRegistrator.init();
        HackableHandler.addDefaultEntries();
        SensorHandler.getInstance().init();

        PermissionAPI.registerNode(Names.AMADRON_ADD_PERIODIC_TRADE, DefaultPermissionLevel.OP,
                "Allow player to add a custom periodic offer via the Amadron Tablet");
        PermissionAPI.registerNode(Names.AMADRON_ADD_STATIC_TRADE, DefaultPermissionLevel.OP,
                "Allow player to add a custom static offer via the Amadron Tablet");

        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Itemss.DRONE, new BehaviorDispenseDrone());
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Itemss.LOGISTICS_DRONE, new BehaviorDispenseDrone());
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Itemss.HARVESTING_DRONE, new BehaviorDispenseDrone());

        if (ConfigHandler.general.enableDungeonLoot) {
            LootTableList.register(RL("inject/simple_dungeon_loot"));
        }

        OreDictionaryHelper.addOreDictEntries();

        MinecraftForge.EVENT_BUS.register(Itemss.GPS_AREA_TOOL);
        MinecraftForge.EVENT_BUS.register(CommonHUDHandler.class);
        MinecraftForge.EVENT_BUS.register(proxy.getHackTickHandler());

        proxy.init();

        SemiBlockManager.registerEventHandler(proxy.getClientWorld() != null);
        ThirdPartyManager.instance().init();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        ServerCommandManager comManager = (ServerCommandManager) event.getServer().getCommandManager();
        new PCCommandManager().init(comManager);
    }

    @EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
        AmadronOfferManager.getInstance().saveAll();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Add these later so we include other mods' storage recipes.
//         CraftingRegistrator.addPressureChamberStorageBlockRecipes();
        ConfigHandler.onPostInit();

        AssemblyRecipe.calculateAssemblyChain();
        HeatBehaviourManager.getInstance().onPostInit();
        HeatExchangerManager.getInstance().onPostInit();
        FluidFuelManager.registerFuels();

        ThirdPartyManager.instance().postInit();
        proxy.postInit();
        AmadronOfferManager.getInstance().shufflePeriodicOffers();
        AmadronOfferManager.getInstance().recompileOffers();
        ModInteractionUtils.registerThirdPartyWrenches();

        for (Block block : Blockss.blocks) {
            if (block instanceof IUpgradeAcceptor) {
                PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor((IUpgradeAcceptor) block);
            }
        }
        for (Item item : Itemss.items) {
            if (item instanceof IUpgradeAcceptor) {
                PneumaticRegistry.getInstance().getItemRegistry().registerUpgradeAcceptor((IUpgradeAcceptor) item);
            }
        }
    }

    @EventHandler
    public void validateFluids(FMLServerStartedEvent event) {
        if (ConfigHandler.general.oilGenerationChance > 0) {
            Fluid oil = FluidRegistry.getFluid(Fluids.OIL.getName());
            if (oil.getBlock() == null) {
                String modName = FluidRegistry.getDefaultFluidName(oil).split(":")[0];
                Log.error(String.format("Oil fluid does not have a block associated with it. The fluid is owned by [%s]. " +
                        "This might be fixable by creating the world with having this mod loaded after PneumaticCraft.", modName));
                Log.error(String.format("Now disabling PneumaticCraft oil world gen: setting 'D:oilGenerationChance=0.0' " +
                        "in the config file pneumaticcraft.cfg.  PneumaticCraft machines should however accept the oil from [%s].", modName));
                ConfigHandler.general.oilGenerationChance = 0;
                ConfigHandler.sync();
            }
        }
    }
}
