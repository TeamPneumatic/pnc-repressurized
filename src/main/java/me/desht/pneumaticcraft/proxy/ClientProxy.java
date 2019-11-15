package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.client.AreaShowManager;
import me.desht.pneumaticcraft.client.ClientEventHandler;
import me.desht.pneumaticcraft.client.ClientTickHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiHelmetMainScreen;
import me.desht.pneumaticcraft.client.model.TintedOBJLoader;
import me.desht.pneumaticcraft.client.model.pressureglass.PressureGlassModelLoader;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoordTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.semiblock.ClientSemiBlockManager;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.DramaSplash;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ClientProxy implements IProxy {

    private final HackTickHandler clientHackTickHandler = new HackTickHandler();
    public final Map<String, Pair<Integer,KeyModifier>> keybindToKeyCodes = new HashMap<>();

    @Override
    public void preInit() {
        TintedOBJLoader.INSTANCE.addDomain(Names.MOD_ID);
        ModelLoaderRegistry.registerLoader(TintedOBJLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(PressureGlassModelLoader.INSTANCE);

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(HUDHandler.instance());
        MinecraftForge.EVENT_BUS.register(ClientTickHandler.instance());
        MinecraftForge.EVENT_BUS.register(getHackTickHandler());
        MinecraftForge.EVENT_BUS.register(new ClientSemiBlockManager());
        MinecraftForge.EVENT_BUS.register(HUDHandler.instance().getSpecificRenderer(CoordTrackUpgradeHandler.class));
        MinecraftForge.EVENT_BUS.register(AreaShowManager.getInstance());
        MinecraftForge.EVENT_BUS.register(KeyHandler.getInstance());

        ThirdPartyManager.instance().clientPreInit();

        EntityTrackHandler.registerDefaultEntries();

        getAllKeybindsFromOptionsFile();
    }

    @Override
    public void init() {
        // todo 1.14 fluids
//        for (Fluid fluid : Fluids.FLUIDS) {
//            ModelLoader.setBucketModelDefinition(Fluids.getBucket(fluid));
//        }

        ThirdPartyManager.instance().clientInit();
    }

    @Override
    public void postInit() {
        EntityTrackHandler.init();
        GuiHelmetMainScreen.initHelmetMainScreen();
        DramaSplash.getInstance();
    }

    @Override
    public boolean isSneakingInGui() {
        return Screen.hasShiftDown();
    }

//    @Override
//    public void initConfig() {
//        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
//            for (IUpgradeRenderHandler renderHandler : UpgradeRenderHandlerList.instance().getHandlersForSlot(slot)) {
//                renderHandler.initConfig();
//            }
//        }
//    }

    @Override
    public String xlate(String key) {
        return I18n.format(key);
    }

    @Override
    public void suppressItemEquipAnimation() {
        FirstPersonRenderer renderer = Minecraft.getInstance().getFirstPersonRenderer();
        renderer.equippedProgressMainHand = 1;
        renderer.prevEquippedProgressMainHand = 1;
    }

    @Override
    public World getWorldFor(NetworkEvent.Context ctx) {
        return Minecraft.getInstance().world;
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public int getArmorRenderID(String armorName) {
        return 0;
    }

    @Override
    public HackTickHandler getHackTickHandler() {
        return clientHackTickHandler;
    }

    private void getAllKeybindsFromOptionsFile() {
        File optionsFile = new File(Minecraft.getInstance().gameDir, "options.txt");
        if (optionsFile.exists()) {
            try (BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile))) {
                String s = "";
                while ((s = bufferedreader.readLine()) != null) {
                    String[] str = s.split(":");
                    if (str[0].startsWith("key_")) {
                        KeyModifier mod = str.length > 2 ? KeyModifier.valueFromString(str[2]) : KeyModifier.NONE;
                        keybindToKeyCodes.put(str[0].substring(4), Pair.of(Integer.parseInt(str[1]), mod));
                    }
                }
            } catch (Exception exception1) {
                Log.error("Failed to process options.txt:");
                exception1.printStackTrace();
            }
        }
    }

    @Override
    public int particleLevel() {
        return Minecraft.getInstance().gameSettings.particles.func_216832_b(); // id : 0..2
    }

    @Override
    public Pair<Integer, Integer> getScaledScreenSize() {
        return ClientEventHandler.getScaledScreenSize();
    }

    @Override
    public Iterable<? extends Entity> getAllEntities(World world) {
        return ((ClientWorld) world).getAllEntities();
    }

    @Override
    public boolean isScreenHiRes() {
        MainWindow mw = Minecraft.getInstance().mainWindow;
        return mw.getScaledWidth() > 700 && mw.getScaledHeight() > 512;
    }

    @Override
    public void openGui(Screen gui) {
        Minecraft.getInstance().displayGuiScreen(gui);
    }
}
