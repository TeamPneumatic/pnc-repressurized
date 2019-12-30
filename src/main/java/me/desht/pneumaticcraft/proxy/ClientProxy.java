package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.client.ClientEventHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ClientProxy implements IProxy {

//    private final HackTickHandler clientHackTickHandler = new HackTickHandler();
//    public final Map<String, Pair<Integer,KeyModifier>> keybindToKeyCodes = new HashMap<>();

    @Override
    public void preInit() {
//        TintedOBJLoader.INSTANCE.addDomain(Names.MOD_ID);
//        ModelLoaderRegistry.registerLoader(TintedOBJLoader.INSTANCE);
//        ModelLoaderRegistry.registerLoader(PressureGlassModelLoader.INSTANCE);

        ThirdPartyManager.instance().clientPreInit();

        EntityTrackHandler.registerDefaultEntries();
    }

    @Override
    public void init() {
        // todo 1.14 fluids
//        for (Fluid fluid : Fluids.FLUIDS) {
//            ModelLoader.setBucketModelDefinition(Fluids.getBucket(fluid));
//        }

//        getAllKeybindsFromOptionsFile();

        ThirdPartyManager.instance().clientInit();
    }

    @Override
    public void postInit() {
//        EntityTrackHandler.init();
//        GuiHelmetMainScreen.initHelmetMainScreen();
//        DramaSplash.getInstance();
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

//    @Override
//    public HackTickHandler getHackTickHandler() {
//        return clientHackTickHandler;
//    }

//    private void getAllKeybindsFromOptionsFile() {
//        File optionsFile = new File(Minecraft.getInstance().gameDir, "options.txt");
//        if (optionsFile.exists()) {
//            try (BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile))) {
//                String s = "";
//                while ((s = bufferedreader.readLine()) != null) {
//                    String[] str = s.split(":");
//                    if (str[0].startsWith("key_")) {
//                        KeyModifier mod = str.length > 2 ? KeyModifier.valueFromString(str[2]) : KeyModifier.NONE;
////                        keybindToKeyCodes.put(str[0].substring(4), Pair.of(Integer.parseInt(str[1]), mod));
//                        InputMappings.Input i = InputMappings.getInputByName(str[1]);
//                        keybindToKeyCodes.put(str[0].substring(4), Pair.of(i.getKeyCode(), mod));
//                    }
//                }
//            } catch (Exception exception1) {
//                Log.error("Failed to process options.txt:");
//                exception1.printStackTrace();
//            }
//        }
//    }

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
