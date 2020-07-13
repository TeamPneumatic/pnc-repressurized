package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WidgetComboBox extends WidgetTextField implements IDrawAfterRender {

    private final ArrayList<String> elements = new ArrayList<>();
    private final FontRenderer fontRenderer;
    private boolean enabled = true;
    private boolean fixedOptions;
    private boolean shouldSort = true;
    private int selectedIndex = -1;
    private final int baseHeight; // unexpanded height
    private final Consumer<WidgetComboBox> pressable;
    private List<String> applicable = null;

    public WidgetComboBox(FontRenderer fontRenderer, int x, int y, int width, int height) {
        this(fontRenderer, x, y, width, height, b -> {});
    }

    public WidgetComboBox(FontRenderer fontRenderer, int x, int y, int width, int height, Consumer<WidgetComboBox> pressable) {
        super(fontRenderer, x, y, width, height);
        this.fontRenderer = fontRenderer;
        this.baseHeight = height;
        this.pressable = pressable;
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
                .filter(element -> fixedOptions || element.toLowerCase().contains(getText().toLowerCase()))
                .collect(Collectors.toList());
//        }
        return applicable;
    }

    @Override
    public void writeText(String textToWrite) {
        super.writeText(textToWrite);

        applicable = null; // force recalc
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        super.renderButton(mouseX, mouseY, partialTick);

        fontRenderer.drawString(isFocused() ? GuiConstants.TRIANGLE_UP : GuiConstants.TRIANGLE_DOWN, x + width - 7, y + 1, 0xc0c0c0);
    }

    @Override
    public void renderAfterEverythingElse(int mouseX, int mouseY, float partialTick) {
        if (enabled && active && isFocused()) {
            List<String> applicableElements = getApplicableElements();
            fill(x - 1, y + height + 1, x + width + 1, y + height + 3 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFFA0A0A0);
            fill(x,     y + height + 1, x + width,     y + height + 2 + applicableElements.size() * fontRenderer.FONT_HEIGHT, 0xFF000000);
            for (int i = 0; i < applicableElements.size(); i++) {
                String element = applicableElements.get(i);
                fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(element, getWidth()), x + 4, y + height + 2 + i * fontRenderer.FONT_HEIGHT, 0xE0E0E0);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (getVisible() && active) {
            int h = baseHeight + (isFocused() ? getApplicableElements().size() * fontRenderer.FONT_HEIGHT : 0);
            boolean flag = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width)
                    && mouseY >= (double)this.y && mouseY < (double)(this.y + h);
            if (flag) {
                if (mouseY < y + height) {
                    // in the textfield itself
                    setFocused(!isFocused());
                } else {
                    // in the drop-down area
                    setFocused(false);
                    int i = ((int) mouseY - y - height) / fontRenderer.FONT_HEIGHT;
                    if (i < getApplicableElements().size()) {
                        setText(getApplicableElements().get(i));
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

        if (enabled && isFocused() && keyCode == GLFW.GLFW_KEY_TAB) { // Tab completion
            List<String> applicableElements = getApplicableElements();
            if (applicableElements.size() > 0) {
                setText(applicableElements.get(0));
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
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
    }

    public WidgetComboBox setFixedOptions() {
        fixedOptions = true;
        applicable = null; // force recalc
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

    /**
     * Convenience method: set up a combo box to display the values of an enum
     * @param initialValue the initial value to display
     * @param xlate a function which gets the string representation of a given value of the enum
     * @param <T> enum type
     * @return the combo box
     */
    public final <T extends Enum<T>> WidgetComboBox initFromEnum(T initialValue, Function<T, String> xlate) {
        @SuppressWarnings("unchecked")
        List<T> values = Arrays.stream(initialValue.getClass().getEnumConstants())
                .filter(val -> initialValue.getClass().isAssignableFrom(val.getClass()))
                .map(val -> (T) val)
                .collect(Collectors.toList());

        List<String> s = values.stream().map(xlate).collect(Collectors.toList());
        setShouldSort(false);
        setElements(s);
        setFixedOptions();
        selectElement(initialValue.ordinal());
        return this;
    }

    public final <T extends Enum<T>> WidgetComboBox initFromEnum(T initialValue) {
        if (initialValue instanceof ITranslatableEnum) {
            return initFromEnum(initialValue, e -> I18n.format(((ITranslatableEnum) e).getTranslationKey()));
        } else {
            throw new IllegalArgumentException(initialValue + " must implement ITranslatableEnum!");
        }
    }
}
