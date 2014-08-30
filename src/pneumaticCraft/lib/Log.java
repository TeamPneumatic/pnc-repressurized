package pneumaticCraft.lib;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log{
    private static Logger logger = LogManager.getLogger(Names.MOD_ID);

    public static void info(String message){
        logger.log(Level.INFO, message);
    }

    public static void error(String message){
        logger.log(Level.ERROR, message);
    }

    public static void warning(String message){
        logger.log(Level.WARN, message);
    }
}
