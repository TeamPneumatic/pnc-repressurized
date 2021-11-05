package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CoordTrackerHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    @Override
    public ResourceLocation getID() {
        return RL("coordinate_tracker");
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.COORDINATE_TRACKER };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return PneumaticValues.USAGE_COORD_TRACKER;
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    /**
     * Used by the Pneumatic Helmet coordinate tracker to control path update frequency.
     */
    public enum PathUpdateSetting implements ITranslatableEnum {
        SLOW(100),
        NORMAL(20),
        FAST(1);

        private final int ticks;

        PathUpdateSetting(int ticks) {
            this.ticks = ticks;
        }

        public int getTicks() {
            return ticks;
        }

        public PathUpdateSetting cycle() {
            return values()[(ordinal() + 1) % values().length];
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.armor.gui.coordinateTracker.pathUpdate." + toString().toLowerCase(Locale.ROOT);
        }
    }
}
