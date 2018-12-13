package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetString;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiProgWidgetString extends GuiProgWidgetOptionBase {
    private WidgetTextField textfield;

    public GuiProgWidgetString(IProgWidget widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();
        textfield = new WidgetTextField(fontRenderer, guiLeft + 10, guiTop + 20, 160, 10);
        textfield.setMaxStringLength(1000);
        textfield.setText(((ProgWidgetString) widget).string);
        textfield.setFocused(true);
        addWidget(textfield);
    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RETURN && textfield.isFocused()) {
            // pressing return also closes this gui
            super.keyTyped('\u001B', Keyboard.KEY_ESCAPE);
        } else {
            super.keyTyped(key, keyCode);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ((ProgWidgetString) widget).string = textfield.getText();
    }
}
