package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.settings.KeyModifier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class KeybindingButton extends WidgetButtonExtended {
    private final KeyBinding keyBinding;
    private final ITextComponent origButtonText;
    private boolean bindingMode = false;

    public KeybindingButton(int startX, int startY, int xSize, int ySize, ITextComponent buttonText, KeyBinding keyBinding, IPressable pressable) {
        super(startX, startY, xSize, ySize, buttonText, pressable);
        this.keyBinding = keyBinding;
        this.origButtonText = buttonText;
        addTooltip();
    }

    private void addTooltip() {
        setTooltipText(xlate("pneumaticcraft.gui.keybindBoundKey", TextFormatting.YELLOW + ClientUtils.translateKeyBind(keyBinding)));
    }

    public void toggleKeybindMode() {
        bindingMode = !bindingMode;

        if (bindingMode) {
            setMessage(xlate("pneumaticcraft.gui.setKeybind"));
            setTooltipText(StringTextComponent.EMPTY);
        } else {
            setMessage(origButtonText);
            addTooltip();
        }
    }

    public boolean receiveKey(int keyCode) {
        if (bindingMode) {
            InputMappings.Input input = InputMappings.Type.KEYSYM.getOrMakeInput(keyCode);
            if (!KeyModifier.isKeyCodeModifier(input)) {
                keyBinding.setKeyModifierAndCode(KeyModifier.getActiveModifier(), input);
                KeyBinding.resetKeyBindingArrayAndHash();
                Minecraft.getInstance().gameSettings.saveOptions();
                Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                toggleKeybindMode();
                return true;
            }
        }
        return false;
    }

}
