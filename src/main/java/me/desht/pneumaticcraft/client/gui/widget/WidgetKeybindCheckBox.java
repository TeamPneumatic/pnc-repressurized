package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.ClientSetup;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorFeatureStatus;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetKeybindCheckBox extends WidgetCheckBox implements ITooltipProvider {
    public static final String UPGRADE_PREFIX = "pneumaticcraft.armor.upgrade.";

    private static WidgetKeybindCheckBox coreComponents;

    private final String upgradeID;
    private boolean isAwaitingKey;
    private ITextComponent oldCheckboxText;
    private KeyBinding keyBinding;

    public WidgetKeybindCheckBox(int x, int y, int color, String upgradeID, Consumer<WidgetCheckBox> pressable) {
        super(x, y, color, xlate("pneumaticcraft.gui.enableModule", xlate(UPGRADE_PREFIX + upgradeID)), pressable);

        this.upgradeID = upgradeID;
        this.keyBinding = findSavedKeybind();

        if (!KeyDispatcher.id2checkBox.containsKey(upgradeID)) {
            this.checked = ArmorFeatureStatus.INSTANCE.isUpgradeEnabled(upgradeID);
            if (keyBinding != null) {
                KeyDispatcher.addKeybind(keyBinding, this);
            }
            KeyDispatcher.id2checkBox.put(this.upgradeID, this);
            if (upgradeID.equals("coreComponents")) {
                // stash this one since it's referenced a lot
                coreComponents = this;
            }
        } else {
            this.checked = KeyDispatcher.id2checkBox.get(upgradeID).checked;
        }
    }

    private KeyBinding makeKeyBinding(int keyCode, KeyModifier modifier) {
        return new KeyBinding(UPGRADE_PREFIX + upgradeID, KeyConflictContext.IN_GAME, modifier,
                InputMappings.Type.KEYSYM, keyCode, Names.PNEUMATIC_KEYBINDING_CATEGORY);
    }

    public static WidgetKeybindCheckBox getCoreComponents() {
        return coreComponents;
    }

    public static WidgetKeybindCheckBox fromKeyBindingName(String name) {
        return KeyDispatcher.id2checkBox.get(name);
    }

    public static boolean isHandlerEnabled(IUpgradeRenderHandler handler) {
        return fromKeyBindingName(handler.getUpgradeID()).checked;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.clicked(x, y)) {
            this.playDownSound(Minecraft.getInstance().getSoundHandler());
            handleClick(x, y, button);
            return true;
        }
        return false;
    }

    private void handleClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // left click - usual toggle action
            super.onClick(mouseX, mouseY);
            ArmorFeatureStatus.INSTANCE.setUpgradeEnabled(upgradeID, checked);
            KeyDispatcher.id2checkBox.get(upgradeID).checked = checked;
            try {
                ArmorFeatureStatus.INSTANCE.writeToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CommonArmorHandler hudHandler = CommonArmorHandler.getHandlerForPlayer();
            for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                for (int i = 0; i < renderHandlers.size(); i++) {
                    IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                    if ((upgradeRenderHandler.getUpgradeID()).equals(upgradeID) && hudHandler.isUpgradeRendererInserted(slot, i)) {
                        NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, coreComponents.checked && checked, slot));
                        hudHandler.setUpgradeRenderEnabled(slot, (byte)i, coreComponents.checked && checked);
                        HUDHandler.instance().addFeatureToggleMessage(UPGRADE_PREFIX + upgradeID, checked);
                        break;
                    }
                }
                if (upgradeID.equals("coreComponents")) {
                    for (int i = 0; i < renderHandlers.size(); i++) {
                        boolean state = WidgetKeybindCheckBox.fromKeyBindingName(renderHandlers.get(i).getUpgradeID()).checked;
                        NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, coreComponents.checked && state, slot));
                        hudHandler.setUpgradeRenderEnabled(slot, (byte)i, coreComponents.checked && state);
                    }
                }
            }
        } else if (button == 1) {
            // right click - clear or set up key binding
            if (Screen.hasShiftDown()) {
                clearKeybinding();
            } else {
                isAwaitingKey = !isAwaitingKey;
                if (isAwaitingKey) {
                    oldCheckboxText = getMessage();
                    setMessage(xlate("pneumaticcraft.gui.setKeybind"));
                } else {
                    setMessage(oldCheckboxText);
                }
            }
        }
    }

    private void clearKeybinding() {
        if (keyBinding != null) KeyDispatcher.removeKeybind(keyBinding);

        KeyBinding[] keyBindings = Minecraft.getInstance().gameSettings.keyBindings;
        Set<Integer> idx = new HashSet<>();
        for (int i = 0; i < keyBindings.length; i++) {
            if (keyBindings[i].getKeyDescription().equals(keyBinding.getKeyDescription())) {
                idx.add(i);
                break;
            }
        }
        if (!idx.isEmpty()) {
            List<KeyBinding> l = new ArrayList<>(keyBindings.length);
            for (int i = 0; i < keyBindings.length; i++) {
                if (!idx.contains(i)) l.add(keyBindings[i]);
            }
            Minecraft.getInstance().gameSettings.keyBindings = l.toArray(new KeyBinding[0]);
            keyBinding = makeKeyBinding(GLFW.GLFW_KEY_UNKNOWN, KeyModifier.NONE);
            ClientRegistry.registerKeyBinding(keyBinding);
            KeyBinding.resetKeyBindingArrayAndHash();
            ClientSetup.keybindToKeyCodes.put(upgradeID, Pair.of(GLFW.GLFW_KEY_UNKNOWN, KeyModifier.NONE));
            Minecraft.getInstance().gameSettings.saveOptions();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isAwaitingKey) {
            InputMappings.Input input = InputMappings.Type.KEYSYM.getOrMakeInput(keyCode);
            if (!KeyModifier.isKeyCodeModifier(input)) {
                isAwaitingKey = false;
                keyBinding = setOrAddKeybind(keyCode, KeyModifier.getActiveModifier());
                KeyDispatcher.cleanupKeybind(upgradeID);
                // NOTE: we can't use "this" here because the id->checkbox and keybind->checkbox
                // maps MUST continue to refer to the same object!
                KeyDispatcher.addKeybind(keyBinding, WidgetKeybindCheckBox.fromKeyBindingName(upgradeID));
                setMessage(oldCheckboxText);
            }
            return true;
        }
        return false;
    }

    private KeyBinding findSavedKeybind() {
        return setOrAddKeybind(-1, KeyModifier.NONE);
    }

    /**
     * @param keyCode     when < 0, this will function as a getter & may return null
     * @param modifier key modifier (may be NONE)
     * @return the key binding
     */
    private KeyBinding setOrAddKeybind(int keyCode, KeyModifier modifier) {
        String keybindName = UPGRADE_PREFIX + upgradeID;

        GameSettings gameSettings = Minecraft.getInstance().gameSettings;
        for (KeyBinding keyBinding : gameSettings.keyBindings) {
            if (keyBinding != null && keyBinding.getKeyDescription().equals(keybindName)) {
                if (keyCode >= 0) {
                    keyBinding.setKeyModifierAndCode(modifier, InputMappings.Type.KEYSYM.getOrMakeInput(keyCode));
                    KeyBinding.resetKeyBindingArrayAndHash();
                    gameSettings.saveOptions();
                }
                return keyBinding;
            }
        }
        // If the keybind wasn't registered yet, look for it in the Minecraft options.txt file (which
        // we scanned in ClientProxy#getAllKeybindsFromOptionsFile() during pre-init)
        if (keyCode < 0) {
            if (ClientSetup.keybindToKeyCodes.containsKey(keybindName)) {
                Pair<Integer,KeyModifier> binding = ClientSetup.keybindToKeyCodes.get(keybindName);
                keyCode = binding.getLeft();
                modifier = binding.getRight();
            } else {
                return null;
            }
        }

        KeyBinding keyBinding = makeKeyBinding(keyCode, modifier);
        ClientRegistry.registerKeyBinding(keyBinding);
        KeyBinding.resetKeyBindingArrayAndHash();
        gameSettings.saveOptions();
        return keyBinding;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTooltip, boolean shiftPressed) {
        if (keyBinding != null) {
            String s = keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier() + " + " : "";
            curTooltip.add(xlate("pneumaticcraft.gui.keybindBoundKey", s + I18n.format(keyBinding.getKey().getTranslationKey())));
        }
        if (!isAwaitingKey) {
            curTooltip.add(xlate("pneumaticcraft.gui.keybindRightClickToSet"));
            if (keyBinding != null && keyBinding.getKey().getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
                curTooltip.add(xlate("pneumaticcraft.gui.keybindShiftRightClickToClear"));
            }
        }
    }

    public String getUpgradeId() {
        return upgradeID;
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class KeyDispatcher {
        // maps upgrade ID to keybind widget
        private static final Map<String, WidgetKeybindCheckBox> id2checkBox = new HashMap<>();
        // maps "<keycode>/<modifier>" to keybind widget
        private static final Map<String, WidgetKeybindCheckBox> dispatchMap = new HashMap<>();

        @SubscribeEvent
        public static void onKeyPress(InputEvent.KeyInputEvent event) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                WidgetKeybindCheckBox cb = dispatchMap.get(event.getKey() + "/" + event.getModifiers());
                if (cb != null) {
                    cb.handleClick(0, 0, 0);
                }
            }
        }

        static void addKeybind(KeyBinding keyBinding, WidgetKeybindCheckBox widget) {
            String key = keyBinding.getKey().getKeyCode() + "/" + keyModifierToInt(keyBinding.getKeyModifier());
            dispatchMap.put(key, widget);
        }

        static void removeKeybind(KeyBinding keyBinding) {
            String key = keyBinding.getKey().getKeyCode() + "/" + keyModifierToInt(keyBinding.getKeyModifier());
            dispatchMap.remove(key);
        }

        static void cleanupKeybind(String upgradeID) {
            dispatchMap.values().removeIf(w -> w.upgradeID.equals(upgradeID));
        }

        private static int keyModifierToInt(KeyModifier km) {
            switch (km) {
                case SHIFT: return GLFW.GLFW_MOD_SHIFT;
                case CONTROL: return GLFW.GLFW_MOD_CONTROL;
                case ALT: return GLFW.GLFW_MOD_ALT;
                default: return 0;
            }
        }
    }
}
