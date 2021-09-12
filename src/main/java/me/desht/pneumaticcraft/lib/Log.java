package me.desht.pneumaticcraft.lib;

import me.desht.pneumaticcraft.api.lib.Names;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private static final Logger LOGGER = LogManager.getLogger(Names.MOD_ID);

    public static void debug(String message, Object... params) {
        LOGGER.log(Level.DEBUG, String.format(message, params));
    }

    public static void info(String message, Object... params) {
        LOGGER.log(Level.INFO, String.format(message, params));
    }

    public static void error(String message, Object... params) {
        LOGGER.log(Level.ERROR, String.format(message, params));
    }

    public static void warning(String message, Object... params) {
        LOGGER.log(Level.WARN, String.format(message, params));
    }

    public static void stacktrace(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }
}
