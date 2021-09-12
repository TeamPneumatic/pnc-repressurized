package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class NightVisionHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    @Override
    public ResourceLocation getID() {
        return RL("night_vision");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.NIGHT_VISION };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return PneumaticValues.PNEUMATIC_NIGHT_VISION_USAGE;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        boolean hasPressure = commonArmorHandler.hasMinPressure(EquipmentSlotType.HEAD);
        if (!player.level.isClientSide) {
            EffectInstance nvInstance = player.getEffect(Effects.NIGHT_VISION);
            if (enabled && hasPressure && (nvInstance == null || nvInstance.getDuration() <= 220)) {
                player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 500, 0, false, false));
            } else if ((!enabled || !hasPressure) && nvInstance != null) {
                player.removeEffect(Effects.NIGHT_VISION);
            }
        }
    }

    @Override
    public void onShutdown(ICommonArmorHandler commonArmorHandler) {
        commonArmorHandler.getPlayer().removeEffect(Effects.NIGHT_VISION);
    }
}
