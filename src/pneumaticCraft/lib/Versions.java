package pneumaticCraft.lib;

public class Versions{
    private static final String MASSIVE = "@MASSIVE@";
    private static final String MAJOR = "@MAJOR@";
    private static final String MINOR = "@MINOR@";
    private static final String BUILD = "@BUILD_NUMBER@";
    private static final String MC_VERSION = "@MC_VERSION@";

    public static String fullVersionString(){

        return String.format("%s-%s.%s.%s-%s", MC_VERSION, MASSIVE, MAJOR, MINOR, BUILD);
    }
}
