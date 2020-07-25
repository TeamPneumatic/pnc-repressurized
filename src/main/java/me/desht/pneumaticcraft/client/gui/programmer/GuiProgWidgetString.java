package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetText;

public class GuiProgWidgetString<T extends ProgWidgetText> extends GuiProgWidgetOptionBase<T> {
    private WidgetTextField textfield;

    public GuiProgWidgetString(T widget, GuiProgrammer guiProgrammer) {
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
        setListener(textfield);
        addButton(textfield);
    }

    @Override
    public void onClose() {
        progWidget.string = textfield.getText();

        super.onClose();
    }
}
