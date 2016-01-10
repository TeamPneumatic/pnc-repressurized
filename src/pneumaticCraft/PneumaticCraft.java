package pneumaticCraft;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.client.CreativeTabPneumaticCraft;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.AchievementHandler;
import pneumaticCraft.common.EventHandlerPneumaticCraft;
import pneumaticCraft.common.EventHandlerUniversalSensor;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.TickHandlerPneumaticCraft;
import pneumaticCraft.common.VillagerHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.commands.PCCommandManager;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.entity.EntityRegistrator;
import pneumaticCraft.common.event.DroneSpecialVariableHandler;
import pneumaticCraft.common.fluid.FluidFuelManager;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.heat.HeatExchangerManager;
import pneumaticCraft.common.heat.behaviour.HeatBehaviourManager;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.progwidgets.WidgetRegistrator;
import pneumaticCraft.common.recipes.AmadronOfferManager;
import pneumaticCraft.common.recipes.CraftingHandler;
import pneumaticCraft.common.recipes.CraftingRegistrator;
import pneumaticCraft.common.semiblock.SemiBlockInitializer;
import pneumaticCraft.common.sensor.SensorHandler;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.common.tileentity.TileEntityRegistrator;
import pneumaticCraft.common.worldgen.WorldGeneratorPneumaticCraft;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.Versions;
import pneumaticCraft.proxy.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Names.MOD_ID, name = "PneumaticCraft", guiFactory = "pneumaticCraft.client.GuiConfigHandler", dependencies = "required-after:Forge@[10.13.3.1388,);" + "after:Forestry;after:" + ModIds.COMPUTERCRAFT)
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
        WidgetRegistrator.init();
        ThirdPartyManager.instance().preInit();
        TileEntityRegistrator.init();
        EntityRegistrator.init();
        SemiBlockInitializer.init();
        CraftingRegistrator.init();
        VillagerHandler.instance().init();
        GameRegistry.registerWorldGenerator(new WorldGeneratorPneumaticCraft(), 0);
        AchievementHandler.init();
        HeatBehaviourManager.getInstance().init();

        proxy.registerRenders();
        proxy.registerHandlers();
        tickHandler = new TickHandlerPneumaticCraft();
        FMLCommonHandler.instance().bus().register(tickHandler);
        MinecraftForge.EVENT_BUS.register(new EventHandlerPneumaticCraft());
        MinecraftForge.EVENT_BUS.register(new EventHandlerUniversalSensor());
        MinecraftForge.EVENT_BUS.register(new DroneSpecialVariableHandler());

        FMLCommonHandler.instance().bus().register(new CraftingHandler());
        FMLCommonHandler.instance().bus().register(new Config());
    }

    @EventHandler
    public void load(FMLInitializationEvent event){
        NetworkHandler.init();

        if(Config.enableDungeonLoot) {
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
        }

        proxy.init();
        ThirdPartyManager.instance().init();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event){
        ServerCommandManager comManager = (ServerCommandManager)MinecraftServer.getServer().getCommandManager();
        new PCCommandManager().init(comManager);
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
        Config.postInit();
        AmadronOfferManager.getInstance().shufflePeriodicOffers();
        AmadronOfferManager.getInstance().recompileOffers();
        CraftingRegistrator.addProgrammingPuzzleRecipes();
    }

    @EventHandler
    public void onMissingMapping(FMLMissingMappingsEvent event){
        for(MissingMapping mapping : event.get()) {
            if(mapping.type == GameRegistry.Type.BLOCK && mapping.name.equals("PneumaticCraft:etchingAcid")) {
                mapping.remap(Fluids.etchingAcid.getBlock());
                Log.info("Remapping Etching Acid");
            }
            if(mapping.type == GameRegistry.Type.ITEM && mapping.name.equals("PneumaticCraft:etchingAcidBucket")) {
                mapping.remap(Fluids.getBucket(Fluids.etchingAcid));
                Log.info("Remapping Etching Acid Bucket");
            }

        }
    }

    public void registerFuel(final ItemStack fuelStack, final int fuelValue){
        GameRegistry.registerFuelHandler(new IFuelHandler(){
            @Override
            public int getBurnTime(ItemStack fuel){
                return fuel != null && fuel.isItemEqual(fuelStack) ? fuelValue : 0;
            }
        });
    }

    @EventHandler
    public void validateFluids(FMLServerStartedEvent event){
        Fluid oil = FluidRegistry.getFluid(Fluids.oil.getName());
        if(oil.getBlock() == null) {
            String modName = FluidRegistry.getDefaultFluidName(oil).split(":")[0];
            throw new IllegalStateException(String.format("Oil fluid does not have a block associated with it. The fluid is owned by %s. This could be fixed by creating the world with having this mod loaded after PneumaticCraft. This can be done by adding a injectedDependencies.json inside the config folder containing: [{\"modId\": \"%s\",\"deps\": [{\"type\":\"after\",\"target\":\"%s\"}]}]", modName, modName, Names.MOD_ID));
        }
    }
}
