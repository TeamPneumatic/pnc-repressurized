package me.desht.pneumaticcraft.api.misc;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

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

    public static IFormattableTextComponent bullet() {
        return new StringTextComponent(BULLET + " ");
    }
}
