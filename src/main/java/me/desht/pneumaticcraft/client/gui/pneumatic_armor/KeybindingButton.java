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
import org.lwjgl.glfw.GLFW;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class KeybindingButton extends WidgetButtonExtended {
    private final KeyBinding keyBinding;
    private final ITextComponent origButtonText;
    private boolean bindingMode = false;

    public KeybindingButton(int startX, int startY, int xSize, int ySize, ITextComponent buttonText, KeyBinding keyBinding) {
        super(startX, startY, xSize, ySize, buttonText);
        this.keyBinding = keyBinding;
        this.origButtonText = buttonText;
        addTooltip();
    }

    private void addTooltip() {
        setTooltipText(xlate("pneumaticcraft.gui.keybindBoundKey", ClientUtils.translateKeyBind(keyBinding)));
    }

    @Override
    public void onPress() {
        bindingMode = !bindingMode;

        if (bindingMode) {
            setMessage(xlate("pneumaticcraft.gui.setKeybind").mergeStyle(TextFormatting.YELLOW));
            setTooltipText(StringTextComponent.EMPTY);
        } else {
            setMessage(origButtonText);
            addTooltip();
        }
    }

    public boolean receiveKey(InputMappings.Type type, int keyCode) {
        if (bindingMode) {
            InputMappings.Input input = type.getOrMakeInput(keyCode);
            if (!KeyModifier.isKeyCodeModifier(input)) {
                if (type == InputMappings.Type.KEYSYM && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    keyBinding.setKeyModifierAndCode(KeyModifier.NONE, InputMappings.INPUT_INVALID);
                    Minecraft.getInstance().gameSettings.setKeyBindingCode(keyBinding, InputMappings.INPUT_INVALID);
                    Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
                } else {
                    keyBinding.setKeyModifierAndCode(KeyModifier.getActiveModifier(), input);
                    Minecraft.getInstance().gameSettings.setKeyBindingCode(keyBinding, input);
                    Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                }
                KeyBinding.resetKeyBindingArrayAndHash();
                onPress();
                return true;
            }
        }
        return false;
    }

}
