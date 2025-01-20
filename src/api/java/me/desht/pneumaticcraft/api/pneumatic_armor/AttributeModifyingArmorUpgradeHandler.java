package me.desht.pneumaticcraft.api.pneumatic_armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/**
 * Convenience class for an armor upgrade which can be used to toggle on and off an attribute modifier.
 *
 * @param <T> extension data type (use {@link IArmorExtensionData} if you don't have specific player-specific data)
 */
public abstract class AttributeModifyingArmorUpgradeHandler<T extends IArmorExtensionData> extends BaseArmorUpgradeHandler<T> {
    /**
     * {@return the attribute which this upgrade should modify}
     */
    protected abstract Holder<Attribute> getModifiedAttribute();

    /**
     * {@return the modified value when the upgrade is active}
     * @param handler the common armor handler object, for context
     */
    protected abstract double getModifiedAttributeValue(ICommonArmorHandler handler);

    /**
     * {@return the attribute modifier ID; by default the armor upgrade ID is used}
     */
    protected ResourceLocation getModifierId() {
        return getID();
    }

    /**
     * Check that this modifier should actually be active at this time. Note there is already an implicit check for the
     * upgrade being enabled, and for sufficient pressure in the armor piece; this method can be used to carry out
     * extra checks.
     *
     * @param handler the common armor handler, for context
     * @return true if the modifier should be applied, false otherwise
     */
    protected boolean isAttributeModifierApplicable(ICommonArmorHandler handler) {
        return true;
    }

    /**
     * {@return the modifier operation used for the modifier when it is applicable; ADD_VALUE by default}
     */
    protected AttributeModifier.Operation getModifierOperation() {
        return AttributeModifier.Operation.ADD_VALUE;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        AttributeInstance attributeInstance = player.getAttribute(getModifiedAttribute());
        if (attributeInstance != null) {
            AttributeModifier currentModifier = attributeInstance.getModifier(getModifierId());
            double newValue = enabled && commonArmorHandler.hasMinPressure(getEquipmentSlot()) && isAttributeModifierApplicable(commonArmorHandler) ?
                    getModifiedAttributeValue(commonArmorHandler) : 0f;
            if (currentModifier != null) {
                if (Math.abs(currentModifier.amount() - newValue) < Mth.EPSILON) {
                    return;  // already good
                }
                attributeInstance.removeModifier(currentModifier.id());
            }
            if (newValue > 0) {
                attributeInstance.addTransientModifier(new AttributeModifier(getModifierId(), newValue, getModifierOperation()));
            }
        }
    }

    @Override
    public void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
        if (!newState) {
            removeModifier(commonArmorHandler);
        }
    }

    @Override
    public void onShutdown(ICommonArmorHandler commonArmorHandler) {
        removeModifier(commonArmorHandler);
    }

    private void removeModifier(ICommonArmorHandler commonArmorHandler) {
        AttributeInstance attributeInstance = commonArmorHandler.getPlayer().getAttribute(getModifiedAttribute());
        if (attributeInstance != null) {
            AttributeModifier currentModifier = attributeInstance.getModifier(getModifierId());
            if (currentModifier != null) attributeInstance.removeModifier(currentModifier.id());
        }
    }
}
