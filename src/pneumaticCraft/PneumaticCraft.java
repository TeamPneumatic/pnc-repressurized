package pneumaticCraft;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.client.CreativeTabPneumaticCraft;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.EventHandlerPneumaticCraft;
import pneumaticCraft.common.EventHandlerUniversalSensor;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.TickHandlerPneumaticCraft;
import pneumaticCraft.common.VillagerHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.entity.EntityRegistrator;
import pneumaticCraft.common.fluid.FluidFuelManager;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.heat.HeatExchangerManager;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.recipes.CraftingHandler;
import pneumaticCraft.common.recipes.CraftingRegistrator;
import pneumaticCraft.common.sensor.SensorHandler;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.common.tileentity.TileEntityRegistrator;
import pneumaticCraft.common.worldgen.WorldGeneratorPneumaticCraft;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.Versions;
import pneumaticCraft.proxy.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Names.MOD_ID, name = "PneumaticCraft", guiFactory = "pneumaticCraft.client.GuiConfigHandler", dependencies = "required-after:Forge@[10.13.0.1179,);" + "after:Forestry")
public class PneumaticCraft{

    @SidedProxy(clientSide = "pneumaticCraft.proxy.ClientProxy", serverSide = "pneumaticCraft.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Instance(Names.MOD_ID)
    public static PneumaticCraft instance;

    public static TickHandlerPneumaticCraft tickHandler;
    public static CreativeTabPneumaticCraft tabPneumaticCraft;

    public static boolean isNEIInstalled;

    @EventHandler
    public void PreInit(FMLPreInitializationEvent event){
        event.getModMetadata().version = Versions.fullVersionString();
        isNEIInstalled = Loader.isModLoaded(ModIds.NEI);

        PneumaticRegistry.init(PneumaticCraftAPIHandler.getInstance());
        UpgradeRenderHandlerList.init();
        SensorHandler.init();
        Config.init(event.getSuggestedConfigurationFile());
        ThirdPartyManager.instance().index();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        tabPneumaticCraft = new CreativeTabPneumaticCraft("tabPneumaticCraft");
        Fluids.initFluids();
        Blockss.init();
        Itemss.init();
        HackableHandler.addDefaultEntries();
        ModuleRegistrator.init();
        ThirdPartyManager.instance().preInit();
        TileEntityRegistrator.init();
        EntityRegistrator.init();
        CraftingRegistrator.init();
        VillagerHandler.instance().init();
        GameRegistry.registerWorldGenerator(new WorldGeneratorPneumaticCraft(), 0);

        proxy.registerRenders();
        proxy.registerHandlers();
        tickHandler = new TickHandlerPneumaticCraft();
        FMLCommonHandler.instance().bus().register(tickHandler);
        MinecraftForge.EVENT_BUS.register(new EventHandlerPneumaticCraft());
        MinecraftForge.EVENT_BUS.register(new EventHandlerUniversalSensor());

        FMLCommonHandler.instance().bus().register(new CraftingHandler());
        FMLCommonHandler.instance().bus().register(new Config());
    }

    @EventHandler
    public void load(FMLInitializationEvent event){
        NetworkHandler.init();

        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.stopWorm), 1, 4, 10));

        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(Itemss.nukeVirus), 1, 4, 10));

        ThirdPartyManager.instance().init();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event){
        /*      ServerCommandManager comManager = (ServerCommandManager)MinecraftServer.getServer().getCommandManager();
              comManager.registerCommand(new UpdateChecker.CommandChangelog());*/
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){

        //Add these later so we include other mod's storage recipes.
        // CraftingRegistrator.addPressureChamberStorageBlockRecipes();
        CraftingRegistrator.addAssemblyCombinedRecipes();
        HeatExchangerManager.getInstance().init();
        FluidFuelManager.registerFuels();

        ThirdPartyManager.instance().postInit();
        proxy.postInit();
    }
}
