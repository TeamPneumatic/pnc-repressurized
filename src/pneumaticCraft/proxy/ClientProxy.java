package pneumaticCraft.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.input.Keyboard;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.AreaShowManager;
import pneumaticCraft.client.ClientEventHandler;
import pneumaticCraft.client.ClientTickHandler;
import pneumaticCraft.client.gui.pneumaticHelmet.GuiHelmetMainScreen;
import pneumaticCraft.client.model.BaseModel;
import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.client.model.ModelAirCannon;
import pneumaticCraft.client.model.ModelAssemblyController;
import pneumaticCraft.client.model.ModelAssemblyDrill;
import pneumaticCraft.client.model.ModelAssemblyIOUnit;
import pneumaticCraft.client.model.ModelAssemblyLaser;
import pneumaticCraft.client.model.ModelAssemblyPlatform;
import pneumaticCraft.client.model.ModelChargingStation;
import pneumaticCraft.client.model.ModelComputer;
import pneumaticCraft.client.model.ModelElevatorBase;
import pneumaticCraft.client.model.ModelElevatorFrame;
import pneumaticCraft.client.model.ModelOmnidirectionalHopper;
import pneumaticCraft.client.model.ModelPneumaticDoor;
import pneumaticCraft.client.model.ModelPneumaticDoorBase;
import pneumaticCraft.client.model.ModelPressureChamberInterface;
import pneumaticCraft.client.model.ModelUVLightBox;
import pneumaticCraft.client.model.ModelUniversalSensor;
import pneumaticCraft.client.model.ModelVacuumPump;
import pneumaticCraft.client.render.block.RenderPneumaticDoorCamo;
import pneumaticCraft.client.render.entity.RenderDrone;
import pneumaticCraft.client.render.entity.RenderEntityChopperSeeds;
import pneumaticCraft.client.render.entity.RenderEntityPotionCloud;
import pneumaticCraft.client.render.entity.RenderEntityRing;
import pneumaticCraft.client.render.entity.RenderEntityVortex;
import pneumaticCraft.client.render.item.RenderItemCannonParts;
import pneumaticCraft.client.render.item.RenderItemDrone;
import pneumaticCraft.client.render.item.RenderItemPneumaticCilinder;
import pneumaticCraft.client.render.item.RenderItemPneumaticHelmet;
import pneumaticCraft.client.render.item.RenderItemProgrammingPuzzle;
import pneumaticCraft.client.render.item.RenderItemVortexCannon;
import pneumaticCraft.client.render.itemblock.RenderItemPressureTube;
import pneumaticCraft.client.render.itemblock.RenderItemTubeModule;
import pneumaticCraft.client.render.pneumaticArmor.CoordTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.client.render.pneumaticArmor.entitytracker.EntityTrackHandler;
import pneumaticCraft.client.render.tileentity.RenderAirCannon;
import pneumaticCraft.client.render.tileentity.RenderAphorismTile;
import pneumaticCraft.client.render.tileentity.RenderElevatorCaller;
import pneumaticCraft.client.render.tileentity.RenderModelBase;
import pneumaticCraft.client.render.tileentity.RenderPressureTube;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.HackTickHandler;
import pneumaticCraft.common.UpdateChecker;
import pneumaticCraft.common.block.BlockPneumaticCraft;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.entity.EntityRing;
import pneumaticCraft.common.entity.item.EntityItemSpecial;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.entity.projectile.EntityChopperSeeds;
import pneumaticCraft.common.entity.projectile.EntityPotionCloud;
import pneumaticCraft.common.entity.projectile.EntityVortex;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.ThirdPartyManager;
import pneumaticCraft.common.tileentity.TileEntityAirCannon;
import pneumaticCraft.common.tileentity.TileEntityAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityAphorismTile;
import pneumaticCraft.common.tileentity.TileEntityAssemblyController;
import pneumaticCraft.common.tileentity.TileEntityAssemblyDrill;
import pneumaticCraft.common.tileentity.TileEntityAssemblyIOUnit;
import pneumaticCraft.common.tileentity.TileEntityAssemblyLaser;
import pneumaticCraft.common.tileentity.TileEntityAssemblyPlatform;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.common.tileentity.TileEntityCreativeCompressor;
import pneumaticCraft.common.tileentity.TileEntityElectrostaticCompressor;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.common.tileentity.TileEntityElevatorCaller;
import pneumaticCraft.common.tileentity.TileEntityElevatorFrame;
import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberInterface;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.common.tileentity.TileEntityUVLightBox;
import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import pneumaticCraft.common.tileentity.TileEntityVacuumPump;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class ClientProxy extends CommonProxy{

    private final HackTickHandler clientHackTickHandler = new HackTickHandler();
    public final Map<String, Integer> keybindToKeyCodes = new HashMap<String, Integer>();

    @Override
    public void registerRenders(){
        SPECIAL_RENDER_TYPE_VALUE = RenderingRegistry.getNextAvailableRenderId();
        CAMO_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();

        RenderingRegistry.registerBlockHandler(new RenderModelBase());
        // RenderingRegistry.registerBlockHandler(new RendererSpecialBlock());
        registerBaseModelRenderer(Blockss.airCompressor, TileEntityAirCompressor.class, new BaseModel("airCompressor.tcn"));
        registerBaseModelRenderer(Blockss.assemblyController, TileEntityAssemblyController.class, new ModelAssemblyController());
        registerBaseModelRenderer(Blockss.assemblyDrill, TileEntityAssemblyDrill.class, new ModelAssemblyDrill());
        registerBaseModelRenderer(Blockss.assemblyIOUnit, TileEntityAssemblyIOUnit.class, new ModelAssemblyIOUnit());
        registerBaseModelRenderer(Blockss.assemblyLaser, TileEntityAssemblyLaser.class, new ModelAssemblyLaser());
        registerBaseModelRenderer(Blockss.assemblyPlatform, TileEntityAssemblyPlatform.class, new ModelAssemblyPlatform());
        registerBaseModelRenderer(Blockss.chargingStation, TileEntityChargingStation.class, new ModelChargingStation());
        registerBaseModelRenderer(Blockss.creativeCompressor, TileEntityCreativeCompressor.class, new BaseModel("creativeCompressor.obj"));
        registerBaseModelRenderer(Blockss.electrostaticCompressor, TileEntityElectrostaticCompressor.class, new BaseModel("electrostaticCompressor.obj"));
        registerBaseModelRenderer(Blockss.elevatorBase, TileEntityElevatorBase.class, new ModelElevatorBase());
        registerBaseModelRenderer(Blockss.elevatorFrame, TileEntityElevatorFrame.class, new ModelElevatorFrame());
        registerBaseModelRenderer(Blockss.pneumaticDoor, TileEntityPneumaticDoor.class, new ModelPneumaticDoor());
        registerBaseModelRenderer(Blockss.pneumaticDoorBase, TileEntityPneumaticDoorBase.class, new ModelPneumaticDoorBase());
        registerBaseModelRenderer(Blockss.pressureChamberInterface, TileEntityPressureChamberInterface.class, new ModelPressureChamberInterface());
        registerBaseModelRenderer(Blockss.securityStation, TileEntitySecurityStation.class, new ModelComputer(Textures.MODEL_SECURITY_STATION));
        registerBaseModelRenderer(Blockss.universalSensor, TileEntityUniversalSensor.class, new ModelUniversalSensor());
        registerBaseModelRenderer(Blockss.uvLightBox, TileEntityUVLightBox.class, new ModelUVLightBox());
        registerBaseModelRenderer(Blockss.vacuumPump, TileEntityVacuumPump.class, new ModelVacuumPump());
        registerBaseModelRenderer(Blockss.omnidirectionalHopper, TileEntityOmnidirectionalHopper.class, new ModelOmnidirectionalHopper());
        registerBaseModelRenderer(Blockss.programmer, TileEntityProgrammer.class, new ModelComputer(Textures.MODEL_PROGRAMMER));

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressureTube.class, new RenderPressureTube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAirCannon.class, new RenderAirCannon());
        // ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElevatorBase.class, new RenderElevatorBase());
        // ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAdvancedPressureTube.class, new RenderAdvancedPressureTube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAphorismTile.class, new RenderAphorismTile());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElevatorCaller.class, new RenderElevatorCaller());

        RenderingRegistry.registerBlockHandler(new RenderPneumaticDoorCamo());

        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Blockss.pressureTube), new RenderItemPressureTube(false));
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Blockss.advancedPressureTube), new RenderItemPressureTube(true));

        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Blockss.airCannon), new RenderModelBase(new ModelAirCannon()));
        MinecraftForgeClient.registerItemRenderer(Itemss.vortexCannon, new RenderItemVortexCannon());
        // MinecraftForgeClient.registerItemRenderer(Blocks.elevatorBase, new RenderItemElevatorBase());
        MinecraftForgeClient.registerItemRenderer(Itemss.cannonBarrel, new RenderItemCannonParts(false));
        MinecraftForgeClient.registerItemRenderer(Itemss.stoneBase, new RenderItemCannonParts(true));
        MinecraftForgeClient.registerItemRenderer(Itemss.pneumaticCylinder, new RenderItemPneumaticCilinder());
        //   MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Blockss.advancedPressureTube), new RenderItemAdvancedPressureTube());
        MinecraftForgeClient.registerItemRenderer(Itemss.drone, new RenderItemDrone());
        MinecraftForgeClient.registerItemRenderer(Itemss.programmingPuzzle, new RenderItemProgrammingPuzzle());
        MinecraftForgeClient.registerItemRenderer(Itemss.pneumaticHelmet, new RenderItemPneumaticHelmet());

        RenderingRegistry.registerEntityRenderingHandler(EntityVortex.class, new RenderEntityVortex());
        RenderingRegistry.registerEntityRenderingHandler(EntityItemSpecial.class, new RenderItem());
        RenderingRegistry.registerEntityRenderingHandler(EntityChopperSeeds.class, new RenderEntityChopperSeeds());
        RenderingRegistry.registerEntityRenderingHandler(EntityPotionCloud.class, new RenderEntityPotionCloud());
        RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, new RenderDrone());

        RenderingRegistry.registerEntityRenderingHandler(EntityRing.class, new RenderEntityRing());
        EntityRegistry.registerModEntity(EntityRing.class, "Ring", 100, PneumaticCraft.instance, 80, 1, true);

        registerModuleRenderers();
        super.registerRenders();
    }

    public static void registerBaseModelRenderer(Block block, Class<? extends TileEntity> tileEntityClass, IBaseModel model){
        if(model instanceof BaseModel) {
            ((BaseModel)model).rotatable = ((BlockPneumaticCraft)block).isRotatable();
        }
        registerBaseModelRenderer(Item.getItemFromBlock(block), tileEntityClass, model);
    }

    private static void registerBaseModelRenderer(Item item, Class<? extends TileEntity> tileEntityClass, IBaseModel model){
        RenderModelBase renderer = new RenderModelBase(model);
        ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass, renderer);
        MinecraftForgeClient.registerItemRenderer(item, renderer);
    }

    @Override
    public boolean isSneakingInGui(){

        return Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
    }

    @Override
    public void registerHandlers(){
        super.registerHandlers();
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        MinecraftForge.EVENT_BUS.register(HUDHandler.instance());
        FMLCommonHandler.instance().bus().register(HUDHandler.instance());
        FMLCommonHandler.instance().bus().register(ClientTickHandler.instance());
        FMLCommonHandler.instance().bus().register(getHackTickHandler());
        FMLCommonHandler.instance().bus().register(clientHudHandler = new CommonHUDHandler());

        MinecraftForge.EVENT_BUS.register(HUDHandler.instance().getSpecificRenderer(CoordTrackUpgradeHandler.class));
        MinecraftForge.EVENT_BUS.register(AreaShowManager.getInstance());
        FMLCommonHandler.instance().bus().register(AreaShowManager.getInstance());

        HUDHandler.instance().registerKeyBinds();

        ThirdPartyManager.instance().clientSide();

        if(Config.enableUpdateChecker) {
            UpdateChecker.instance().start();
            FMLCommonHandler.instance().bus().register(UpdateChecker.instance());
        }
        EntityTrackHandler.registerDefaultEntries();
        getAllKeybindsFromOptionsFile();
    }

    private void getAllKeybindsFromOptionsFile(){
        File optionsFile = new File(FMLClientHandler.instance().getClient().mcDataDir, "options.txt");
        if(optionsFile.exists()) {
            try {
                BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile));
                String s = "";

                while((s = bufferedreader.readLine()) != null) {
                    try {
                        String[] astring = s.split(":");
                        if(astring[0].startsWith("key_")) {
                            keybindToKeyCodes.put(astring[0].substring(4), Integer.parseInt(astring[1]));
                        }
                    } catch(Exception exception) {
                        Log.warning("Skipping bad option: " + s);
                    }
                }
            } catch(Exception exception1) {
                Log.error("Failed to load options");
                exception1.printStackTrace();
            }
        }
    }

    @Override
    public void postInit(){
        EntityTrackHandler.init();
        GuiHelmetMainScreen.init();
    }

    public void registerModuleRenderers(){
        Collection<Item> moduleItems = ModuleRegistrator.moduleItems.values();
        Collection<Class<? extends TubeModule>> modules = ModuleRegistrator.modules.values();
        Iterator<Item> itemIterator = moduleItems.iterator();
        Iterator<Class<? extends TubeModule>> moduleIterator = modules.iterator();
        while(itemIterator.hasNext()) {
            try {
                MinecraftForgeClient.registerItemRenderer(itemIterator.next(), new RenderItemTubeModule(moduleIterator.next().newInstance()));
            } catch(Exception e) {
                Log.error("Something happened when registering tube module renderers!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initConfig(Configuration config){
        for(IUpgradeRenderHandler renderHandler : UpgradeRenderHandlerList.instance().upgradeRenderers) {
            renderHandler.initConfig(config);
        }
    }

    @Override
    public World getClientWorld(){
        return FMLClientHandler.instance().getClient().theWorld;
    }

    @Override
    public EntityPlayer getPlayer(){
        return FMLClientHandler.instance().getClient().thePlayer;
    }

    @Override
    public int getArmorRenderID(String armorName){
        return RenderingRegistry.addNewArmourRendererPrefix(armorName);
    }

    @Override
    public void registerVillagerSkins(){
        VillagerRegistry.instance().registerVillagerSkin(Config.villagerMechanicID, new ResourceLocation(Textures.VILLAGER_MECHANIC));
    }

    @Override
    public HackTickHandler getHackTickHandler(){
        return clientHackTickHandler;
    }
}
