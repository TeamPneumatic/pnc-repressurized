package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

public class WidgetComboBox extends WidgetTextField {

    private final ArrayList<String> elements = new ArrayList<>();
    private final FontRenderer fontRenderer;
    private boolean enabled = true;
    private boolean fixedOptions;

    public WidgetComboBox(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
    }

    public WidgetComboBox setElements(Collection<String> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
        Collections.sort(this.elements);
        return this;
    }

    public WidgetComboBox setElements(String[] elements) {
        this.elements.clear();
        this.elements.ensureCapacity(elements.length);
        this.elements.addAll(Arrays.asList(elements));
        Collections.sort(this.elements);
        return this;
    }

    private List<String> getApplicableElements() {
        List<String> list = new ArrayList<String>();
        for (String element : elements) {
            if (fixedOptions || element.toLowerCase().contains(getText().toLowerCase())) list.add(element);
        }
        return list;
    }

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick) {
        super.postRender(mouseX, mouseY, partialTick);
        if (enabled && isFocused()) {
            List<String> applicableElements = getApplicableElements();
            drawRect(x - 1, y + height + 1, x + width + 1, y + height + 3 + applicableElements.size() * fontRenderer.FONT_HEIGHT, -6250336);
            drawRect(x, y + height + 1, x + width, y + height + 2 + applicableElements.size() * fontRenderer.FONT_HEIGHT, -16777216);
            for (int i = 0; i < applicableElements.size(); i++) {
                String element = applicableElements.get(i);
                fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(element, getWidth()), x + 4, y + height + 2 + i * fontRenderer.FONT_HEIGHT, 14737632);
            }
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        if (!fixedOptions || button != 1) super.onMouseClicked(mouseX, mouseY, button);
        if (enabled) {
            setFocused(true);
            List<String> applicableElements = getApplicableElements();
            for (int i = 0; i < applicableElements.size(); i++) {
                if (new Rectangle(x - 1, y + height + 2 + i * fontRenderer.FONT_HEIGHT, width, fontRenderer.FONT_HEIGHT).contains(mouseX, mouseY)) {
                    setText(applicableElements.get(i));
                    listener.onKeyTyped(this);
                    setFocused(false);
                    break;
                }
            }
        }
    }

    @Override
    public void onMouseClickedOutsideBounds(int mouseX, int mouseY, int button) {
        setFocused(false);
    }

    @Override
    public Rectangle getBounds() {
        return enabled && isFocused() ? new Rectangle(x, y, width, height + 2 + getApplicableElements().size() * fontRenderer.FONT_HEIGHT) : super.getBounds();
    }

    @Override
    public boolean onKey(char key, int keyCode) {
        if (fixedOptions) return false;
        if (enabled && isFocused() && keyCode == Keyboard.KEY_TAB) {//Auto-complete
            List<String> applicableElements = getApplicableElements();
            if (applicableElements.size() > 0) {
                setText(applicableElements.get(0));
                listener.onKeyTyped(this);
                return true;
            } else {
                return super.onKey(key, keyCode);
            }
        } else {
            return super.onKey(key, keyCode);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
    }

    public WidgetComboBox setFixedOptions() {
        fixedOptions = true;
        return this;
    }
}
