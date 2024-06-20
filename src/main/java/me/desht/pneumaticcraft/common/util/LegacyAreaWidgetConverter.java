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

import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.EnumOldAreaType;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.*;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.Util;

import java.util.EnumMap;
import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

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

    public static AreaType convertFromLegacyFormat(EnumOldAreaType oldType, int subTypeInfo) {
        String newTypeId = oldFormatToAreaTypes.get(oldType);
        if (newTypeId == null) {
            Log.error("Legacy import: no area converter found for {}! Substituting 'box'.", oldType);
            return new AreaTypeBox();
        } else {
            var s = PNCRegistries.AREA_TYPE_SERIALIZER_REGISTRY.get(RL(newTypeId));
            return s != null ? Util.make(s.createDefaultInstance(), t -> t.convertFromLegacy(oldType, subTypeInfo)) : new AreaTypeBox();
        }
    }
}
