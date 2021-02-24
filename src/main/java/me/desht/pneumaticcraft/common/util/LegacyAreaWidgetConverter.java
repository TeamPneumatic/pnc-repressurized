package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.area.*;
import me.desht.pneumaticcraft.lib.Log;

import java.util.EnumMap;
import java.util.Map;

/**
 * Exists to support saved drone programs from 1.12 and older versions of PneumaticCraft, and also to support
 * the Computer Control progwidget's "addArea" and "removeArea" methods.
 */
public class LegacyAreaWidgetConverter {
    private static final Map<EnumOldAreaType, String> oldFormatToAreaTypes = new EnumMap<>(EnumOldAreaType.class);

    static {
        register(AreaTypeBox.ID, EnumOldAreaType.FILL, EnumOldAreaType.WALL, EnumOldAreaType.FRAME);
        register(AreaTypeSphere.ID, EnumOldAreaType.SPHERE);
        register(AreaTypeLine.ID, EnumOldAreaType.LINE);
        register(AreaTypeWall.ID, EnumOldAreaType.X_WALL, EnumOldAreaType.Y_WALL, EnumOldAreaType.Z_WALL);
        register(AreaTypeCylinder.ID, EnumOldAreaType.X_CYLINDER, EnumOldAreaType.Y_CYLINDER, EnumOldAreaType.Z_CYLINDER);
        register(AreaTypePyramid.ID, EnumOldAreaType.X_PYRAMID, EnumOldAreaType.Y_PYRAMID, EnumOldAreaType.Z_PYRAMID);
        register(AreaTypeGrid.ID, EnumOldAreaType.GRID);
        register(AreaTypeRandom.ID, EnumOldAreaType.RANDOM);
        if (oldFormatToAreaTypes.size() != EnumOldAreaType.values().length)
            throw new IllegalStateException("Not all old formats are handled!");
    }

    private static void register(String id, EnumOldAreaType... oldTypes) {
        for (EnumOldAreaType oldType : oldTypes) {
            oldFormatToAreaTypes.put(oldType, id);
        }
    }

    public static AreaType convertFromLegacyFormat(EnumOldAreaType oldType, int typeInfo) {
        String newTypeId = oldFormatToAreaTypes.get(oldType);
        if (newTypeId == null) {
            Log.error("Legacy import: no area converter found for " + oldType + "! Substituting 'box'.");
            return new AreaTypeBox();
        } else {
            AreaType type = ProgWidgetArea.createType(newTypeId);
            type.convertFromLegacy(oldType, typeInfo);
            return type;
        }
    }

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

        EnumOldAreaType(String name) {
            this(name, false);
        }

        EnumOldAreaType(String name, boolean utilizesTypeInfo) {
            this.name = name;
            this.utilizesTypeInfo = utilizesTypeInfo;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
