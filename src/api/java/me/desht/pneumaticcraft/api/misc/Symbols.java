/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * A small collection of handy Unicode symbols
 */
public class Symbols {
    public static final String BULLET = "\u2022";
    public static final String ARROW_LEFT_SHORT = "\u2b05";
    public static final String ARROW_LEFT = "\u27f5";
    public static final String ARROW_RIGHT = "\u27f6";
    public static final String ARROW_DOWN_RIGHT = "\u21b3";
    public static final String TRIANGLE_UP = "\u25b2";
    public static final String TRIANGLE_DOWN = "\u25bc";
    public static final String TRIANGLE_LEFT = "\u25c0";
    public static final String TRIANGLE_RIGHT = "\u25b6";
    public static final String DEGREES = "\u00b0";
    public static final String INFINITY = "\u221E";
    public static final String TRIANGLE_UP_LEFT = "\u25e4";
    public static final String TRIANGLE_DOWN_RIGHT = "\u25e2";
    public static final String CIRCULAR_ARROW = "\u293e";
    public static final String TICK_MARK = "\u2714";
    public static final String X_MARK = "\u2717";

    public static MutableComponent bullet() {
        return Component.literal(BULLET + " ");
    }
}
