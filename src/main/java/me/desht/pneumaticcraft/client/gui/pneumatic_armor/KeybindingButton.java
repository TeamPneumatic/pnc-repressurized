package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.client.settings.KeyModifier;

class KeybindingButton extends WidgetButtonExtended {
    private final KeyBinding keyBinding;
    private final String origButtonText;
    private boolean bindingMode = false;

    KeybindingButton(int startX, int startY, int xSize, int ySize, String buttonText, KeyBinding keyBinding, IPressable pressable) {
        super(startX, startY, xSize, ySize, buttonText, pressable);
        this.keyBinding = keyBinding;
        this.origButtonText = buttonText;
        addTooltip();
    }

    private void addTooltip() {
        setTooltipText(I18n.format("gui.keybindBoundKey", I18n.format(keyBinding.getKey().getTranslationKey())));
    }

    void toggleKeybindMode() {
        bindingMode = !bindingMode;

        if (bindingMode) {
            setMessage(I18n.format("gui.setKeybind"));
            setTooltipText("");
        } else {
            setMessage(origButtonText);
            addTooltip();
        }
    }

    boolean receiveKey(int keyCode) {
        InputMappings.Input input = InputMappings.Type.KEYSYM.getOrMakeInput(keyCode);
        if (bindingMode && !KeyModifier.isKeyCodeModifier(input)) {
            keyBinding.setKeyModifierAndCode(KeyModifier.getActiveModifier(), input);
            KeyBinding.resetKeyBindingArrayAndHash();
            Minecraft.getInstance().gameSettings.saveOptions();
            Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
            toggleKeybindMode();
            return true;
        } else {
            return false;
        }
    }

}
