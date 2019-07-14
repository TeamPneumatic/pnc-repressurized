package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiHelmetMainOptions;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiHelmetMainScreen;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainHelmetHandler implements IUpgradeRenderHandler {
    private GuiAnimatedStat powerStat;
    public GuiAnimatedStat testMessageStat;

    @Override
    public String getUpgradeName() {
        return "coreComponents";
    }

    @Override
    public void update(PlayerEntity player, int rangeUpgrades) {
        List<String> l = Arrays.stream(UpgradeRenderHandlerList.ARMOR_SLOTS)
                .map(slot -> getPressureStr(player, slot))
                .collect(Collectors.toList());
        powerStat.setText(l);
    }

    private String getPressureStr(PlayerEntity player, EquipmentSlotType slot) {
        if (!ItemPneumaticArmor.isPneumaticArmorPiece(player, slot))
            return "-";
        float pressure = CommonArmorHandler.getHandlerForPlayer(player).armorPressure[slot.getIndex()];
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
    @OnlyIn(Dist.CLIENT)
    public void render3D(float partialTicks) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render2D(float partialTicks, boolean helmetEnabled) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IGuiAnimatedStat getAnimatedStat() {
        if (powerStat == null) {
            powerStat = new GuiAnimatedStat(null, "", GuiAnimatedStat.StatIcon.NONE,0x3000AA00, null, ArmorHUDLayout.INSTANCE.powerStat);
            powerStat.setLineSpacing(15);
            powerStat.setWidgetOffsets(-18, 0);  // ensure armor icons are rendered in the right place
            for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                GuiButtonSpecial pressureButton = new GuiButtonSpecial(0, 5 + (3 - slot.getIndex()) * 15, 18, 18, "") ;
                ItemStack stack = GuiHelmetMainScreen.ARMOR_STACKS[slot.getIndex()];
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
    public Item[] getRequiredUpgrades() {
        return new Item[]{};
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void reset() {
        powerStat = null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiHelmetMainOptions(this);
    }

    @Override
    public float getMinimumPressure() {
        // pressure display always shows, even when empty
        return -1.0f;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    public void onResolutionChanged() {
        powerStat = null;
    }
}
