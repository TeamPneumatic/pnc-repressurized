package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

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
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoreComponentsClientHandler extends IArmorUpgradeClientHandler.AbstractHandler {
    private WidgetAnimatedStat powerStat;
    public WidgetAnimatedStat testMessageStat;

    public CoreComponentsClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().coreComponentsHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        List<String> l = Arrays.stream(ArmorUpgradeRegistry.ARMOR_SLOTS)
                .map(slot -> getPressureStr(armorHandler, slot))
                .collect(Collectors.toList());
        powerStat.setText(l);
    }

    private String getPressureStr(ICommonArmorHandler handler, EquipmentSlotType slot) {
        if (!ItemPneumaticArmor.isPneumaticArmorPiece(handler.getPlayer(), slot))
            return "-";
        float pressure = handler.getArmorPressure(slot);
        TextFormatting colour;
        if (pressure < 0.5F) {
            colour = TextFormatting.RED;
        } else if (pressure < 2.0F) {
            colour = TextFormatting.GOLD;
        } else if (pressure < 4.0F) {
            colour = TextFormatting.YELLOW;
        } else {
            colour = TextFormatting.GREEN;
        }
        return colour.toString() + String.format("%5.2f", pressure);
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
            powerStat.setWidgetOffsets(-18, 0);  // ensure armor icons are rendered in the right place
            for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                WidgetButtonExtended pressureButton = new WidgetButtonExtended(0, 5 + (3 - slot.getIndex()) * 15, 18, 18, "") ;
                ItemStack stack = GuiArmorMainScreen.ARMOR_STACKS[slot.getIndex()];
                pressureButton.setVisible(false);
                pressureButton.setRenderStacks(stack);
                powerStat.addSubWidget(pressureButton);
            }
            powerStat.setMinDimensionsAndReset(0, 0);
            powerStat.openWindow();
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
    }
}
