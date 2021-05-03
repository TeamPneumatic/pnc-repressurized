package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
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
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiArmorColors extends GuiPneumaticScreenBase implements Slider.ISlider {
    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    private boolean needSave = false;
    private final int[][] origColors = new int[4][SelectorType.values().length]; // primary & secondary for each slot
    private final int[][] colors = new int[4][SelectorType.values().length]; // primary & secondary for each slot
    private final boolean[] isPneumatic = new boolean[4];
    private final List<SelectorButton> selectorButtons = new ArrayList<>();
    private final List<RGBSlider> rgbSliders = new ArrayList<>();
    private WidgetButtonExtended saveButton;

    // these are static so the selected button is remembered across GUI invocations
    private static EquipmentSlotType selectedSlot = EquipmentSlotType.HEAD;
    private static SelectorType selectorType = SelectorType.PRIMARY;
    private WidgetLabel scrollLabel;

    public GuiArmorColors() {
        super(new StringTextComponent("Colors"));

        PlayerEntity player = Minecraft.getInstance().player;
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = player.getItemStackFromSlot(slot);
            int idx = slot.getIndex();
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                isPneumatic[idx] = true;
                origColors[idx][0] = colors[idx][0] = ((ItemPneumaticArmor) stack.getItem()).getColor(stack);
                origColors[idx][1] = colors[idx][1] = ((ItemPneumaticArmor) stack.getItem()).getSecondaryColor(stack);
                origColors[idx][2] = colors[idx][2] = ((ItemPneumaticArmor) stack.getItem()).getEyepieceColor(stack);
            } else {
                isPneumatic[idx] = false;
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
        rgbSliders.add(new RGBSlider(this, RED, 43, 165, 128, 10,
                new StringTextComponent(TextFormatting.RED + "R: "), StringTextComponent.EMPTY,
                0, 255, (getCurrentColor() >> 16) & 0xFF, false, true));
        rgbSliders.add(new RGBSlider(this, GREEN, 43, 180, 128, 10,
                new StringTextComponent(TextFormatting.GREEN + "G: "), StringTextComponent.EMPTY,
                0, 255, (getCurrentColor() >> 8) & 0xFF, false, true));
        rgbSliders.add(new RGBSlider(this, BLUE, 43, 195, 128, 10,
                new StringTextComponent(TextFormatting.BLUE + "B: "), StringTextComponent.EMPTY,
                0, 255, getCurrentColor() & 0xFF, false, true));
        rgbSliders.forEach(this::addButton);
        addButton(scrollLabel = new WidgetLabel(107, 210, xlate("pneumaticcraft.armor.gui.misc.colors.scrollWheel"), 0xFFA0A0A0).setAlignment(WidgetLabel.Alignment.CENTRE));
        scrollLabel.visible = false;

        addButton(new WidgetButtonExtended(85, 130, 40, 20, xlate("pneumaticcraft.armor.gui.misc.copy"),
                b -> copyColorsToOtherPieces()).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.copyTooltip"));

        saveButton = addButton(new WidgetButtonExtended(43, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.save"),
                b -> saveChanges()));
        addButton(new WidgetButtonExtended(88, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.reset"),
                b -> resetColors(Screen.hasShiftDown())).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.resetTooltip"));
        addButton(new WidgetButtonExtended(133, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.cancel"),
                b -> closeScreen()));

        ITextComponent txt = xlate("pneumaticcraft.armor.gui.misc.colors.showEnchantGlint");
        addButton(new WidgetCheckBox(width - font.getStringPropertyWidth(txt) - 40, height - font.FONT_HEIGHT - 10, 0xFFFFFFFF, txt,
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
            updateClientSideArmor(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, i));
        }
    }

    private void resetColors(boolean factorySettings) {
        for (int i = 0; i < colors.length; i++) {
            for (SelectorType type : SelectorType.values()) {
                colors[i][type.ordinal()] = factorySettings ? type.defaultColor : origColors[i][type.ordinal()];
            }
            updateClientSideArmor(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, i));
        }
        updateSliders();
        needSave = factorySettings;
    }

    private void updateRed(int val) {
        setCurrentColor((getCurrentColor() & 0xFF00FFFF) | val << 16);
        updateClientSideArmor(selectedSlot);
    }

    private void updateGreen(int val) {
        setCurrentColor((getCurrentColor() & 0xFFFF00FF) | val << 8);
        updateClientSideArmor(selectedSlot);
    }

    private void updateBlue(int val) {
        setCurrentColor((getCurrentColor() & 0xFFFFFF00) | val);
        updateClientSideArmor(selectedSlot);
    }

    private void updateClientSideArmor(EquipmentSlotType slot) {
        PlayerEntity player = Minecraft.getInstance().player;
        ItemStack stack = player.getItemStackFromSlot(slot);
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
        updateSlider(rgbSliders.get(RED), (getCurrentColor() & 0x00FF0000) >> 16);
        updateSlider(rgbSliders.get(GREEN), (getCurrentColor() & 0x0000FF00) >> 8);
        updateSlider(rgbSliders.get(BLUE), getCurrentColor() & 0x000000FF);
    }

    private void updateSlider(Slider s, int val) {
        s.setValue(val);
        s.setMessage(s.dispString.deepCopy().appendString(Integer.toString(val)).append(s.suffix));
    }

    @Override
    public void tick() {
        super.tick();

        selectorButtons.forEach(b -> b.active = !(selectorType == b.selectorType && selectedSlot == b.slot));
        saveButton.active = needSave;
        scrollLabel.visible = rgbSliders.stream().anyMatch(Widget::isHovered);
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, x, y, partialTicks);
        double scaleFactor = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        int scale = (int) (Minecraft.getInstance().getMainWindow().getHeight() / (scaleFactor * 3));
        InventoryScreen.drawEntityOnScreen(width * 2 / 3, height * 3 / 4, scale, width * 2 / 3f - x, height / 4f - y,
                Minecraft.getInstance().player);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (RGBSlider slider : rgbSliders) {
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
    public void closeScreen() {
        minecraft.displayGuiScreen(GuiArmorMainScreen.getInstance());
    }

    @Override
    public void onClose() {
        if (needSave) {
            resetColors(false);
        }

        super.onClose();
    }

    @Override
    public void onChangeSliderValue(Slider slider) {
        if (slider instanceof RGBSlider) {
            switch (((RGBSlider) slider).getColIdx()) {
                case RED: updateRed(slider.getValueInt()); break;
                case GREEN: updateGreen(slider.getValueInt()); break;
                case BLUE: updateBlue(slider.getValueInt()); break;
            }
        }
    }

    private static class RGBSlider extends Slider {
        private final int colIdx;

        public RGBSlider(GuiArmorColors gui, int colIdx, int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr) {
            super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, null, gui);
            this.colIdx = colIdx;
        }

        public int getColIdx() {
            return colIdx;
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
            visible = selectorType.showButton(slot);

            if (selectorType == SelectorType.PRIMARY) {
                setRenderStacks(Minecraft.getInstance().player.getItemStackFromSlot(slot));
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
