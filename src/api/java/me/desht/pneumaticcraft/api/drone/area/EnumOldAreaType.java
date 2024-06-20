package me.desht.pneumaticcraft.api.drone.area;

import net.minecraft.Util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Old-style area types from 1.12 and earlier. Need to keep this around to support loading old programs from pastebin.
 */
public enum EnumOldAreaType {
    FILL("Filled"),
    FRAME("Frame"),
    WALL("Walls"),
    SPHERE("Sphere"),
    LINE("Line"),
    X_WALL("X-Wall"),
    Y_WALL("Y-Wall"),
    Z_WALL("Z-Wall"),
    X_CYLINDER("X-Cylinder"),
    Y_CYLINDER("Y-Cylinder"),
    Z_CYLINDER("Z-Cylinder"),
    X_PYRAMID("X-Pyramid"),
    Y_PYRAMID("Y-Pyramid"),
    Z_PYRAMID("Z-Pyramid"),
    GRID("Grid", true),
    RANDOM("Random", true);

    private final String name;
    public final boolean utilizesTypeInfo;

    private static final Map<String, EnumOldAreaType> OLD_AREA_TYPE_MAP = Util.make(new HashMap<>(), map -> {
        for (EnumOldAreaType oldAreaType : EnumOldAreaType.values()) {
            map.put(oldAreaType.name.toLowerCase(Locale.ROOT), oldAreaType);
        }
    });

    EnumOldAreaType(String name) {
        this(name, false);
    }

    EnumOldAreaType(String name, boolean utilizesTypeInfo) {
        this.name = name;
        this.utilizesTypeInfo = utilizesTypeInfo;
    }

    public static EnumOldAreaType byName(String name) {
        return OLD_AREA_TYPE_MAP.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return name;
    }
}
