package igwmod.lib;

import org.lwjgl.input.Keyboard;

public class Constants{
    public static final String MOD_ID = "igwmod";
    private static final String MASSIVE = "@MASSIVE@";
    private static final String MAJOR = "@MAJOR@";
    private static final String MINOR = "@MINOR@";
    private static final String BUILD = "@BUILD_NUMBER@";
    private static final String MC_VERSION = "@MC_VERSION@";
    //  public static final String WIKI_PAGE_LOCATION = "https://github.com/MineMaarten/IGW-mod/archive/master.zip";// "http://www.minemaarten.com/wp-content/uploads/2013/12/WikiPages.zip";
    //  public static final String ZIP_NAME = "igw";
    //   public static final int CONNECTION_TIMEOUT = 3000;
    //    public static final int READ_TIMEOUT = 5000;
    //  public static final String INTERNET_UPDATE_CONFIG_KEY = "Update pages via internet";
    //  public static final String USE_OFFLINE_WIKIPAGES_KEY = "Use offline wikipages";
    public static final int DEFAULT_KEYBIND_OPEN_GUI = Keyboard.KEY_I;

    public static final int TEXTURE_MAP_ID = 15595;

    public static String fullVersionString(){

        return String.format("%s-%s.%s.%s-%s", MC_VERSION, MASSIVE, MAJOR, MINOR, BUILD);
    }
}
