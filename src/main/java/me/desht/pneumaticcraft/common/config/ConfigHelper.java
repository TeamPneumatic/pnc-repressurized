package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoordTrackerHandler.PathUpdateSetting;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;

public class ConfigHelper {
    public static ClientConfig client() {
        return ConfigHolder.client;
    }

    public static CommonConfig common() {
        return ConfigHolder.common;
    }

    public static void setProgrammerDifficulty(WidgetDifficulty difficulty) {
        ConfigHolder.client.general.programmerDifficulty.set(difficulty);
    }

    public static void setGuiRemoteGridSnap(boolean snap) {
        ConfigHolder.client.general.guiRemoteGridSnap.set(snap);
    }

    public static void updateCoordTracker(boolean pathEnabled, boolean wirePath, boolean xRayEnabled, PathUpdateSetting pathUpdateSetting) {
        ConfigHolder.client.armor.pathEnabled.set(pathEnabled);
        ConfigHolder.client.armor.wirePath.set(wirePath);
        ConfigHolder.client.armor.xRayEnabled.set(xRayEnabled);
        ConfigHolder.client.armor.pathUpdateSetting.set(pathUpdateSetting);
    }

    public static void setShowPressureNumerically(boolean numeric) {
        ConfigHolder.client.armor.showPressureNumerically.set(numeric);
    }

    public static void setShowEnchantGlint(boolean show) {
        ConfigHolder.client.armor.showEnchantGlint.set(show);
    }

    public static int getOilLakeChance() {
        return ConfigHolder.common.general.oilGenerationChance.get();
    }
}
