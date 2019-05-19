package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class WidgetComboBox extends WidgetTextField {

    private final ArrayList<String> elements = new ArrayList<>();
    private final FontRenderer fontRenderer;
    private boolean enabled = true;
    private boolean fixedOptions;
    private boolean shouldSort = true;
    private int selectedIndex = -1;

    public WidgetComboBox(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
    }

    public WidgetComboBox setElements(Collection<String> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
        if (shouldSort) Collections.sort(this.elements);
        return this;
    }

    public WidgetComboBox setElements(String[] elements) {
        this.elements.clear();
        this.elements.ensureCapacity(elements.length);
        this.elements.addAll(Arrays.asList(elements));
        if (shouldSort) Collections.sort(this.elements);
        return this;
    }

    public WidgetComboBox setShouldSort(boolean shouldSort) {
        this.shouldSort = shouldSort;
        return this;
    }

    private List<String> getApplicableElements() {
        return elements.stream()
                .filter(element -> fixedOptions || element.toLowerCase().contains(getText().toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void postRender(int mouseX, int mouseY, float partialTick) {
        super.postRender(mouseX, mouseY, partialTick);

        if (enabled && isFocused()) {
            List<String> applicableElements = getApplicableElements();
            GlStateManager.translate(0, 0, 300);
            drawRect(x - 1, y + height + 1, x + width + 1, y + height + 3 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFFA0A0A0);
            drawRect(x, y + height + 1, x + width, y + height + 2 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFF000000);
            for (int i = 0; i < applicableElements.size(); i++) {
                String element = applicableElements.get(i);
                fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(element, getWidth()), x + 4, y + height + 2 + i * fontRenderer.FONT_HEIGHT, 0xE0E0E0);
                fontRenderer.drawString("\u25b2", x + width - 6, y + 1, 0xc0c0c0);
            }
            GlStateManager.translate(0, 0, -300);
        } else {
            fontRenderer.drawString("\u25bc", x + width - 6, y + 1, 0xc0c0c0);
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
        if (!fixedOptions || button != 1) super.onMouseClicked(mouseX, mouseY, button);

        if (enabled) {
            if (mouseY < y + height && mouseX > x + width - 8 && isFocused()) {
                setFocused(false);
            } else {
                setFocused(true);
                List<String> applicableElements = getApplicableElements();
                for (int i = 0; i < applicableElements.size(); i++) {
                    if (new Rectangle(x - 1, y + height + 2 + i * fontRenderer.FONT_HEIGHT, width, fontRenderer.FONT_HEIGHT).contains(mouseX, mouseY)) {
                        setText(applicableElements.get(i));
                        selectedIndex = i;
                        listener.onKeyTyped(this);
                        setFocused(false);
                        break;
                    }
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

    public int getSelectedElementIndex() {
        return selectedIndex;
    }

    public void selectElement(int index) {
        if (index >= 0 && index < elements.size()) {
            selectedIndex = index;
            setText(elements.get(index));
        }
    }
}
