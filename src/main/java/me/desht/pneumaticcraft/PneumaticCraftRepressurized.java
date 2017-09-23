package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.CreativeTabPneumaticCraft;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.EventHandlerPneumaticCraft;
import me.desht.pneumaticcraft.common.EventHandlerUniversalSensor;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.TickHandlerPneumaticCraft;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.commands.PCCommandManager;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.entity.EntityRegistrator;
import me.desht.pneumaticcraft.common.event.DroneSpecialVariableHandler;
import me.desht.pneumaticcraft.common.fluid.FluidFuelManager;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.CraftingHandler;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockInitializer;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRegistrator;
import me.desht.pneumaticcraft.common.util.OreDictionaryHelper;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.common.worldgen.WorldGeneratorPneumaticCraft;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Versions;
import me.desht.pneumaticcraft.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Names.MOD_ID, name = "PneumaticCraft: Repressurized", version = PneumaticCraftRepressurized.MODVERSION,
        dependencies = "required-after:forge@[14.22.0.2474,);" + "after:forestry",
        acceptedMinecraftVersions = "1.12"
)
public class PneumaticCraftRepressurized {
    public static final String MODVERSION = "@VERSION@";

    @SidedProxy(clientSide = "me.desht.pneumaticcraft.proxy.ClientProxy", serverSide = "me.desht.pneumaticcraft.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Instance(Names.MOD_ID)
    public static PneumaticCraftRepressurized instance;

    public static Logger logger;

    public static TickHandlerPneumaticCraft tickHandler;
    public static CreativeTabPneumaticCraft tabPneumaticCraft;

    public static boolean isNEIInstalled;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        event.getModMetadata().version = Versions.fullVersionString();
        isNEIInstalled = Loader.isModLoaded(ModIds.NEI);

        Reflections.init();

        PneumaticRegistry.init(PneumaticCraftAPIHandler.getInstance());
        UpgradeRenderHandlerList.init();
        WidgetRegistrator.init();
        ConfigHandler.init(event.getSuggestedConfigurationFile());
        ThirdPartyManager.instance().index();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        tabPneumaticCraft = new CreativeTabPneumaticCraft("tabPneumaticCraft");
        Fluids.preInit();
        ThirdPartyManager.instance().preInit();
        TileEntityRegistrator.init();
        EntityRegistrator.init();
        SemiBlockInitializer.preInit();
        //TODO 1.8 fix  VillagerHandler.instance().init();
        GameRegistry.registerWorldGenerator(new WorldGeneratorPneumaticCraft(), 0);
        HeatBehaviourManager.getInstance().init();

        proxy.preInit();
        tickHandler = new TickHandlerPneumaticCraft();
        MinecraftForge.EVENT_BUS.register(tickHandler);
        MinecraftForge.EVENT_BUS.register(new EventHandlerPneumaticCraft());
        MinecraftForge.EVENT_BUS.register(new EventHandlerUniversalSensor());
        MinecraftForge.EVENT_BUS.register(new DroneSpecialVariableHandler());

        MinecraftForge.EVENT_BUS.register(new CraftingHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigHandler());
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        NetworkHandler.init();

        Fluids.init();
        SemiBlockInitializer.init();
        CraftingRegistrator.init();
        HackableHandler.addDefaultEntries();
        SensorHandler.getInstance().init();

        // FIXME: chest loot generation
        if (ConfigHandler.general.enableDungeonLoot) {
//            ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
//
//            ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
//            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        }

        OreDictionaryHelper.addOreDictEntries();

        proxy.init();
        ThirdPartyManager.instance().init();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        ServerCommandManager comManager = (ServerCommandManager) event.getServer().getCommandManager();
        new PCCommandManager().init(comManager);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        //Add these later so we include other mod's storage recipes.
//         CraftingRegistrator.addPressureChamberStorageBlockRecipes();
        CraftingRegistrator.addAssemblyCombinedRecipes();
        HeatExchangerManager.getInstance().init();
        FluidFuelManager.registerFuels();

        ThirdPartyManager.instance().postInit();
        proxy.postInit();
        ConfigHandler.postInit();
        AmadronOfferManager.getInstance().shufflePeriodicOffers();
        AmadronOfferManager.getInstance().recompileOffers();
        CraftingRegistrator.addProgrammingPuzzleRecipes();

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
        Fluid oil = FluidRegistry.getFluid(Fluids.OIL.getName());
        if (oil.getBlock() == null) {
            String modName = FluidRegistry.getDefaultFluidName(oil).split(":")[0];
            throw new IllegalStateException(String.format("Oil fluid does not have a block associated with it. The fluid is owned by %s. This could be fixed by creating the world with having this mod loaded after PneumaticCraft. This can be done by adding a injectedDependencies.json inside the config folder containing: [{\"modId\": \"%s\",\"deps\": [{\"type\":\"after\",\"target\":\"%s\"}]}]", modName, modName, Names.MOD_ID));
        }
    }
}
