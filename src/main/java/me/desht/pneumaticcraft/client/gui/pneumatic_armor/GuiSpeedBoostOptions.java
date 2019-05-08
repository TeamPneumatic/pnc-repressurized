package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.inventory.EntityEquipmentSlot;

public class GuiSpeedBoostOptions extends GuiSliderOptions {
    private int pendingVal = -1;
    private GuiSlider slider;

    public GuiSpeedBoostOptions(IUpgradeRenderHandler handler) {
        super(handler);
    }

    @Override
    protected String getTagName() {
        return "speedBoost";
    }

    @Override
    protected EntityEquipmentSlot getSlot() {
        return EntityEquipmentSlot.LEGS;
    }

    @Override
    protected GuiSlider.FormatHelper getFormatHelper() {
        return (id, name, value) -> "Boost: " + (int) value + "%";
    }
}
