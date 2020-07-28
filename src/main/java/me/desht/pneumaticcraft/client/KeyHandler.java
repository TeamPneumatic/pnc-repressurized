package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyHandler {
    private static final String DESCRIPTION_ARMOR_OPTIONS = "pneumaticcraft.armor.options";
    private static final String DESCRIPTION_HELMET_HACK = "pneumaticcraft.helmet.hack";
    private static final String DESCRIPTION_HELMET_DEBUGGING_DRONE = "pneumaticcraft.helmet.debugging.drone";
    private static final String DESCRIPTION_BOOTS_KICK = "pneumaticcraft.boots.kick";
    private static final String DESCRIPTION_LAUNCHER = "pneumaticcraft.chestplate.launcher";

    private static final KeyHandler INSTANCE = new KeyHandler();

    public final KeyBinding keybindOpenOptions;
    public final KeyBinding keybindHack;
    public final KeyBinding keybindDebuggingDrone;
    public final KeyBinding keybindKick;
    public final KeyBinding keybindLauncher;
    private final List<IKeyListener> keyListeners = new ArrayList<>();
    private final List<KeyBinding> keys = new ArrayList<>();

    public static KeyHandler getInstance() {
        return INSTANCE;
    }

    private KeyHandler() {
        registerKeyListener(HUDHandler.getInstance());

        keybindOpenOptions = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_ARMOR_OPTIONS, KeyConflictContext.IN_GAME,
                KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_U, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindHack = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_HELMET_HACK, KeyConflictContext.IN_GAME,
                KeyModifier.NONE, InputMappings.Type.KEYSYM,  GLFW.GLFW_KEY_H, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindDebuggingDrone = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_HELMET_DEBUGGING_DRONE, KeyConflictContext.IN_GAME,
                KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_Y, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindKick = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_BOOTS_KICK, KeyConflictContext.IN_GAME,
                KeyModifier.CONTROL, InputMappings.Type.KEYSYM,  GLFW.GLFW_KEY_X, Names.PNEUMATIC_KEYBINDING_CATEGORY));
        keybindLauncher = registerKeyBinding(new KeyBinding(KeyHandler.DESCRIPTION_LAUNCHER, KeyConflictContext.IN_GAME,
                KeyModifier.CONTROL, InputMappings.Type.KEYSYM,  GLFW.GLFW_KEY_C, Names.PNEUMATIC_KEYBINDING_CATEGORY));
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
    public void onKey(InputEvent.KeyInputEvent event) {
        for (KeyBinding key : keys) {
            if (key.isPressed()) {
                onKey(key);
            }
        }
    }

    private void onKey(KeyBinding keybinding) {
        for (IKeyListener listener : keyListeners) {
            listener.onKeyPress(keybinding);
        }
    }

}
