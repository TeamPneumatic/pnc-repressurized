package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.FMLClientHandler;

class KeybindingButton extends GuiButtonSpecial {
    private final KeyBinding keyBinding;
    private final String origButtonText;
    private boolean bindingMode = false;

    KeybindingButton(int buttonID, int startX, int startY, int xSize, int ySize, String buttonText, KeyBinding keyBinding) {
        super(buttonID, startX, startY, xSize, ySize, buttonText);
        this.keyBinding = keyBinding;
        this.origButtonText = buttonText;
        addTooltip();
    }

    private void addTooltip() {
        setTooltipText("Bound to: " + TextFormatting.GREEN + keyBinding.getDisplayName());
    }

    void toggleKeybindMode() {
        bindingMode = !bindingMode;

        if (bindingMode) {
            displayString = TextFormatting.YELLOW + "Press a key to set keybind";
            setTooltipText("");
        } else {
            displayString = origButtonText;
            addTooltip();
        }
    }

    boolean receiveKey(int key) {
        if (bindingMode && !KeyModifier.isKeyCodeModifier(key)) {
            keyBinding.setKeyModifierAndCode(KeyModifier.getActiveModifier(), key);
            KeyBinding.resetKeyBindingArrayAndHash();
            FMLClientHandler.instance().getClient().gameSettings.saveOptions();
            FMLClientHandler.instance().getClient().player.playSound(SoundEvents.BLOCK_NOTE_CHIME, 1.0f, 1.0f);
            toggleKeybindMode();
            return true;
        } else {
            return false;
        }
    }

}
