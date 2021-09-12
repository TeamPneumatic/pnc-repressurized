package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ReachDistanceHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final UUID REACH_DIST_BOOST_ID = UUID.fromString("c9dce729-70c4-4c0f-95d4-31d2e50bc826");
    public static final AttributeModifier REACH_DIST_BOOST = new AttributeModifier(REACH_DIST_BOOST_ID, "Pneumatic Reach Boost", 3.5D, AttributeModifier.Operation.ADDITION);

    private final ResourceLocation ID = RL("reach_distance");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.RANGE };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 5;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.CHEST;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        PlayerEntity player = commonArmorHandler.getPlayer();
        if ((player.level.getGameTime() & 0xf) == 0) {
            ModifiableAttributeInstance attr = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
            if (attr != null) {
                attr.removeModifier(REACH_DIST_BOOST);
                if (enabled && commonArmorHandler.hasMinPressure(EquipmentSlotType.CHEST) && commonArmorHandler.isArmorEnabled()) {
                    attr.addTransientModifier(REACH_DIST_BOOST);
                }
            }
        }
    }

    @Override
    public void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
        if (!newState) {
            ModifiableAttributeInstance attr = commonArmorHandler.getPlayer().getAttribute(ForgeMod.REACH_DISTANCE.get());
            if (attr != null) {
                attr.removeModifier(ReachDistanceHandler.REACH_DIST_BOOST);
            }
        }
    }
}
