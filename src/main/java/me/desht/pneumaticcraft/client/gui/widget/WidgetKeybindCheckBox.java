package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoreComponentsClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorFeatureStatus;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk.FeatureSetting;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
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

import java.util.*;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetKeybindCheckBox extends WidgetCheckBox implements ITooltipProvider {
    private static WidgetKeybindCheckBox coreComponents;

    private final ResourceLocation upgradeID;
    private ResourceLocation ownerUpgradeID = null;
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
                KeyDispatcher.desc2checkbox.put(keyBinding.getName(), newCheckBox);
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

    public static WidgetKeybindCheckBox forUpgrade(IArmorUpgradeHandler<?> handler) {
        return get(handler.getID());
    }

    public static WidgetKeybindCheckBox forUpgrade(IArmorUpgradeClientHandler<?> handler) {
        return get(handler.getCommonHandler().getID());
    }

    public static WidgetKeybindCheckBox getCoreComponents() {
        return coreComponents;
    }

    /**
     * Set the upgrade ID of the owning upgrade. Use this for sub-controls, e.g. the builder mode setting on jet boots.
     * @param ownerUpgradeID the upgrade ID of the owning upgrade
     * @return this widget, for fluency
     */
    public WidgetKeybindCheckBox withOwnerUpgradeID(ResourceLocation ownerUpgradeID) {
        this.ownerUpgradeID = ownerUpgradeID;
        return this;
    }

    public static boolean isHandlerEnabled(IArmorUpgradeHandler<?> handler) {
        return forUpgrade(handler).checked;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.clicked(x, y)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (handleClick(x, y, button)) {
                return true;
            } else if (isListeningForBinding) {
                // add a mouse binding
                InputMappings.Input input = InputMappings.Type.MOUSE.getOrCreate(button);
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

            CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
            IArmorUpgradeHandler<?> entry = ArmorUpgradeRegistry.getInstance().getUpgradeEntry(upgradeID);
            IArmorUpgradeHandler<?> ownerEntry = ArmorUpgradeRegistry.getInstance().getUpgradeEntry(ownerUpgradeID);

            if (!checked) {
                // require armor to be ready to switch on a feature (but always allow switching off)
                // for main control: entry != null, ownerEntry == null
                // for sub-control: entry == null, ownerEntry != null
                if (entry != null) {
                    if (this != coreComponents && !commonArmorHandler.isArmorReady(entry.getEquipmentSlot())) return true;
                } else if (ownerEntry != null && !commonArmorHandler.isArmorReady(ownerEntry.getEquipmentSlot())) {
                    return true;
                }
            }

            // left click - usual toggle action
            super.onClick(mouseX, mouseY);

            ArmorFeatureStatus.INSTANCE.setUpgradeEnabled(upgradeID, checked);
            ArmorFeatureStatus.INSTANCE.tryWriteToFile();

            if (entry != null) {
                EquipmentSlotType slot = entry.getEquipmentSlot();
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
                updateBinding(InputMappings.UNKNOWN);
            } else {
                isListeningForBinding = !isListeningForBinding;
                if (isListeningForBinding) {
                    oldCheckboxText = getMessage();
                    setMessage(xlate("pneumaticcraft.gui.setKeybind").withStyle(TextFormatting.YELLOW));
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
            boolean state = coreComponents.checked && WidgetKeybindCheckBox.forUpgrade(entry).checked;
            byte idx = (byte) entry.getIndex();
            features.add(new FeatureSetting(entry.getEquipmentSlot(), idx, state));
            commonArmorHandler.setUpgradeEnabled(entry.getEquipmentSlot(), idx, state);
        });
        NetworkHandler.sendToServer(new PacketToggleArmorFeatureBulk(features));
        if (checked) {
            // force pressure stat to recalc its layout (just using .reset() isn't enough)
            ArmorUpgradeClientRegistry.getInstance()
                    .getClientHandler(ArmorUpgradeRegistry.getInstance().coreComponentsHandler, CoreComponentsClientHandler.class)
                    .onResolutionChanged();
        } else {
            Minecraft.getInstance().player.playSound(ModSounds.MINIGUN_STOP.get(), 1f, 0.5f);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isListeningForBinding) {
            InputMappings.Input input = InputMappings.Type.KEYSYM.getOrCreate(keyCode);
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
        String k = IArmorUpgradeHandler.getStringKey(upgradeID) + ".desc";
        if (I18n.exists(k)) {
            curTooltip.addAll(GuiUtils.xlateAndSplit(k));
        }
        if (keyBinding != null) {
            curTooltip.add(xlate("pneumaticcraft.gui.keybindBoundKey", ClientUtils.translateKeyBind(keyBinding)).withStyle(TextFormatting.GOLD));
            if (!isListeningForBinding) {
                curTooltip.add(xlate("pneumaticcraft.gui.keybindRightClickToSet").withStyle(TextFormatting.GRAY));
                if (keyBinding.getKey().getValue() != GLFW.GLFW_KEY_UNKNOWN) {
                    curTooltip.add(xlate("pneumaticcraft.gui.keybindShiftRightClickToClear").withStyle(TextFormatting.GRAY));
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
            KeyModifier mod = input == InputMappings.UNKNOWN ? KeyModifier.NONE : KeyModifier.getActiveModifier();
            keyBinding.setKeyModifierAndCode(mod, input);
            Minecraft.getInstance().options.setKey(keyBinding, input);
            KeyBinding.resetMapping();
            Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_CHIME, 1.0f, input == InputMappings.UNKNOWN ? 0.5f :1.0f);
        }
        setMessage(oldCheckboxText);
    }

    public ResourceLocation getUpgradeId() {
        return upgradeID;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
    public static class KeyDispatcher {
        // maps upgrade ID to keybind widget
        private static final Map<ResourceLocation, WidgetKeybindCheckBox> id2checkBox = new HashMap<>();
        // maps keybind ID (description) to keybind widget
        private static final Map<String, WidgetKeybindCheckBox> desc2checkbox = new HashMap<>();
        // thanks forge for caching these
        private static final KeyBindingMap KEY_BINDING_MAP = new KeyBindingMap();

        @SubscribeEvent
        public static void onKeyPress(InputEvent.KeyInputEvent event) {
            if (Minecraft.getInstance().screen == null && event.getAction() == GLFW.GLFW_PRESS) {
                KeyBinding binding = KEY_BINDING_MAP.lookupActive(InputMappings.Type.KEYSYM.getOrCreate(event.getKey()));
                if (binding != null) {
                    getBoundWidget(binding.getName()).ifPresent(w -> w.handleClick(0, 0, 0));
                }
            }
        }

        @SubscribeEvent
        public static void onMouseClick(InputEvent.MouseInputEvent event) {
            if (Minecraft.getInstance().screen == null && event.getAction() == GLFW.GLFW_PRESS) {
                KeyBinding binding = KEY_BINDING_MAP.lookupActive(InputMappings.Type.MOUSE.getOrCreate(event.getButton()));
                if (binding != null) {
                    getBoundWidget(binding.getName()).ifPresent(w -> w.handleClick(0, 0, 0));
                }
            }
        }

        private static Optional<WidgetKeybindCheckBox> getBoundWidget(String key) {
            return Optional.ofNullable(desc2checkbox.get(key));
        }
    }
}
