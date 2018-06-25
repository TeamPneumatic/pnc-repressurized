package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class KeyHandler {
    private static final String DESCRIPTION_ARMOR_OPTIONS = "pneumaticcraft.armor.options";
    private static final String DESCRIPTION_HELMET_HACK = "pneumaticcraft.helmet.hack";
    private static final String DESCRIPTION_HELMET_DEBUGGING_DRONE = "pneumaticcraft.helmet.debugging.drone";
    private static final String DESCRIPTION_BOOTS_KICK = "pneumaticcraft.boots.kick";

    private static KeyHandler INSTANCE = new KeyHandler();

    public KeyBinding keybindOpenOptions;
    public KeyBinding keybindHack;
    public KeyBinding keybindDebuggingDrone;
    public KeyBinding keybindKick;
    private final List<IKeyListener> keyListeners = new ArrayList<>();
    private final List<KeyBinding> keys = new ArrayList<>();

    public static KeyHandler getInstance() {
        return INSTANCE;
    }

    private KeyHandler() {
        registerKeyListener(HUDHandler.instance());

        keybindOpenOptions = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_ARMOR_OPTIONS, KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_U, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindHack = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_HELMET_HACK, KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_H, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindDebuggingDrone = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_HELMET_DEBUGGING_DRONE, KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_Y, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindKick = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_BOOTS_KICK, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Keyboard.KEY_X, Names.PNEUMATIC_KEYBINDING_CATEGORY));
    }

    private KeyBinding registerKeyBinding(KeyBinding keyBinding) {
        ClientRegistry.registerKeyBinding(keyBinding);
        keys.add(keyBinding);
        return keyBinding;
    }

    private void registerKeyListener(IKeyListener listener) {
        keyListeners.add(listener);
    }

    /**
     * This will only subscribe when NotEnoughKeys is not installed.
     *
     * @param event
     */
    @SubscribeEvent
    public void onKey(KeyInputEvent event) {
        for (KeyBinding key : keys) {
            if (key.isPressed()) {
                onKey(key);
            }
        }
    }

    public void onKey(KeyBinding keybinding) {
        for (IKeyListener listener : keyListeners) {
            listener.onKeyPress(keybinding);
        }
    }

}
