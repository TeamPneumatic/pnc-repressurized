package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.config.HelmetWidgetDefaults;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.proxy.ClientProxy;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class GuiKeybindCheckBox extends GuiCheckBox implements ITooltipSupplier {
    public static final String UPGRADE_PREFIX = "pneumaticHelmet.upgrade.";

    private boolean isAwaitingKey;
    private String oldCheckboxText;
    private KeyBinding keyBinding;
    private final String keyBindingName;
    private static final Map<String, GuiKeybindCheckBox> trackedCheckboxes = new HashMap<>();
    private static GuiKeybindCheckBox coreComponents;

    public GuiKeybindCheckBox(int x, int y, int color, String text, Consumer<GuiCheckBox> pressable) {
        this(x, y, color, text, text, pressable);
    }

    public GuiKeybindCheckBox(int x, int y, int color, String text, String keyBindingName, Consumer<GuiCheckBox> pressable) {
        super(x, y, color, text, pressable);

        this.keyBindingName = keyBindingName;
        keyBinding = setOrAddKeybind(keyBindingName, -1, KeyModifier.NONE); //get the saved value.
        if (!trackedCheckboxes.containsKey(keyBindingName)) {
            checked = HelmetWidgetDefaults.INSTANCE.getKey(keyBindingName);
            trackedCheckboxes.put(keyBindingName, this);
            MinecraftForge.EVENT_BUS.register(this);
            if (keyBindingName.equals(UPGRADE_PREFIX + "coreComponents")) {
                // stash this one since it's referenced a lot
                coreComponents = this;
            }
        } else {
            checked = trackedCheckboxes.get(keyBindingName).checked;
        }
    }

    public static GuiKeybindCheckBox getCoreComponents() {
        return coreComponents;
    }

    public static GuiKeybindCheckBox fromKeyBindingName(String name) {
        return trackedCheckboxes.get(name);
    }

    public static boolean isHandlerEnabled(IUpgradeRenderHandler handler) {
        return fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + handler.getUpgradeName()).checked;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.clicked(x, y);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    handleClick(x, y, button);
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private void handleClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            super.onClick(mouseX, mouseY);
            GuiKeybindCheckBox trackedBox = trackedCheckboxes.get(keyBindingName);
            if (trackedBox != this) {
                trackedBox.mouseClicked(mouseX, mouseY, button);
            } else {
                HelmetWidgetDefaults.INSTANCE.setKey(keyBindingName, checked);
                try {
                    HelmetWidgetDefaults.INSTANCE.writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CommonArmorHandler hudHandler = CommonArmorHandler.getHandlerForPlayer();
                for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                    List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                    for (int i = 0; i < renderHandlers.size(); i++) {
                        IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                        if ((UPGRADE_PREFIX + upgradeRenderHandler.getUpgradeName()).equals(keyBindingName) && hudHandler.isUpgradeRendererInserted(slot, i)) {
                            NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, coreComponents.checked && checked, slot));
                            hudHandler.setUpgradeRenderEnabled(slot, (byte)i, coreComponents.checked && checked);
                            HUDHandler.instance().addFeatureToggleMessage(keyBindingName, checked);
                            break;
                        }
                    }
                    if (keyBindingName.equals(UPGRADE_PREFIX + "coreComponents")) {
                        for (int i = 0; i < renderHandlers.size(); i++) {
                            boolean state = GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()).checked;
                            NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, coreComponents.checked && state, slot));
                            hudHandler.setUpgradeRenderEnabled(slot, (byte)i, coreComponents.checked && state);
                        }
                    }
                }
            }
        } else {
            if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
                clearKeybinding();
            } else {
                isAwaitingKey = !isAwaitingKey;
                if (isAwaitingKey) {
                    oldCheckboxText = getMessage();
                    setMessage(I18n.format("gui.setKeybind"));
                } else {
                    setMessage(oldCheckboxText);
                }
            }
        }
    }

    private void clearKeybinding() {
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
            keyBinding = new KeyBinding(keyBindingName, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, Names.PNEUMATIC_KEYBINDING_CATEGORY);
            ClientRegistry.registerKeyBinding(keyBinding);
            KeyBinding.resetKeyBindingArrayAndHash();
            ((ClientProxy) PneumaticCraftRepressurized.proxy).keybindToKeyCodes.put(keyBindingName, Pair.of(GLFW.GLFW_KEY_UNKNOWN, KeyModifier.NONE));
            Minecraft.getInstance().gameSettings.saveOptions();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isAwaitingKey) {
            InputMappings.Input input = InputMappings.Type.KEYSYM.getOrMakeInput(keyCode);
            if (KeyModifier.isKeyCodeModifier(input)) {
                return true;
            } else {
                isAwaitingKey = false;
                keyBinding = setOrAddKeybind(keyBindingName, keyCode, KeyModifier.getActiveModifier());
                setMessage(oldCheckboxText);
                if (trackedCheckboxes.containsKey(keyBindingName)) {
                    MinecraftForge.EVENT_BUS.unregister(trackedCheckboxes.get(keyBindingName));
                }
                MinecraftForge.EVENT_BUS.register(this);
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isGameFocused() && keyBinding != null && keyBinding.isPressed()) {
            mouseClicked(0, 0, 0);
        }
    }

    /**
     * @param keybindName name of the key binding
     * @param keyCode     when < 0, this will function as a getter & may return null
     * @param modifier key modifier (may be NONE)
     * @return the key binding
     */
    private static KeyBinding setOrAddKeybind(String keybindName, int keyCode, KeyModifier modifier) {
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
            if (((ClientProxy) PneumaticCraftRepressurized.proxy).keybindToKeyCodes.containsKey(keybindName)) {
                Pair<Integer,KeyModifier> binding = ((ClientProxy) PneumaticCraftRepressurized.proxy).keybindToKeyCodes.get(keybindName);
                keyCode = binding.getLeft();
                modifier = binding.getRight();
            } else {
                return null;
            }
        }
        KeyBinding keyBinding = new KeyBinding(keybindName, KeyConflictContext.IN_GAME, modifier,
                InputMappings.Type.KEYSYM, keyCode, Names.PNEUMATIC_KEYBINDING_CATEGORY);
        ClientRegistry.registerKeyBinding(keyBinding);
        KeyBinding.resetKeyBindingArrayAndHash();
        gameSettings.saveOptions();
        return keyBinding;
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed) {
        if (keyBinding != null) {
            String s = keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier() + " + " : "";
            curTooltip.add(I18n.format("gui.keybindBoundKey", s + I18n.format(keyBinding.getKey().getTranslationKey())));
        }
        if (!isAwaitingKey) {
            curTooltip.add("gui.keybindRightClickToSet");
            if (keyBinding != null && keyBinding.getKey().getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
                curTooltip.add("gui.keybindShiftRightClickToClear");
            }
        }
    }
}
