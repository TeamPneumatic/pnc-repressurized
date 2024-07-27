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

package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.misc.Symbols;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WidgetComboBox extends WidgetTextField implements IDrawAfterRender {
    private final ArrayList<String> elements = new ArrayList<>();
    private final Font fontRenderer;
    private boolean enabled = true;
    private boolean fixedOptions;
    private boolean shouldSort = true;
    private int selectedIndex = -1;
    private final int baseHeight; // unexpanded height
    private final Consumer<WidgetComboBox> pressable;
    private List<String> applicable = null;
    private boolean expanded = false;

    public WidgetComboBox(Font fontRenderer, int x, int y, int width, int height) {
        this(fontRenderer, x, y, width, height, b -> {});
    }

    public WidgetComboBox(Font fontRenderer, int x, int y, int width, int height, Consumer<WidgetComboBox> pressable) {
        super(fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
        this.baseHeight = height;
        this.pressable = pressable;

        setTextColorUneditable(0xE0E0E0);
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
//        if (applicable == null) {
        applicable = elements.stream()
                .filter(element -> fixedOptions || element.toLowerCase().contains(getValue().toLowerCase()))
                .collect(Collectors.toList());
//        }
        return applicable;
    }

    @Override
    public void insertText(String textToWrite) {
        super.insertText(textToWrite);

        applicable = null; // force recalc
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(fontRenderer, expanded ? Symbols.TRIANGLE_UP : Symbols.TRIANGLE_DOWN, getX() + width - 7, getY() + 1, 0xC0C0C0, false);
    }

    @Override
    public void renderAfterEverythingElse(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (enabled && active && expanded) {
            int x = getX(), y = getY();
            List<String> applicableElements = getApplicableElements();
            graphics.fill(x - 1, y + height + 1, x + width + 1, y + height + 3 + applicableElements.size() * fontRenderer.lineHeight, 0xFFA0A0A0);
            graphics.fill(x,     y + height + 1, x + width,     y + height + 2 + applicableElements.size() * fontRenderer.lineHeight, 0xFF000000);
            int hovered = (mouseY - y - height) / fontRenderer.lineHeight;
            for (int i = 0; i < applicableElements.size(); i++) {
                String element = applicableElements.get(i);
                graphics.drawString(fontRenderer, fontRenderer.plainSubstrByWidth(element, getWidth()), x + 4, y + height + 2 + i * fontRenderer.lineHeight, i == hovered ? 0xFFE080 : 0xE0E0E0);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isVisible() && active && button == 0) {
            int x = getX(), y = getY();
            int h = baseHeight + (expanded ? getApplicableElements().size() * fontRenderer.lineHeight : 0);
            boolean flag = mouseX >= (double)x && mouseX < (double)(x + this.width)
                    && mouseY >= (double)y && mouseY < (double)(y + h);
            if (flag) {
                if (mouseY < y + height) {
                    // in the textfield itself
                    expanded = !expanded;
                } else {
                    // in the drop-down area
                    expanded = false;
                    int i = ((int) mouseY - y - height) / fontRenderer.lineHeight;
                    if (i < getApplicableElements().size()) {
                        setValue(getApplicableElements().get(i));
                        selectedIndex = i;
                        pressable.accept(this);
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (fixedOptions) return false;

        if (enabled && expanded && keyCode == GLFW.GLFW_KEY_TAB) { // Tab completion
            List<String> applicableElements = getApplicableElements();
            if (!applicableElements.isEmpty()) {
                setValue(applicableElements.getFirst());
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char key, int keyCode) {
        return !fixedOptions && super.charTyped(key, keyCode);
    }

    @Override
    public void setEditable(boolean enabled) {
        super.setEditable(enabled);
        this.enabled = enabled;
    }

    public WidgetComboBox setFixedOptions(boolean fixed) {
        fixedOptions = fixed;
        super.setEditable(!fixed);
        applicable = null; // force recalc
        return this;
    }

    public int getSelectedElementIndex() {
        return selectedIndex;
    }

    public void selectElement(int index) {
        if (index >= 0 && index < elements.size()) {
            selectedIndex = index;
            setValue(elements.get(index));
        }
    }

    /**
     * Convenience method: set up a combo box to display the values of an enum
     * @param initialValue the initial value to display
     * @param xlate a function which gets the string representation of a given value of the enum
     * @param <T> enum type
     * @return the combo box
     */
    public final <T extends Enum<T>> WidgetComboBox initFromEnum(T initialValue, Function<T, String> xlate) {
        @SuppressWarnings("unchecked")
        List<String> labels = Arrays.stream(initialValue.getClass().getEnumConstants())
                .filter(val -> initialValue.getClass().isAssignableFrom(val.getClass()))
                .map(val -> (T) val)
                .map(xlate)
                .collect(Collectors.toList());

        setShouldSort(false);
        setElements(labels);
        setFixedOptions(true);
        selectElement(initialValue.ordinal());
        return this;
    }

    public final <T extends Enum<T>> WidgetComboBox initFromEnum(T initialValue) {
        if (initialValue instanceof ITranslatableEnum tr) {
            return initFromEnum(initialValue, e -> I18n.get(tr.getTranslationKey()));
        } else {
            throw new IllegalArgumentException(initialValue + " must implement ITranslatableEnum!");
        }
    }
}
