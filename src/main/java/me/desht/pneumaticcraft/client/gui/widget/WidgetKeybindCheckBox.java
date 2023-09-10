/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.ICheckboxWidget;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.CoreComponentsClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
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
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyMappingLookup;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetKeybindCheckBox extends WidgetCheckBox {
    // maps upgrade ID to keybind widget
    private static final Map<ResourceLocation, WidgetKeybindCheckBox> id2checkBox = new HashMap<>();

    private static WidgetKeybindCheckBox coreComponents;

    private final ResourceLocation upgradeID;
    private ResourceLocation ownerUpgradeID = null;
    private boolean isListeningForBinding;
    private Component oldCheckboxText;

    private WidgetKeybindCheckBox(ResourceLocation upgradeID, int x, int y, int color, Consumer<ICheckboxWidget> pressable) {
        super(x, y, color,
                xlate("pneumaticcraft.gui.enableModule", xlate(IArmorUpgradeHandler.getStringKey(upgradeID))),
                pressable);

        this.oldCheckboxText = getMessage();
        this.upgradeID = upgradeID;
    }

    public static WidgetKeybindCheckBox getOrCreate(ResourceLocation upgradeID, int x, int y, int color, Consumer<ICheckboxWidget> pressable) {
        return id2checkBox.computeIfAbsent(upgradeID, id -> {
            WidgetKeybindCheckBox newCheckBox = new WidgetKeybindCheckBox(id, x, y, color, pressable);
            newCheckBox.checked = ArmorFeatureStatus.INSTANCE.isUpgradeEnabled(id);
            KeyMapping keyBinding = ClientArmorRegistry.getInstance().getKeybindingForUpgrade(id);
            if (keyBinding != null) {
                KeyDispatcher.desc2checkbox.put(keyBinding.getName(), newCheckBox);
            }
            if (id.equals(CommonUpgradeHandlers.coreComponentsHandler.getID())) {
                // stash this one since it's referenced a lot
                coreComponents = newCheckBox;
            }
            return newCheckBox;
        });
    }

    public static WidgetKeybindCheckBox get(ResourceLocation upgradeID) {
        return id2checkBox.get(upgradeID);
    }

    public static WidgetKeybindCheckBox forUpgrade(IArmorUpgradeHandler<?> handler) {
        return get(handler.getID());
    }

    public static WidgetKeybindCheckBox forUpgrade(IArmorUpgradeClientHandler<?> handler) {
        return get(handler.getID());
    }

    public static WidgetKeybindCheckBox getCoreComponents() {
        return coreComponents;
    }

    @Override
    public ICheckboxWidget withOwnerUpgradeID(ResourceLocation ownerUpgradeID) {
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
                InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(button);
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
            ArmorFeatureStatus.INSTANCE.saveIfChanged();

            if (entry != null) {
                EquipmentSlot slot = entry.getEquipmentSlot();
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
                updateBinding(InputConstants.UNKNOWN);
            } else {
                isListeningForBinding = !isListeningForBinding;
                if (isListeningForBinding) {
                    oldCheckboxText = getMessage();
                    setMessage(xlate("pneumaticcraft.gui.setKeybind").withStyle(ChatFormatting.YELLOW));
                } else {
                    setMessage(oldCheckboxText);
                }
            }
            return true;
        }
        return false;
    }

    private void toggleUpgrade(CommonArmorHandler commonArmorHandler, EquipmentSlot slot, byte idx) {
        // set the on/off state for this upgrade on both client and server
        NetworkHandler.sendToServer(new PacketToggleArmorFeature(slot, idx, coreComponents.checked && checked));
        commonArmorHandler.setUpgradeEnabled(slot, idx, coreComponents.checked && checked);
        HUDHandler.getInstance().addFeatureToggleMessage(IArmorUpgradeHandler.getStringKey(upgradeID), checked);
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
            ClientArmorRegistry.getInstance()
                    .getClientHandler(CommonUpgradeHandlers.coreComponentsHandler, CoreComponentsClientHandler.class)
                    .onResolutionChanged();
        } else {
            Minecraft.getInstance().player.playSound(ModSounds.MINIGUN_STOP.get(), 1f, 0.5f);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isListeningForBinding) {
            InputConstants.Key input = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
            if (!KeyModifier.isKeyCodeModifier(input)) {
                updateBinding(input);
            }
            return true;
        }
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        buildTooltip();
    }

    private void buildTooltip() {
        KeyMapping keyBinding = ClientArmorRegistry.getInstance().getKeybindingForUpgrade(upgradeID);
        String k = IArmorUpgradeHandler.getStringKey(upgradeID) + ".desc";
        if (keyBinding == null && !I18n.exists(k)) {
            setTooltip(null);
            return;
        }

        List<Component> l = new ArrayList<>();
        if (I18n.exists(k)) {
            l.addAll(GuiUtils.xlateAndSplit(k));
        }
        if (keyBinding != null) {
            l.add(xlate("pneumaticcraft.gui.keybindBoundKey", ClientUtils.translateKeyBind(keyBinding)).withStyle(ChatFormatting.GOLD));
            if (!isListeningForBinding) {
                l.add(xlate("pneumaticcraft.gui.keybindRightClickToSet").withStyle(ChatFormatting.GRAY));
                if (keyBinding.getKey().getValue() != GLFW.GLFW_KEY_UNKNOWN) {
                    l.add(xlate("pneumaticcraft.gui.keybindShiftRightClickToClear").withStyle(ChatFormatting.GRAY));
                }
            }
        }

        setTooltip(Tooltip.create(PneumaticCraftUtils.combineComponents(l)));
    }

    /**
     * Update the key/mouse binding for this checkbox.
     *
     * @param input the new input binding, or InputMappings.INPUT_INVALID to clear the binding
     */
    private void updateBinding(InputConstants.Key input) {
        isListeningForBinding = false;
        KeyMapping keyBinding = ClientArmorRegistry.getInstance().getKeybindingForUpgrade(upgradeID);
        if (keyBinding != null) {
            KeyModifier mod = input == InputConstants.UNKNOWN ? KeyModifier.NONE : KeyModifier.getActiveModifier();
            keyBinding.setKeyModifierAndCode(mod, input);
            Minecraft.getInstance().options.setKey(keyBinding, input);
            KeyMapping.resetMapping();
            Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_CHIME.get(), 1.0f, input == InputConstants.UNKNOWN ? 0.5f :1.0f);
        }
        setMessage(oldCheckboxText);
    }

    @Override
    public ResourceLocation getUpgradeId() {
        return upgradeID;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
    public static class KeyDispatcher {
        // maps keybind ID (description) to keybind widget
        private static final Map<String, WidgetKeybindCheckBox> desc2checkbox = new HashMap<>();
        // thanks forge for caching these
        private static final KeyMappingLookup KEY_BINDING_MAP = new KeyMappingLookup();

        @SubscribeEvent
        public static void onKeyPress(InputEvent.Key event) {
            if (Minecraft.getInstance().screen == null && event.getAction() == GLFW.GLFW_PRESS) {
                KEY_BINDING_MAP.getAll(InputConstants.Type.KEYSYM.getOrCreate(event.getKey()))
                        .forEach(binding -> getBoundWidget(binding.getName()).ifPresent(w -> w.handleClick(0, 0, 0)));
            }
        }

        @SubscribeEvent
        public static void onMouseClick(InputEvent.MouseButton event) {
            if (Minecraft.getInstance().screen == null && event.getAction() == GLFW.GLFW_PRESS) {
                KEY_BINDING_MAP.getAll(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))
                        .forEach(binding -> getBoundWidget(binding.getName()).ifPresent(w -> w.handleClick(0, 0, 0)));
            }
        }

        private static Optional<WidgetKeybindCheckBox> getBoundWidget(String key) {
            return Optional.ofNullable(desc2checkbox.get(key));
        }
    }
}
