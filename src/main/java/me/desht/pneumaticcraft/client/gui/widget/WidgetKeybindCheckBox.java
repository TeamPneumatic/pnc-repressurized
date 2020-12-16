package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorFeatureStatus;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetKeybindCheckBox extends WidgetCheckBox implements ITooltipProvider {
    private static WidgetKeybindCheckBox coreComponents;

    private final ResourceLocation upgradeID;
    private boolean isListeningForBinding;
    private ITextComponent oldCheckboxText;
    private KeyBinding keyBinding;

    private WidgetKeybindCheckBox(ResourceLocation upgradeID, int x, int y, int color, Consumer<WidgetCheckBox> pressable) {
        super(x, y, color,
                xlate("pneumaticcraft.gui.enableModule", xlate(ArmorUpgradeRegistry.getStringKey(upgradeID))),
                pressable);

        this.upgradeID = upgradeID;
        this.keyBinding = findKeybind();
    }

    public static WidgetKeybindCheckBox getOrCreate(ResourceLocation upgradeID, int x, int y, int color, Consumer<WidgetCheckBox> pressable) {
        WidgetKeybindCheckBox newCheckBox = KeyDispatcher.id2checkBox.get(upgradeID);
        if (newCheckBox == null) {
            newCheckBox = new WidgetKeybindCheckBox(upgradeID, x, y, color, pressable);
            newCheckBox.checked = ArmorFeatureStatus.INSTANCE.isUpgradeEnabled(upgradeID);
            KeyDispatcher.id2checkBox.put(upgradeID, newCheckBox);
            if (newCheckBox.keyBinding != null) {
                KeyDispatcher.desc2checkbox.put(newCheckBox.keyBinding.getKeyDescription(), newCheckBox);
            }
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

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.clicked(x, y)) {
            this.playDownSound(Minecraft.getInstance().getSoundHandler());
            if (handleClick(x, y, button)) {
                return true;
            } else if (isListeningForBinding) {
                // add a mouse binding
                InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(button);
                setupKeyOrMouseBind(input);
                return true;
            }
        }
        return false;
    }

    private boolean handleClick(double mouseX, double mouseY, int button) {
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
            return true;
        } else if (button == 1) {
            // right click - clear or set up key binding
            if (Screen.hasShiftDown()) {
                clearKeybind();
            } else {
                isListeningForBinding = !isListeningForBinding;
                if (isListeningForBinding) {
                    oldCheckboxText = getMessage();
                    setMessage(xlate("pneumaticcraft.gui.setKeybind").mergeStyle(TextFormatting.YELLOW));
                } else {
                    setMessage(oldCheckboxText);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isListeningForBinding) {
            InputMappings.Input input = InputMappings.Type.KEYSYM.getOrMakeInput(keyCode);
            if (!KeyModifier.isKeyCodeModifier(input)) {
                setupKeyOrMouseBind(input);
            }
            return true;
        }
        return false;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTooltip, boolean shiftPressed) {
        if (keyBinding != null) {
            curTooltip.add(xlate("pneumaticcraft.gui.keybindBoundKey", ClientUtils.translateKeyBind(keyBinding)));
            if (!isListeningForBinding) {
                curTooltip.add(xlate("pneumaticcraft.gui.keybindRightClickToSet").mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                if (keyBinding.getKey().getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
                    curTooltip.add(xlate("pneumaticcraft.gui.keybindShiftRightClickToClear").mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                }
            }
        }
    }

    private void setupKeyOrMouseBind(InputMappings.Input input) {
        isListeningForBinding = false;
        keyBinding = findAndUpdateKeybind(input, KeyModifier.getActiveModifier());
        setMessage(oldCheckboxText);
    }

    private void clearKeybind() {
        if (keyBinding != null) {
            keyBinding.setKeyModifierAndCode(KeyModifier.NONE, InputMappings.INPUT_INVALID);
            Minecraft.getInstance().gameSettings.setKeyBindingCode(keyBinding, InputMappings.INPUT_INVALID);
            KeyBinding.resetKeyBindingArrayAndHash();
            Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
        }
    }

    /**
     * Attempt to find the registered keybind for this widget.
     * @return the keybind, or null if no keybind has been registered for this widget
     */
    private KeyBinding findKeybind() {
        return findAndUpdateKeybind(null, KeyModifier.NONE);
    }

    /**
     * Find the registered key binding for this widget, and possibly update it. Note that this widget might not
     * have a toggle keybind, making it a dummy placeholder widget (e.g. hacking handler); but a widget is created
     * for every upgrade regardless of whether it's used.
     *
     * @param input the input to update (if null, don't attempt to update)
     * @param modifier key modifier (may be NONE)
     * @return the keybind, or null if no toggle keybind has been registered for this widget
     */
    private KeyBinding findAndUpdateKeybind(InputMappings.Input input, KeyModifier modifier) {
        String keybindName = IArmorUpgradeHandler.getStringKey(upgradeID);
        GameSettings gameSettings = Minecraft.getInstance().gameSettings;
        for (KeyBinding keyBinding : gameSettings.keyBindings) {
            if (keyBinding != null && keyBinding.getKeyDescription().equals(keybindName)) {
                if (input != null) {
                    keyBinding.setKeyModifierAndCode(modifier, input);
                    Minecraft.getInstance().gameSettings.setKeyBindingCode(keyBinding, input);
                    KeyBinding.resetKeyBindingArrayAndHash();
                    Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                }
                return keyBinding;
            }
        }
        return null;
    }

    public ResourceLocation getUpgradeId() {
        return upgradeID;
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class KeyDispatcher {
        // maps upgrade ID to keybind widget
        private static final Map<ResourceLocation, WidgetKeybindCheckBox> id2checkBox = new HashMap<>();
        // maps keybind ID (description) to keybind widget
        private static final Map<String, WidgetKeybindCheckBox> desc2checkbox = new HashMap<>();
        // thanks forge for caching these
        private static final KeyBindingMap KEY_BINDING_MAP = new KeyBindingMap();

        @SubscribeEvent
        public static void onKeyPress(InputEvent.KeyInputEvent event) {
            if (Minecraft.getInstance().currentScreen == null && event.getAction() == GLFW.GLFW_PRESS) {
                KeyBinding binding = KEY_BINDING_MAP.lookupActive(InputMappings.Type.KEYSYM.getOrMakeInput(event.getKey()));
                if (binding != null) {
                    getBoundWidget(binding.getKeyDescription()).ifPresent(w -> w.handleClick(0, 0, 0));
                }
            }
        }

        @SubscribeEvent
        public static void onMouseClick(InputEvent.MouseInputEvent event) {
            if (Minecraft.getInstance().currentScreen == null && event.getAction() == GLFW.GLFW_PRESS) {
                KeyBinding binding = KEY_BINDING_MAP.lookupActive(InputMappings.Type.MOUSE.getOrMakeInput(event.getButton()));
                if (binding != null) {
                    getBoundWidget(binding.getKeyDescription()).ifPresent(w -> w.handleClick(0, 0, 0));
                }
            }
        }

        private static Optional<WidgetKeybindCheckBox> getBoundWidget(String key) {
            return Optional.ofNullable(desc2checkbox.get(key));
        }
    }
}
