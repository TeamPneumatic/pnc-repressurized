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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorColors;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiArmorColors extends GuiPneumaticScreenBase implements Slider.ISlider {
    private boolean needSave = false;
    private final int[][] origColors = new int[4][SelectorType.values().length]; // primary & secondary for each slot
    private final int[][] colors = new int[4][SelectorType.values().length]; // primary & secondary for each slot
    private final List<SelectorButton> selectorButtons = new ArrayList<>();
    private final Map<ColorComponent,RGBSlider> rgbSliders = new EnumMap<>(ColorComponent.class);

    private WidgetButtonExtended saveButton;
    private WidgetLabel scrollLabel;

    // these are static so the selected button is remembered across GUI invocations
    private static EquipmentSlotType selectedSlot = EquipmentSlotType.HEAD;
    private static SelectorType selectorType = SelectorType.PRIMARY;

    public GuiArmorColors() {
        super(new StringTextComponent("Colors"));

        PlayerEntity player = Minecraft.getInstance().player;
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            int idx = slot.getIndex();
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                origColors[idx][0] = colors[idx][0] = ((ItemPneumaticArmor) stack.getItem()).getColor(stack);
                origColors[idx][1] = colors[idx][1] = ((ItemPneumaticArmor) stack.getItem()).getSecondaryColor(stack);
                origColors[idx][2] = colors[idx][2] = ((ItemPneumaticArmor) stack.getItem()).getEyepieceColor(stack);
            }
        }
    }

    @Override
    public void init() {
        super.init();

        xSize = width;
        ySize = height;

        selectorButtons.clear();
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            for (SelectorType type : SelectorType.values()) {
                selectorButtons.add(new SelectorButton(this, slot, type));
            }
        }
        selectorButtons.forEach(this::addButton);
        for (SelectorType type : SelectorType.values()) {
            addButton(new WidgetLabel(87 + type.xOffset, 30, new StringTextComponent(type.label), 0xFFFFFFFF).setAlignment(WidgetLabel.Alignment.CENTRE));
        }

        rgbSliders.clear();
        for (ColorComponent color : ColorComponent.values()) {
            rgbSliders.put(color, new RGBSlider(this, color, 43, 165 + color.yOffset, 128, 10,
                    (getCurrentColor() >> color.bitShift) & 0xFF));
        }
        rgbSliders.values().forEach(this::addButton);

        addButton(scrollLabel = new WidgetLabel(107, 210, xlate("pneumaticcraft.armor.gui.misc.colors.scrollWheel"), 0xFFA0A0A0).setAlignment(WidgetLabel.Alignment.CENTRE));
        scrollLabel.visible = false;

        addButton(new WidgetButtonExtended(85, 130, 40, 20, xlate("pneumaticcraft.armor.gui.misc.copy"),
                b -> copyColorsToOtherPieces()).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.copyTooltip"));

        saveButton = addButton(new WidgetButtonExtended(43, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.save"),
                b -> saveChanges()));
        addButton(new WidgetButtonExtended(88, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.reset"),
                b -> resetColors(Screen.hasShiftDown())).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.resetTooltip"));
        addButton(new WidgetButtonExtended(133, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.cancel"),
                b -> onClose()));

        ITextComponent txt = xlate("pneumaticcraft.armor.gui.misc.colors.showEnchantGlint");
        addButton(new WidgetCheckBox(width - font.width(txt) - 40, height - font.lineHeight - 10, 0xFFFFFFFF, txt,
                b -> ConfigHelper.setShowEnchantGlint(b.checked)).setChecked(PNCConfig.Client.Armor.showEnchantGlint));
    }

    private void setCurrentColor(int newCol) {
        colors[selectedSlot.getIndex()][selectorType.ordinal()] = newCol;
    }

    private int getCurrentColor() {
        return colors[selectedSlot.getIndex()][selectorType.ordinal()];
    }

    private void saveChanges() {
        Minecraft.getInstance().player.playSound(ModSounds.HUD_INIT_COMPLETE.get(), 1f, 1f);
        NetworkHandler.sendToServer(new PacketUpdateArmorColors());
        needSave = false;
    }

    private void copyColorsToOtherPieces() {
        for (int i = 0; i < colors.length; i++) {
            if (i != selectedSlot.getIndex()) {
                colors[i][selectorType.ordinal()] = getCurrentColor();
            }
            updateClientSideArmor(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, i));
        }
    }

    private void resetColors(boolean factorySettings) {
        for (int i = 0; i < colors.length; i++) {
            for (SelectorType type : SelectorType.values()) {
                colors[i][type.ordinal()] = factorySettings ? type.defaultColor : origColors[i][type.ordinal()];
            }
            updateClientSideArmor(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, i));
        }
        updateSliders();
        needSave = factorySettings;
    }

    private void updateClientSideArmor(EquipmentSlotType slot) {
        PlayerEntity player = Minecraft.getInstance().player;
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.getItem() instanceof ItemPneumaticArmor) {
            ((ItemPneumaticArmor) stack.getItem()).setColor(stack, colors[slot.getIndex()][SelectorType.PRIMARY.ordinal()]);
            ((ItemPneumaticArmor) stack.getItem()).setSecondaryColor(stack, colors[slot.getIndex()][SelectorType.SECONDARY.ordinal()]);
            if (slot == EquipmentSlotType.HEAD) {
                ((ItemPneumaticArmor) stack.getItem()).setEyepieceColor(stack, colors[slot.getIndex()][SelectorType.EYEPIECE.ordinal()]);
            }
        }
        for (EquipmentSlotType slot2: EquipmentSlotType.values()) HUDHandler.getInstance().updateOverlayColors(slot2);
        selectorButtons.get((3 - slot.getIndex()) * SelectorType.values().length).setRenderStacks(stack);
        needSave = true;
    }

    private void updateSliders() {
        for (ColorComponent color : ColorComponent.values()) {
            updateSlider(rgbSliders.get(color), (getCurrentColor() & color.mask) >> color.bitShift);
        }
    }

    private void updateSlider(Slider s, int val) {
        s.setValue(val);
        s.setMessage(s.dispString.copy().append(Integer.toString(val)).append(s.suffix));
    }

    @Override
    public void tick() {
        super.tick();

        selectorButtons.forEach(b -> b.active = !(selectorType == b.selectorType && selectedSlot == b.slot));
        saveButton.active = needSave;
        scrollLabel.visible = rgbSliders.values().stream().anyMatch(Widget::isHovered);
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, x, y, partialTicks);
        double scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
        int scale = (int) (Minecraft.getInstance().getWindow().getScreenHeight() / (scaleFactor * 3));
        InventoryScreen.renderEntityInInventory(width * 2 / 3, height * 3 / 4, scale, width * 2 / 3f - x, height / 4f - y,
                Minecraft.getInstance().player);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (RGBSlider slider : rgbSliders.values()) {
            if (slider.isHovered()) {
                double val = Math.signum(delta);
                if (Screen.hasShiftDown()) val *= 10;
                int newVal = MathHelper.clamp((int) (slider.getValueInt() + val), 0, 255);
                if (slider.getValueInt() != newVal) {
                    updateSlider(slider, newVal);
                    onChangeSliderValue(slider);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(GuiArmorMainScreen.getInstance());
    }

    @Override
    public void removed() {
        if (needSave) {
            resetColors(false);
        }

        super.removed();
    }

    @Override
    public void onChangeSliderValue(Slider slider) {
        if (slider instanceof RGBSlider) {
            ColorComponent component = ((RGBSlider) slider).getColor();
            setCurrentColor((getCurrentColor() & ~component.mask) | (slider.getValueInt() << component.bitShift));
            updateClientSideArmor(selectedSlot);
        }
    }

    private static class RGBSlider extends Slider {
        private final ColorComponent color;

        public RGBSlider(GuiArmorColors gui, ColorComponent color, int xPos, int yPos, int width, int height, double currentVal) {
            super(xPos, yPos, width, height, new StringTextComponent(color.prefix), StringTextComponent.EMPTY, 0, 255, currentVal, false, true, null, gui);
            this.color = color;
        }

        public ColorComponent getColor() {
            return color;
        }
    }

    private enum ColorComponent {
        RED(16, TextFormatting.RED + "R: ", 0),
        GREEN(8, TextFormatting.GREEN + "G: ", 15),
        BLUE(0, TextFormatting.BLUE + "B: ", 30);

        private final int mask;
        private final int bitShift;
        private final String prefix;
        private final int yOffset;

        ColorComponent(int bitShift, String prefix, int yOffset) {
            this.mask = 0xFF << bitShift;
            this.bitShift = bitShift;
            this.prefix = prefix;
            this.yOffset = yOffset;
        }
    }

    public enum SelectorType {
        PRIMARY(0, "1", ItemPneumaticArmor.DEFAULT_PRIMARY_COLOR),
        SECONDARY(22, "2", ItemPneumaticArmor.DEFAULT_SECONDARY_COLOR),
        EYEPIECE(44, "E", ItemPneumaticArmor.DEFAULT_EYEPIECE_COLOR);

        private final int xOffset;
        private final String label;
        private final int defaultColor;

        SelectorType(int xOffset, String label, int defaultColor) {
            this.xOffset = xOffset;
            this.label = label;
            this.defaultColor = defaultColor;
        }

        boolean showButton(EquipmentSlotType slot) {
            return slot == EquipmentSlotType.HEAD || this != EYEPIECE;
        }

        public int getDefaultColor() {
            return defaultColor;
        }
    }

    private static class SelectorButton extends WidgetButtonExtended {
        private final GuiArmorColors gui;
        private final EquipmentSlotType slot;
        private final SelectorType selectorType;

        public SelectorButton(GuiArmorColors gui, EquipmentSlotType slot, SelectorType selectorType) {
            super(78 + selectorType.xOffset, 41 + (3 - slot.getIndex()) * 22, 18, 18, "", b -> {
                GuiArmorColors.selectedSlot = slot;
                GuiArmorColors.selectorType = selectorType;
                gui.updateSliders();
            });
            this.gui = gui;
            this.slot = slot;
            this.selectorType = selectorType;
            visible = selectorType.showButton(slot) && ItemPneumaticArmor.isPneumaticArmorPiece(ClientUtils.getClientPlayer(), slot);

            if (selectorType == SelectorType.PRIMARY) {
                setRenderStacks(Minecraft.getInstance().player.getItemBySlot(slot));
                setIconPosition(IconPosition.LEFT);
            }
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.renderButton(matrixStack, mouseX, mouseY, partialTicks);

            fill(matrixStack, x + 3, y + 3, x + 14, y + 14, gui.colors[slot.getIndex()][selectorType.ordinal()]);
            hLine(matrixStack, x + 4, x + 14, y + 14, 0xFF202020);
            vLine(matrixStack, x + 14, y + 3, y + 14, 0xFF202020);
        }
    }
}
