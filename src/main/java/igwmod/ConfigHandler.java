package igwmod;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler{

    public static boolean shouldShowTooltip;
    public static boolean debugMode;
    private static Configuration conf;

    public static void init(File configFile){
        conf = new Configuration(configFile);
        conf.load();
        shouldShowTooltip = conf.get(Configuration.CATEGORY_GENERAL, "Should show tooltip", true).getBoolean(true);
        debugMode = conf.get(Configuration.CATEGORY_GENERAL, "Debug mode", false).getBoolean(false);
        conf.save();
    }

    public static void disableTooltip(){
        conf.load();
        conf.get(Configuration.CATEGORY_GENERAL, "Should show tooltip", true).set(false);
        shouldShowTooltip = false;
        conf.save();
    }
}
