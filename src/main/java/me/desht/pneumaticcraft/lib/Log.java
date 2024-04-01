/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.lib;

import me.desht.pneumaticcraft.api.lib.Names;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private static final Logger LOGGER = LogManager.getLogger(Names.MOD_ID);

    public static void debug(String message, Object... params) {
        LOGGER.log(Level.DEBUG, message, params);
    }

    public static void info(String message, Object... params) {
        LOGGER.log(Level.INFO, message, params);
    }

    public static void error(String message, Object... params) {
        LOGGER.log(Level.ERROR, message, params);
    }

    public static void warning(String message, Object... params) {
        LOGGER.log(Level.WARN, message, params);
    }

    public static void stacktrace(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }
}
