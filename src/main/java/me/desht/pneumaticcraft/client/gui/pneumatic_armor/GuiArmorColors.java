package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorColors;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiArmorColors extends GuiPneumaticScreenBase implements Slider.ISlider {
    private boolean needSave = false;
    private final int[][] origColors = new int[4][2]; // primary & secondary for each slot
    private final int[][] colors = new int[4][2]; // primary & secondary for each slot
    private final boolean[] isPneumatic = new boolean[4];
    private final List<SelectorButton> selectorButtons = new ArrayList<>();
    private final List<RGBSlider> rgbSliders = new ArrayList<>();
    private EquipmentSlotType selectedSlot = EquipmentSlotType.HEAD;
    private boolean selectedPrimary = true;
    private WidgetButtonExtended saveButton;

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
            selectorButtons.add(new SelectorButton(this, slot, true));
            selectorButtons.add(new SelectorButton(this, slot, false));
        }
        selectorButtons.forEach(this::addButton);

        rgbSliders.clear();
        rgbSliders.add(new RGBSlider(this, 0, 43, 165, 128, 10,
                new StringTextComponent(TextFormatting.RED + "R: "), StringTextComponent.EMPTY,
                0, 255, (getCurrentColor() >> 16) & 0xFF, false, true));
        rgbSliders.add(new RGBSlider(this, 1, 43, 180, 128, 10,
                new StringTextComponent(TextFormatting.GREEN + "G: "), StringTextComponent.EMPTY,
                0, 255, (getCurrentColor() >> 8) & 0xFF, false, true));
        rgbSliders.add(new RGBSlider(this, 2, 43, 195, 128, 10,
                new StringTextComponent(TextFormatting.BLUE + "B: "), StringTextComponent.EMPTY,
                0, 255, getCurrentColor() & 0xFF, false, true));
        rgbSliders.forEach(this::addButton);

        saveButton = addButton(new WidgetButtonExtended(63, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.save"),
                b -> saveChanges()));

        addButton(new WidgetButtonExtended(108, height - 30, 40, 20, xlate("pneumaticcraft.armor.gui.misc.reset"),
                b -> resetColors(Screen.hasShiftDown())).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.resetTooltip"));

        addButton(new WidgetButtonExtended(85, 125, 40, 20, xlate("pneumaticcraft.armor.gui.misc.copy"),
                b -> copyColorsToOtherPieces()).setTooltipKey("pneumaticcraft.armor.gui.misc.colors.copyTooltip"));

        ITextComponent txt = xlate("pneumaticcraft.armor.gui.misc.colors.showEnchantGlint");
        addButton(new WidgetCheckBox(width - font.getStringPropertyWidth(txt) - 40, height - font.FONT_HEIGHT - 10, 0xFFFFFFFF, txt,
                b -> ConfigHelper.setShowEnchantGlint(b.checked)).setChecked(PNCConfig.Client.Armor.showEnchantGlint));
    }

    private void setCurrentColor(int newCol) {
        colors[selectedSlot.getIndex()][selectedPrimary ? 0 : 1] = newCol;
    }

    private int getCurrentColor() {
        return colors[selectedSlot.getIndex()][selectedPrimary ? 0 : 1];
    }

    private void saveChanges() {
        Minecraft.getInstance().player.playSound(ModSounds.HUD_INIT_COMPLETE.get(), 1f, 1f);
        NetworkHandler.sendToServer(new PacketUpdateArmorColors());
        needSave = false;
    }

    private void copyColorsToOtherPieces() {
        for (int i = 0; i < colors.length; i++) {
            if (i != selectedSlot.getIndex()) {
                colors[i][selectedPrimary ? 0 : 1] = getCurrentColor();
            }
            updateClientSideArmor(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, i));
        }
    }

    private void resetColors(boolean factorySettings) {
        for (int i = 0; i < colors.length; i++) {
            colors[i][0] = factorySettings ? 0xFF969696 : origColors[i][0];
            colors[i][1] = factorySettings ? 0xFFC0C0C0 : origColors[i][1];
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
            ((ItemPneumaticArmor) stack.getItem()).setColor(stack, colors[slot.getIndex()][0]);
            ((ItemPneumaticArmor) stack.getItem()).setSecondaryColor(stack, colors[slot.getIndex()][1]);
        }
        selectorButtons.get((3 - slot.getIndex()) * 2).setRenderStacks(stack);
        needSave = true;
    }

    private void updateSliders() {
        updateSlider(rgbSliders.get(0), (getCurrentColor() & 0x00FF0000) >> 16);
        updateSlider(rgbSliders.get(1), (getCurrentColor() & 0x0000FF00) >> 8);
        updateSlider(rgbSliders.get(2), getCurrentColor() & 0x000000FF);
    }

    private void updateSlider(Slider s, int val) {
        s.setValue(val);
        s.setMessage(s.dispString.deepCopy().appendString(Integer.toString(val)).append(s.suffix));
    }

    @Override
    public void tick() {
        super.tick();

        selectorButtons.forEach(b -> b.active = !(selectedPrimary == b.primary && selectedSlot == b.slot));
        saveButton.active = needSave;
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
                case 0: updateRed(slider.getValueInt()); break;
                case 1: updateGreen(slider.getValueInt()); break;
                case 2: updateBlue(slider.getValueInt()); break;
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

    private static class SelectorButton extends WidgetButtonExtended {
        private final GuiArmorColors gui;
        private final EquipmentSlotType slot;
        private final boolean primary;

        public SelectorButton(GuiArmorColors gui, EquipmentSlotType slot, boolean primary) {
            super(68 + (primary ? 0 : 40), 41 + (3 - slot.getIndex()) * 20, 35, 18, primary ? "1   " : "2   ", b -> {
                gui.selectedSlot = slot;
                gui.selectedPrimary = primary;
                gui.updateSliders();
            });
            this.gui = gui;
            this.slot = slot;
            this.primary = primary;
            visible = gui.isPneumatic[slot.getIndex()];

            if (primary) setRenderStacks(Minecraft.getInstance().player.getItemStackFromSlot(slot));
            setIconPosition(IconPosition.LEFT);
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int x, int y, float partialTicks) {
            super.renderButton(matrixStack, x, y, partialTicks);

            AbstractGui.fill(matrixStack, this.x + 22, this.y + 4, this.x + 32, this.y + 14, gui.colors[slot.getIndex()][primary ? 0 : 1]);
        }
    }
}
