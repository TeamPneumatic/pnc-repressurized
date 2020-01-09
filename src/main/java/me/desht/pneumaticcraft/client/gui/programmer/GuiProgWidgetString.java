package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetText;

public class GuiProgWidgetString extends GuiProgWidgetOptionBase<ProgWidgetText> {
    private WidgetTextField textfield;

    public GuiProgWidgetString(ProgWidgetText widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        textfield = new WidgetTextField(font, guiLeft + 10, guiTop + 20, 160, 10) {
            @Override
            public boolean charTyped(char c, int keyCode) {
                if (c == '\n') {
                    onClose();
                    minecraft.player.closeScreen();
                    return true;
                } else {
                    return super.charTyped(c, keyCode);
                }
            }
        };
        textfield.setMaxStringLength(1000);
        textfield.setText(progWidget.string);
        textfield.setFocused2(true);
        addButton(textfield);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();

        progWidget.string = textfield.getText();
    }
}
