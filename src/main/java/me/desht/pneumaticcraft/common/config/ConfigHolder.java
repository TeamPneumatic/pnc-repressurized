package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.lib.Log;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHolder {
    public static ClientConfig client;
    public static CommonConfig common;
    private static ForgeConfigSpec configCommonSpec;
    private static ForgeConfigSpec configClientSpec;

    public static void init() {
        final Pair<ClientConfig, ForgeConfigSpec> spec1 = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        client = spec1.getLeft();
        configClientSpec = spec1.getRight();

        final Pair<CommonConfig, ForgeConfigSpec> spec2 = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        common = spec2.getLeft();
        configCommonSpec = spec2.getRight();

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ConfigHolder.configCommonSpec);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, ConfigHolder.configClientSpec);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigHolder::modConfig);
    }

    private static void modConfig(final net.minecraftforge.fml.config.ModConfig.ModConfigEvent event) {
        net.minecraftforge.fml.config.ModConfig config = event.getConfig();
        if (config.getSpec() == ConfigHolder.configClientSpec) {
            ConfigHelper.refreshClient(config);
            Log.debug("PNC-DEBUG [%s]: refreshed client config: programmer difficulty = %s", Thread.currentThread().getName(), PNCConfig.Client.programmerDifficulty);
        } else if (config.getSpec() == ConfigHolder.configCommonSpec) {
            ConfigHelper.refreshCommon(config);
        }
    }
}
