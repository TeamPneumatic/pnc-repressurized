package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.inventory.EquipmentSlotType;

public class GuiJumpBoostOptions extends GuiSliderOptions {
    public GuiJumpBoostOptions(IUpgradeRenderHandler handler) {
        super(handler);
    }

    @Override
    protected String getTagName() {
        return ItemPneumaticArmor.NBT_JUMP_BOOST;
    }

    @Override
    protected EquipmentSlotType getSlot() {
        return EquipmentSlotType.LEGS;
    }

    @Override
    protected String getPrefix() {
        return "Boost: ";
    }

    @Override
    protected String getSuffix() {
        return "%";
    }
}
