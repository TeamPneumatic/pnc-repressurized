package pneumaticCraft.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.Names;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

public class KeyHandler{
    private static KeyHandler INSTANCE = new KeyHandler();
    public KeyBinding keybindOpenOptions;
    public KeyBinding keybindHack;
    public KeyBinding keybindDebuggingDrone;
    public static final String DESCRIPTION_HELMET_HACK = "Pneumatic Helmet Hack";
    public static final String DESCRIPTION_HELMET_OPTIONS = "Pneumatic Helmet Options";
    public static final String DESCRIPTION_HELMET_DEBUGGING_DRONE = "Pneumatic Helmet Debugging Drone";
    private final List<IKeyListener> keyListeners = new ArrayList<IKeyListener>();
    private final List<KeyBinding> keys = new ArrayList<KeyBinding>();

    public static KeyHandler getInstance(){
        return INSTANCE;
    }

    private KeyHandler(){
        registerKeyListener(HUDHandler.instance());

        keybindOpenOptions = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_HELMET_OPTIONS, Keyboard.KEY_F, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindHack = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_HELMET_HACK, Keyboard.KEY_H, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindDebuggingDrone = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_HELMET_DEBUGGING_DRONE, Keyboard.KEY_Y, Names.PNEUMATIC_KEYBINDING_CATEGORY));
    }

    private KeyBinding registerKeyBinding(KeyBinding keyBinding){
        ClientRegistry.registerKeyBinding(keyBinding);
        keys.add(keyBinding);
        return keyBinding;
    }

    public void registerKeyListener(IKeyListener listener){
        keyListeners.add(listener);
    }

    /**
     * This will only subscribe when NotEnoughKeys is not installed.
     * @param event
     */
    @SubscribeEvent
    public void onKey(KeyInputEvent event){
        if(!nekLoaded()) {
            for(KeyBinding key : keys) {
                if(key.isPressed()) {
                    onKey(key);
                }
            }
        }
    }

    private boolean nekLoaded(){
        return Loader.isModLoaded(ModIds.NOT_ENOUGH_KEYS) && Config.config.get("Third_Party_Enabling", ModIds.NOT_ENOUGH_KEYS, true).getBoolean();
    }

    public void onKey(KeyBinding keybinding){
        for(IKeyListener listener : keyListeners) {
            listener.onKeyPress(keybinding);
        }
    }

}
