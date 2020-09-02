package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.ClientSetup;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorFeatureStatus;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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
    private static WidgetKeybindCheckBox coreComponents;

    private final ResourceLocation upgradeID;
    private boolean isAwaitingKey;
    private ITextComponent oldCheckboxText;
    private KeyBinding keyBinding;

    private WidgetKeybindCheckBox(ResourceLocation upgradeID, int x, int y, int color, Consumer<WidgetCheckBox> pressable) {
        super(x, y, color,
                xlate("pneumaticcraft.gui.enableModule", xlate(ArmorUpgradeRegistry.getStringKey(upgradeID))),
                pressable);

        this.upgradeID = upgradeID;
        this.keyBinding = findSavedKeybind();
    }

    public static WidgetKeybindCheckBox getOrCreate(ResourceLocation upgradeID, int x, int y, int color, Consumer<WidgetCheckBox> pressable) {
        WidgetKeybindCheckBox newCheckBox = KeyDispatcher.id2checkBox.get(upgradeID);
        if (newCheckBox == null) {
            newCheckBox = new WidgetKeybindCheckBox(upgradeID, x, y, color, pressable);
            newCheckBox.checked = ArmorFeatureStatus.INSTANCE.isUpgradeEnabled(upgradeID);
            if (newCheckBox.keyBinding != null) {
                KeyDispatcher.addKeybind(newCheckBox.keyBinding, newCheckBox);
            }
            KeyDispatcher.id2checkBox.put(upgradeID, newCheckBox);
            if (upgradeID.equals(ArmorUpgradeRegistry.getInstance().coreComponentsHandler.getID())) {
                // stash this one since it's referenced a lot
                coreComponents = newCheckBox;
            }
        }
        return newCheckBox;
    }

    public static WidgetKeybindCheckBox get(ResourceLocation upgradeID) {
        return KeyDispatcher.id2checkBox.get(upgradeID);
    }

    public static WidgetKeybindCheckBox forUpgrade(IArmorUpgradeHandler handler) {
        return get(handler.getID());
    }

    public static WidgetKeybindCheckBox forUpgrade(IArmorUpgradeClientHandler handler) {
        return get(handler.getCommonHandler().getID());
    }

    public static WidgetKeybindCheckBox getCoreComponents() {
        return coreComponents;
    }

    public static boolean isHandlerEnabled(IArmorUpgradeHandler handler) {
        return forUpgrade(handler).checked;
    }

    private KeyBinding makeKeyBinding(int keyCode, KeyModifier modifier) {
        return new KeyBinding(ArmorUpgradeRegistry.getStringKey(upgradeID), KeyConflictContext.IN_GAME, modifier,
                InputMappings.Type.KEYSYM, keyCode, Names.PNEUMATIC_KEYBINDING_CATEGORY);
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
            CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
            for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
//                List<IArmorUpgradeClientHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                List<IArmorUpgradeHandler> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
                for (int i = 0; i < upgradeHandlers.size(); i++) {
                    IArmorUpgradeHandler upgradeHandler = upgradeHandlers.get(i);
                    if ((upgradeHandler.getID()).equals(upgradeID) && commonArmorHandler.isUpgradeInserted(slot, i)) {
                        NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, coreComponents.checked && checked, slot));
                        commonArmorHandler.setUpgradeEnabled(slot, (byte)i, coreComponents.checked && checked);
                        HUDHandler.getInstance().addFeatureToggleMessage(ArmorUpgradeRegistry.getStringKey(upgradeID), checked);
                        break;
                    }
                }
                if (upgradeID.equals(ArmorUpgradeRegistry.getInstance().coreComponentsHandler.getID())) {
                    for (int i = 0; i < upgradeHandlers.size(); i++) {
                        boolean state = WidgetKeybindCheckBox.forUpgrade(upgradeHandlers.get(i)).checked;
                        NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, coreComponents.checked && state, slot));
                        commonArmorHandler.setUpgradeEnabled(slot, (byte)i, coreComponents.checked && state);
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
            String keybindName = ArmorUpgradeRegistry.getStringKey(upgradeID);
            ClientSetup.keybindToKeyCodes.put(keybindName, Pair.of(GLFW.GLFW_KEY_UNKNOWN, KeyModifier.NONE));
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
                KeyDispatcher.addKeybind(keyBinding, KeyDispatcher.id2checkBox.get(upgradeID));
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
        String keybindName = ArmorUpgradeRegistry.getStringKey(upgradeID);
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
        // we scanned in ClientSetup#getAllKeybindsFromOptionsFile() during client init)
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
            curTooltip.add(xlate("pneumaticcraft.gui.keybindBoundKey",
                    TextFormatting.YELLOW + ClientUtils.translateKeyBind(keyBinding)));
        }
        if (!isAwaitingKey) {
            curTooltip.add(xlate("pneumaticcraft.gui.keybindRightClickToSet"));
            if (keyBinding != null && keyBinding.getKey().getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
                curTooltip.add(xlate("pneumaticcraft.gui.keybindShiftRightClickToClear"));
            }
        }
    }

    public ResourceLocation getUpgradeId() {
        return upgradeID;
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class KeyDispatcher {
        // maps upgrade ID to keybind widget
        private static final Map<ResourceLocation, WidgetKeybindCheckBox> id2checkBox = new HashMap<>();
        // maps "<keycode>/<modifier>" to keybind widget
        private static final Map<String, WidgetKeybindCheckBox> dispatchMap = new HashMap<>();

        @SubscribeEvent
        public static void onKeyPress(InputEvent.KeyInputEvent event) {
            if (Minecraft.getInstance().currentScreen == null && event.getAction() == GLFW.GLFW_PRESS) {
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

        static void cleanupKeybind(ResourceLocation upgradeID) {
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
