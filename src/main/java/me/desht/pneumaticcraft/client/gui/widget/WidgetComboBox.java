package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.glfw.GLFW;

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
    public void render(int mouseX, int mouseY, float partialTick) {
        super.render(mouseX, mouseY, partialTick);

        if (enabled && isFocused()) {
            List<String> applicableElements = getApplicableElements();
            GlStateManager.translated(0, 0, 300);
            fill(x - 1, y + height + 1, x + width + 1, y + height + 3 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFFA0A0A0);
            fill(x, y + height + 1, x + width, y + height + 2 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFF000000);
            for (int i = 0; i < applicableElements.size(); i++) {
                String element = applicableElements.get(i);
                fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(element, getWidth()), x + 4, y + height + 2 + i * fontRenderer.FONT_HEIGHT, 0xE0E0E0);
                fontRenderer.drawString("\u25b2", x + width - 6, y + 1, 0xc0c0c0);
            }
            GlStateManager.translated(0, 0, -300);
        } else {
            fontRenderer.drawString("\u25bc", x + width - 6, y + 1, 0xc0c0c0);
        }
    }

    @Override
    public void onClick(double x, double y) {
        if (fixedOptions && enabled) {
            if (y < y + height && x > x + width - 8 && isFocused()) {
                setFocused(false);
            } else {
                setFocused(true);
                List<String> applicableElements = getApplicableElements();
                for (int i = 0; i < applicableElements.size(); i++) {
                    if (isHovered) {
                        setText(applicableElements.get(i));
                        selectedIndex = i;
                        setFocused(false);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (fixedOptions) return false;

        if (enabled && isFocused() && keyCode == GLFW.GLFW_KEY_TAB) {//Auto-complete
            List<String> applicableElements = getApplicableElements();
            if (applicableElements.size() > 0) {
                setText(applicableElements.get(0));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
