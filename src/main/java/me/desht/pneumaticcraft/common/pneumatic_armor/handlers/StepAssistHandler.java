package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class StepAssistHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    @Override
    public ResourceLocation getID() {
        return RL("step_assist");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[0];  // no upgrades needed, boots built-in
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.FEET;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        if (commonArmorHandler.hasMinPressure(EquipmentSlotType.FEET) && enabled) {
            player.stepHeight = player.isSneaking() ? 0.6001F : 1.25F;
        } /*else {
            player.stepHeight = 0.6F;
        }*/
    }

    @Override
    public void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
//        if (!newState) commonArmorHandler.getPlayer().stepHeight = 0.6F;
    }

    @Override
    public void onShutdown(ICommonArmorHandler commonArmorHandler) {
        commonArmorHandler.getPlayer().stepHeight = 0.6F;
    }
}
