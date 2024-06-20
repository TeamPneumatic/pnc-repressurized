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

import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.gui.widget.PNCForgeSlider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorColors;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ArmorColoringScreen extends AbstractPneumaticCraftScreen {
    private boolean needSave = false;
    private final int[][] origColors = new int[4][SelectorType.values().length]; // primary & secondary for each slot
    private final int[][] colors = new int[4][SelectorType.values().length]; // primary & secondary for each slot
    private final List<SelectorButton> selectorButtons = new ArrayList<>();
    private final Map<ColorComponent,RGBSlider> rgbSliders = new EnumMap<>(ColorComponent.class);

    private WidgetButtonExtended saveButton;
    private WidgetLabel scrollLabel;

    // these are static so the selected button is remembered across GUI invocations
    private static EquipmentSlot selectedSlot = EquipmentSlot.HEAD;
    private static SelectorType selectorType = SelectorType.PRIMARY;

    public ArmorColoringScreen() {
        super(Component.literal("Colors"));

        Player player = ClientUtils.getClientPlayer();
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            int idx = slot.getIndex();
            if (stack.getItem() instanceof PneumaticArmorItem armor) {
                origColors[idx][0] = colors[idx][0] = armor.getPrimaryColor(stack);
                origColors[idx][1] = colors[idx][1] = armor.getSecondaryColor(stack);
                origColors[idx][2] = colors[idx][2] = armor.getEyepieceColor(stack);
            }
        }
    }

    @Override
    public void init() {
        super.init();

        xSize = width;
        ySize = height;

        selectorButtons.clear();
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            for (SelectorType type : SelectorType.values()) {
                selectorButtons.add(new SelectorButton(this, slot, type));
            }
        }
        selectorButtons.forEach(this::addRenderableWidget);
        for (SelectorType type : SelectorType.values()) {
            addRenderableWidget(new WidgetLabel(87 + type.xOffset, 30, Component.literal(type.label), 0xFFFFFFFF).setAlignment(WidgetLabel.Alignment.CENTRE));
        }

        rgbSliders.clear();
        for (ColorComponent color : ColorComponent.values()) {
            rgbSliders.put(color, new RGBSlider(this, color, 43, 165 + color.yOffset, 128, 10,
                    (getCurrentColor() >> color.bitShift) & 0xFF));
        }
        rgbSliders.values().forEach(this::addRenderableWidget);

        addRenderableWidget(scrollLabel = new WidgetLabel(107, 210, xlate("pneumaticcraft.armor.gui.misc.colors.scrollWheel"), 0xFFA0A0A0).setAlignment(WidgetLabel.Alignment.CENTRE));
        scrollLabel.visible = false;

        addRenderableWidget(new WidgetButtonExtended(85, 130, 40, 20, xlate("pneumaticcraft.armor.gui.misc.copy"),
                b -> copyColorsToOtherPieces()).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.copyTooltip"));

        saveButton = addRenderableWidget(new WidgetButtonExtended(43, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.save"),
                b -> saveChanges()));
        addRenderableWidget(new WidgetButtonExtended(88, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.reset"),
                b -> resetColors(Screen.hasShiftDown())).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.resetTooltip"));
        addRenderableWidget(new WidgetButtonExtended(133, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.cancel"),
                b -> onClose()));

        Component txt = xlate("pneumaticcraft.armor.gui.misc.colors.showEnchantGlint");
        addRenderableWidget(new WidgetCheckBox(width - font.width(txt) - 40, height - font.lineHeight - 10, 0xFFFFFFFF, txt,
                b -> ConfigHelper.setShowEnchantGlint(b.checked)).setChecked(ConfigHelper.client().armor.showEnchantGlint.get()));
    }

    private void setCurrentColor(int newCol) {
        colors[selectedSlot.getIndex()][selectorType.ordinal()] = newCol;
    }

    private int getCurrentColor() {
        return colors[selectedSlot.getIndex()][selectorType.ordinal()];
    }

    private void saveChanges() {
        ClientUtils.getClientPlayer().playSound(ModSounds.HUD_INIT_COMPLETE.get(), 1f, 1f);
        NetworkHandler.sendToServer(PacketUpdateArmorColors.forPlayer(ClientUtils.getClientPlayer()));
        needSave = false;
    }

    private void copyColorsToOtherPieces() {
        for (int i = 0; i < colors.length; i++) {
            if (i != selectedSlot.getIndex()) {
                colors[i][selectorType.ordinal()] = getCurrentColor();
            }
            updateClientSideArmor(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i));
        }
    }

    private void resetColors(boolean factorySettings) {
        for (int i = 0; i < colors.length; i++) {
            for (SelectorType type : SelectorType.values()) {
                colors[i][type.ordinal()] = factorySettings ? type.defaultColor : origColors[i][type.ordinal()];
            }
            updateClientSideArmor(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i));
        }
        updateSliders();
        needSave = factorySettings;
    }

    private void updateClientSideArmor(EquipmentSlot slot) {
        Player player = ClientUtils.getClientPlayer();
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.getItem() instanceof PneumaticArmorItem armor) {
            armor.setPrimaryColor(stack, colors[slot.getIndex()][SelectorType.PRIMARY.ordinal()]);
            armor.setSecondaryColor(stack, colors[slot.getIndex()][SelectorType.SECONDARY.ordinal()]);
            if (slot == EquipmentSlot.HEAD) {
                armor.setEyepieceColor(stack, colors[slot.getIndex()][SelectorType.EYEPIECE.ordinal()]);
            }
        }
        for (EquipmentSlot slot2: EquipmentSlot.values()) {
            HUDHandler.getInstance().updateOverlayColors(slot2);
        }
        selectorButtons.get((3 - slot.getIndex()) * SelectorType.values().length).setRenderStacks(stack);
        needSave = true;
    }

    private void updateSliders() {
        for (ColorComponent color : ColorComponent.values()) {
            updateSlider(rgbSliders.get(color), (getCurrentColor() & color.mask) >> color.bitShift);
        }
    }

    private void updateSlider(RGBSlider s, int val) {
        s.setValue(val);
    }

    @Override
    public void tick() {
        super.tick();

        selectorButtons.forEach(b -> b.active = !(selectorType == b.selectorType && selectedSlot == b.slot));
        saveButton.active = needSave;
        scrollLabel.visible = rgbSliders.values().stream().anyMatch(AbstractWidget::isHoveredOrFocused);
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        double scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
        int scale = (int) (Minecraft.getInstance().getWindow().getScreenHeight() / (scaleFactor * 3));
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics,
                width / 2, 0,
                width, height,
                scale, 0.0625F,
                mouseX, mouseY,
                ClientUtils.getClientPlayer());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        for (RGBSlider slider : rgbSliders.values()) {
            if (slider.isHoveredOrFocused()) {
                double val = Math.signum(deltaY);
                if (Screen.hasShiftDown()) val *= 10;
                int newVal = Mth.clamp((int) (slider.getValueInt() + val), 0, 255);
                if (slider.getValueInt() != newVal) {
                    updateSlider(slider, newVal);
                    slider.applyValue();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(minecraft).setScreen(ArmorMainScreen.getInstance());
    }

    @Override
    public void removed() {
        if (needSave) {
            resetColors(false);
        }

        super.removed();
    }

    private static class RGBSlider extends PNCForgeSlider {
        private final ArmorColoringScreen gui;
        private final ColorComponent color;

        public RGBSlider(ArmorColoringScreen gui, ColorComponent color, int xPos, int yPos, int width, int height, double currentVal) {
            super(xPos, yPos, width, height, Component.literal(color.prefix), Component.empty(), 0, 255, currentVal, true, null);
            this.gui = gui;
            this.color = color;
        }

        public ColorComponent getColor() {
            return color;
        }

        @Override
        protected void applyValue() {
            ColorComponent component = getColor();
            gui.setCurrentColor((gui.getCurrentColor() & ~component.mask) | (getValueInt() << component.bitShift));
            gui.updateClientSideArmor(selectedSlot);
            super.applyValue();
        }

//        @Override
//        protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
//            int xPos = this.getX() + (int)(this.value * (double)(this.width - 8));
//            int vOff = (this.isHoveredOrFocused() ? 2 : 1) * 20;
//            ScreenUtils.blitWithBorder(pPoseStack, WIDGETS_LOCATION, xPos, this.getY(), 0, 46 + vOff, 8, this.height, 200, 20, 2, 3, 2, 2, 0);
//        }
    }

    private enum ColorComponent {
        RED(16, ChatFormatting.RED + "R: ", 0),
        GREEN(8, ChatFormatting.GREEN + "G: ", 15),
        BLUE(0, ChatFormatting.BLUE + "B: ", 30);

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
        PRIMARY(0, "1", PneumaticArmorItem.DEFAULT_PRIMARY_COLOR),
        SECONDARY(22, "2", PneumaticArmorItem.DEFAULT_SECONDARY_COLOR),
        EYEPIECE(44, "E", PneumaticArmorItem.DEFAULT_EYEPIECE_COLOR);

        private final int xOffset;
        private final String label;
        private final int defaultColor;

        SelectorType(int xOffset, String label, DyedItemColor defaultColor) {
            this.xOffset = xOffset;
            this.label = label;
            this.defaultColor = defaultColor.rgb();
        }

        boolean showButton(EquipmentSlot slot) {
            return slot == EquipmentSlot.HEAD || this != EYEPIECE;
        }

        public int getDefaultColor() {
            return defaultColor;
        }
    }

    private static class SelectorButton extends WidgetButtonExtended {
        private final ArmorColoringScreen gui;
        private final EquipmentSlot slot;
        private final SelectorType selectorType;

        public SelectorButton(ArmorColoringScreen gui, EquipmentSlot slot, SelectorType selectorType) {
            super(78 + selectorType.xOffset, 41 + (3 - slot.getIndex()) * 22, 18, 18, "", b -> {
                ArmorColoringScreen.selectedSlot = slot;
                ArmorColoringScreen.selectorType = selectorType;
                gui.updateSliders();
            });
            this.gui = gui;
            this.slot = slot;
            this.selectorType = selectorType;
            visible = selectorType.showButton(slot) && PneumaticArmorItem.isPneumaticArmorPiece(ClientUtils.getClientPlayer(), slot);

            setHighlightWhenInactive(true);

            if (selectorType == SelectorType.PRIMARY) {
                setRenderStacks(ClientUtils.getClientPlayer().getItemBySlot(slot));
                setIconPosition(IconPosition.LEFT);
            }
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);

            int x = getX(), y = getY();
            graphics.fill(x + 3, y + 3, x + 14, y + 14, gui.colors[slot.getIndex()][selectorType.ordinal()]);
            graphics.hLine(x + 4, x + 14, y + 14, 0xFF202020);
            graphics.vLine(x + 14, y + 3, y + 14, 0xFF202020);
        }
    }
}
