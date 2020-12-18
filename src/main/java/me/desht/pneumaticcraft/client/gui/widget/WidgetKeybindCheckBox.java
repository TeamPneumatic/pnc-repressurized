package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorFeatureStatus;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk.FeatureSetting;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
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
import java.util.*;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetKeybindCheckBox extends WidgetCheckBox implements ITooltipProvider {
    private static WidgetKeybindCheckBox coreComponents;

    private final ResourceLocation upgradeID;
    private boolean isListeningForBinding;
    private ITextComponent oldCheckboxText;

    private WidgetKeybindCheckBox(ResourceLocation upgradeID, int x, int y, int color, Consumer<WidgetCheckBox> pressable) {
        super(x, y, color,
                xlate("pneumaticcraft.gui.enableModule", xlate(ArmorUpgradeRegistry.getStringKey(upgradeID))),
                pressable);

        this.upgradeID = upgradeID;
    }

    public static WidgetKeybindCheckBox getOrCreate(ResourceLocation upgradeID, int x, int y, int color, Consumer<WidgetCheckBox> pressable) {
        WidgetKeybindCheckBox newCheckBox = KeyDispatcher.id2checkBox.get(upgradeID);
        if (newCheckBox == null) {
            newCheckBox = new WidgetKeybindCheckBox(upgradeID, x, y, color, pressable);
            newCheckBox.checked = ArmorFeatureStatus.INSTANCE.isUpgradeEnabled(upgradeID);
            KeyDispatcher.id2checkBox.put(upgradeID, newCheckBox);
            KeyBinding keyBinding = ArmorUpgradeClientRegistry.getInstance().getKeybindingForUpgrade(upgradeID);
            if (keyBinding != null) {
                KeyDispatcher.desc2checkbox.put(keyBinding.getKeyDescription(), newCheckBox);
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
                updateBinding(input);
                return true;
            }
        }
        return false;
    }

    private boolean handleClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (!coreComponents.checked && this != coreComponents) {
                Minecraft.getInstance().player.playSound(ModSounds.MINIGUN_STOP.get(), 1f, 2f);
                return true;
            }

            // left click - usual toggle action
            super.onClick(mouseX, mouseY);

            ArmorFeatureStatus.INSTANCE.setUpgradeEnabled(upgradeID, checked);
            try {
                ArmorFeatureStatus.INSTANCE.writeToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
            ArmorUpgradeRegistry.ArmorUpgradeEntry entry = ArmorUpgradeRegistry.getInstance().getUpgradeEntry(upgradeID);
            if (entry != null) {
                EquipmentSlotType slot = entry.getSlot();
                byte idx = (byte) entry.getIndex();
                if (commonArmorHandler.isUpgradeInserted(slot, idx)) {
                    toggleUpgrade(commonArmorHandler, slot, idx);
                }
            }
            if (this == coreComponents) {
                toggleAllUpgrades(commonArmorHandler);
            }
            return true;
        } else if (button == 1) {
            // right click - update or clear key binding
            if (Screen.hasShiftDown()) {
                updateBinding(InputMappings.INPUT_INVALID);
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

    private void toggleUpgrade(CommonArmorHandler commonArmorHandler, EquipmentSlotType slot, byte idx) {
        // set the on/off state for this upgrade on both client and server
        NetworkHandler.sendToServer(new PacketToggleArmorFeature(slot, idx, coreComponents.checked && checked));
        commonArmorHandler.setUpgradeEnabled(slot, idx, coreComponents.checked && checked);
        HUDHandler.getInstance().addFeatureToggleMessage(ArmorUpgradeRegistry.getStringKey(upgradeID), checked);
    }

    private void toggleAllUpgrades(CommonArmorHandler commonArmorHandler) {
        // master switch has been clicked: toggle on/off *all* installed upgrades on both client and server
        List<FeatureSetting> features = new ArrayList<>();
        ArmorUpgradeRegistry.getInstance().entries().forEach(entry -> {
            boolean state = coreComponents.checked && WidgetKeybindCheckBox.forUpgrade(entry.getHandler()).checked;
            byte idx = (byte) entry.getIndex();
            features.add(new FeatureSetting(entry.getSlot(), idx, state));
            commonArmorHandler.setUpgradeEnabled(entry.getSlot(), idx, state);
        });
        NetworkHandler.sendToServer(new PacketToggleArmorFeatureBulk(features));
        if (checked) {
            // force pressure stat to recalc its layout (just using .reset() isn't enough)
            ArmorUpgradeClientRegistry.getInstance().getClientHandler(ArmorUpgradeRegistry.getInstance().coreComponentsHandler).onResolutionChanged();
        } else {
            Minecraft.getInstance().player.playSound(ModSounds.MINIGUN_STOP.get(), 1f, 0.5f);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isListeningForBinding) {
            InputMappings.Input input = InputMappings.Type.KEYSYM.getOrMakeInput(keyCode);
            if (!KeyModifier.isKeyCodeModifier(input)) {
                updateBinding(input);
            }
            return true;
        }
        return false;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTooltip, boolean shiftPressed) {
        KeyBinding keyBinding = ArmorUpgradeClientRegistry.getInstance().getKeybindingForUpgrade(upgradeID);
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

    /**
     * Update the key/mouse binding for this checkbox.
     *
     * @param input the new input binding, or InputMappings.INPUT_INVALID to clear the binding
     */
    private void updateBinding(InputMappings.Input input) {
        isListeningForBinding = false;
        KeyBinding keyBinding = ArmorUpgradeClientRegistry.getInstance().getKeybindingForUpgrade(upgradeID);
        if (keyBinding != null) {
            KeyModifier mod = input == InputMappings.INPUT_INVALID ? KeyModifier.NONE : KeyModifier.getActiveModifier();
            keyBinding.setKeyModifierAndCode(mod, input);
            Minecraft.getInstance().gameSettings.setKeyBindingCode(keyBinding, input);
            KeyBinding.resetKeyBindingArrayAndHash();
            Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f, input == InputMappings.INPUT_INVALID ? 0.5f :1.0f);
        }
        setMessage(oldCheckboxText);
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
