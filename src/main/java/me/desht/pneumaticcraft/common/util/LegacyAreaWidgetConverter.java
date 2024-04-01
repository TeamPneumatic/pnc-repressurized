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

package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.*;
import me.desht.pneumaticcraft.lib.Log;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Exists to support saved drone programs from 1.12 and older versions of PneumaticCraft, and also to support
 * the Computer Control progwidget's "addArea" and "removeArea" methods.
 */
public class LegacyAreaWidgetConverter {
    private static final Map<String,EnumOldAreaType> OLD_AREA_TYPE_MAP = new HashMap<>();
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

        for (EnumOldAreaType oldAreaType : EnumOldAreaType.values()) {
            OLD_AREA_TYPE_MAP.put(oldAreaType.name.toLowerCase(Locale.ROOT), oldAreaType);
        }
    }

    private static void register(String id, EnumOldAreaType... oldTypes) {
        for (EnumOldAreaType oldType : oldTypes) {
            oldFormatToAreaTypes.put(oldType, id);
        }
    }

    public static AreaType convertFromLegacyFormat(EnumOldAreaType oldType, int typeInfo) {
        String newTypeId = oldFormatToAreaTypes.get(oldType);
        if (newTypeId == null) {
            Log.error("Legacy import: no area converter found for {}! Substituting 'box'.", oldType);
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

        public static EnumOldAreaType byName(String name) {
            return OLD_AREA_TYPE_MAP.get(name.toLowerCase(Locale.ROOT));
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
