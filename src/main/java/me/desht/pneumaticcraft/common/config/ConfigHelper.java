package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.common.item.ItemSeismicSensor;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoordTrackerHandler.PathUpdateSetting;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget.WidgetDifficulty;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.common.worldgen.ModWorldGen;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConfigHelper {
    private static net.minecraftforge.fml.config.ModConfig clientConfig;
    private static net.minecraftforge.fml.config.ModConfig commonConfig;

    static void refreshClient(net.minecraftforge.fml.config.ModConfig config) {
        clientConfig = config;

        ArmorUpgradeClientRegistry.getInstance().refreshConfig();
    }

    static void refreshCommon(net.minecraftforge.fml.config.ModConfig config) {
        commonConfig = config;

        TileEntityVacuumTrap.clearBlacklistCache();
        ModWorldGen.clearBlacklistCache();
        ItemSeismicSensor.clearCachedFluids();
    }

    private static void setValueAndSave(final net.minecraftforge.fml.config.ModConfig modConfig, final String path, final Object newValue) {
        modConfig.getConfigData().set(path, newValue);
        modConfig.save();
    }

    private static void setValuesAndSave(final net.minecraftforge.fml.config.ModConfig modConfig, final Map<String,Object>values) {
        values.forEach((k, v) -> modConfig.getConfigData().set(k, v));
        modConfig.save();
    }

    public static void setProgrammerDifficulty(WidgetDifficulty difficulty) {
        setValueAndSave(clientConfig, "general.programmer_difficulty", difficulty);
    }

    public static void setGuiRemoteGridSnap(boolean snap) {
        setValueAndSave(clientConfig, "general.gui_remote_grid_snap", snap);
    }

    public static void updateCoordTracker(boolean pathEnabled, boolean wirePath, boolean xRayEnabled, PathUpdateSetting pathUpdateSetting) {
        setValuesAndSave(clientConfig, ImmutableMap.of(
                "armor.path_enabled", pathEnabled,
                "armor.wire_path", wirePath,
                "armor.xray_enabled", xRayEnabled,
                "armor.path_update_setting", pathUpdateSetting)
        );
    }

    public static void setShowPressureNumerically(boolean numeric) {
        setValueAndSave(clientConfig, "armor.show_pressure_numerically", numeric);
    }

    public static int getOilLakeChance() {
        return ConfigHolder.common.general.oilGenerationChance.get();
    }

    public static void setShowEnchantGlint(boolean show) {
        setValueAndSave(clientConfig, "armor.show_enchant_glint", show);
    }

    public static ClientConfig client() {
        return ConfigHolder.client;
    }

    public static CommonConfig common() {
        return ConfigHolder.common;
    }
}
