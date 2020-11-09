package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.google.common.base.Strings;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorMainScreen;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.CoreComponentsOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoreComponentsClientHandler extends IArmorUpgradeClientHandler.AbstractHandler {
    private static final int MAX_BARS = 40;
    private static final String[] BAR_STR_CACHE = new String[MAX_BARS + 1];
    private static final ITextComponent NO_ARMOR = new StringTextComponent("-").mergeStyle(TextFormatting.DARK_GRAY);

    private final float[] lastPressure = new float[] { -1, -1, -1, -1 };
    private WidgetAnimatedStat powerStat;
    public WidgetAnimatedStat testMessageStat;
    public boolean showPressureNumerically;  // false for numeric readout, true for horizontal bars

    public CoreComponentsClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().coreComponentsHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        boolean needUpdate = false;
        for (int i = 0; i < 4; i++) {
            if (lastPressure[i] != armorHandler.getArmorPressure(ArmorUpgradeRegistry.ARMOR_SLOTS[i])) {
                lastPressure[i] = armorHandler.getArmorPressure(ArmorUpgradeRegistry.ARMOR_SLOTS[i]);
                needUpdate = true;
            }
        }
        if (needUpdate) {
            List<ITextComponent> l = Arrays.stream(ArmorUpgradeRegistry.ARMOR_SLOTS)
                    .map(slot -> getPressureStr(armorHandler, slot))
                    .collect(Collectors.toList());
            powerStat.setText(l);
        }
    }

    @Override
    public void initConfig() {
        showPressureNumerically = PNCConfig.Client.Armor.showPressureNumerically;
    }

    @Override
    public void saveToConfig() {
        ConfigHelper.setShowPressureNumerically(showPressureNumerically);
    }

    private ITextComponent getPressureStr(ICommonArmorHandler handler, EquipmentSlotType slot) {
        if (!ItemPneumaticArmor.isPneumaticArmorPiece(handler.getPlayer(), slot))
            return NO_ARMOR;
        float pressure = handler.getArmorPressure(slot);
        if (showPressureNumerically) {
            return new StringTextComponent(String.format("%4.1f", pressure)).mergeStyle(getColourForPressure(pressure));
        } else {
            return new StringTextComponent(getBarStr(pressure));
        }
    }

    private TextFormatting getColourForPressure(float pressure) {
        if (pressure < 0.5F) {
            return TextFormatting.RED;
        } else if (pressure < 2.0F) {
            return TextFormatting.GOLD;
        } else if (pressure < 4.0F) {
            return TextFormatting.YELLOW;
        } else {
            return TextFormatting.GREEN;
        }
    }

    private String getBarStr(float pressure) {
        int scaled = (int) (MAX_BARS * pressure / 10f);
        int idx = MathHelper.clamp(scaled, 0, MAX_BARS);
        if (BAR_STR_CACHE[idx] == null) {
            int n2 = MAX_BARS - scaled;
            BAR_STR_CACHE[idx] = getColourForPressure(pressure) + Strings.repeat("|", scaled)
                    + TextFormatting.DARK_GRAY + Strings.repeat("|", n2);
        }
        return BAR_STR_CACHE[idx];
    }

    @Override
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean helmetEnabled) {
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (powerStat == null) {
            powerStat = new WidgetAnimatedStat(null, StringTextComponent.EMPTY, WidgetAnimatedStat.StatIcon.NONE,0x3000AA00, null, ArmorHUDLayout.INSTANCE.powerStat);
            powerStat.setLineSpacing(15);
            powerStat.setSubwidgetRenderOffsets(-18, 0);  // ensure armor icons are rendered in the right place
            for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                WidgetButtonExtended pressureButton = new WidgetButtonExtended(0, 5 + (3 - slot.getIndex()) * 15, 18, 18, StringTextComponent.EMPTY) ;
                ItemStack stack = GuiArmorMainScreen.ARMOR_STACKS[slot.getIndex()];
                pressureButton.setVisible(false);
                pressureButton.setRenderStacks(stack);
                powerStat.addSubWidget(pressureButton);
            }
            powerStat.setMinimumContractedDimensions(0, 0);
            powerStat.openStat();
        }
        return powerStat;
    }

    @Override
    public void reset() {
        powerStat = null;
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new CoreComponentsOptions(screen,this);
    }

    @Override
    public void onResolutionChanged() {
        powerStat = null;
        Arrays.fill(lastPressure, -1);
    }
}
