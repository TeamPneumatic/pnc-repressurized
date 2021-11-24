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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IKeybindingButton;
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

public class KeybindingButton extends WidgetButtonExtended implements IKeybindingButton {
    private final KeyBinding keyBinding;
    private final ITextComponent origButtonText;
    private boolean bindingMode = false;
    private boolean modifier = false;
    private Action action = Action.NONE;

    private enum Action { NONE, ADD, REMOVE }

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
        if (bindingMode) {
            if (!modifier) {
                setBindingMode(false);
            }
        } else {
            setBindingMode(true);
        }
    }

    private void setBindingMode(boolean newMode) {
        bindingMode = newMode;
        if (bindingMode) {
            setMessage(xlate("pneumaticcraft.gui.setKeybind").withStyle(TextFormatting.YELLOW));
            setTooltipText(StringTextComponent.EMPTY);
        } else {
            setMessage(origButtonText);
            addTooltip();
            switch (action) {
                case ADD:
                    Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                    break;
                case REMOVE:
                    Minecraft.getInstance().player.playSound(SoundEvents.GLASS_BREAK, 1.0f, 1.0f);
                    break;
            }
            action = Action.NONE;
        }
    }

    @Override
    public boolean receiveKey(InputMappings.Type type, int keyCode) {
        if (bindingMode) {
            if (type == InputMappings.Type.KEYSYM && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                keyBinding.setKeyModifierAndCode(KeyModifier.NONE, InputMappings.UNKNOWN);
                Minecraft.getInstance().options.setKey(keyBinding, InputMappings.UNKNOWN);
                action = Action.REMOVE;
            } else {
                InputMappings.Input input = type.getOrCreate(keyCode);
                modifier = KeyModifier.isKeyCodeModifier(input);
                keyBinding.setKeyModifierAndCode(KeyModifier.getActiveModifier(), input);
                Minecraft.getInstance().options.setKey(keyBinding, input);
                action = Action.ADD;
            }
            KeyBinding.resetMapping();
            onPress();
            return true;
        }
        return false;
    }

    @Override
    public void receiveKeyReleased() {
        setBindingMode(false);
    }
}
