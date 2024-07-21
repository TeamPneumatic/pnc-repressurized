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

package me.desht.pneumaticcraft.common.drone.progwidgets.area;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeSerializer;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeWidget;
import me.desht.pneumaticcraft.api.drone.area.EnumOldAreaType;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetAreaTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AreaTypeCylinder extends AreaType {
    public static final MapCodec<AreaTypeCylinder> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            StringRepresentable.fromEnum(CylinderType::values).optionalFieldOf("cylinder_type", CylinderType.FILLED).forGetter(t -> t.cylinderType),
            StringRepresentable.fromEnum(AreaAxis::values).optionalFieldOf("axis", AreaAxis.X).forGetter(t -> t.axis)
    ).apply(builder, AreaTypeCylinder::new));

    public static final StreamCodec<FriendlyByteBuf, AreaTypeCylinder> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(CylinderType.class), t -> t.cylinderType,
            NeoForgeStreamCodecs.enumCodec(AreaAxis.class), t -> t.axis,
            AreaTypeCylinder::new
    );

    public static final String ID = "cylinder";

    private AreaAxis axis;
    private CylinderType cylinderType;

    private AreaTypeCylinder(CylinderType cylinderType, AreaAxis axis) {
        super(ID);
        this.axis = axis;
        this.cylinderType = cylinderType;
    }

    public AreaTypeCylinder() {
        this(CylinderType.FILLED, AreaAxis.X);
    }

    @Override
    public AreaType copy() {
        return new AreaTypeCylinder(cylinderType, axis);
    }

    @Override
    public String toString() {
        return getName() + "/" + cylinderType + "/" + axis;
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_CYLINDER.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        switch (axis) {
            case X -> {
                double rad = PneumaticCraftUtils.distBetween(p1.getY(), p1.getZ(), p2.getY(), p2.getZ());
                double radSq = rad * rad;
                double innerRadius = rad - 1;
                double innerRadiusSq = innerRadius * innerRadius;
                minY = (int) (p1.getY() - rad - 1);
                minZ = (int) (p1.getZ() - rad - 1);
                maxY = (int) (p1.getY() + rad + 1);
                maxZ = (int) (p1.getZ() + rad + 1);
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        //noinspection SuspiciousNameCombination
                        double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1.getY(), p1.getZ(), y, z);
                        if (centerDistSq <= radSq) {
                            for (int x = minX; x <= maxX; x++) {
                                if (centerDistSq >= innerRadiusSq ||
                                        cylinderType == CylinderType.FILLED ||
                                        cylinderType == CylinderType.HOLLOW && (x == minX || x == maxX)) {

                                    areaAdder.accept(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
            case Y -> {
                double rad = PneumaticCraftUtils.distBetween(p1.getX(), p1.getZ(), p2.getX(), p2.getZ());
                double radSq = rad * rad;
                double innerRadius = rad - 1;
                double innerRadiusSq = innerRadius * innerRadius;
                minX = (int) (p1.getX() - rad - 1);
                minZ = (int) (p1.getZ() - rad - 1);
                maxX = (int) (p1.getX() + rad + 1);
                maxZ = (int) (p1.getZ() + rad + 1);
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1.getX(), p1.getZ(), x, z);
                        if (centerDistSq <= radSq) {
                            for (int y = minY; y <= maxY; y++) {
                                if (centerDistSq >= innerRadiusSq ||
                                        cylinderType == CylinderType.FILLED ||
                                        cylinderType == CylinderType.HOLLOW && (y == minY || y == maxY)) {

                                    areaAdder.accept(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
            case Z -> {
                double rad = PneumaticCraftUtils.distBetween(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                double radSq = rad * rad;
                double innerRadius = rad - 1;
                double innerRadiusSq = innerRadius * innerRadius;
                minX = (int) (p1.getX() - rad - 1);
                minY = (int) (p1.getY() - rad - 1);
                maxX = (int) (p1.getX() + rad + 1);
                maxY = (int) (p1.getY() + rad + 1);
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1.getX(), p1.getY(), x, y);
                        if (centerDistSq <= radSq) {
                            for (int z = minZ; z <= maxZ; z++) {
                                if (centerDistSq >= innerRadiusSq ||
                                        cylinderType == CylinderType.FILLED ||
                                        cylinderType == CylinderType.HOLLOW && (z == minZ || z == maxZ)) {

                                    areaAdder.accept(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
            default -> throw new IllegalArgumentException(axis.toString());
        }
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets) {
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.cylinder.cylinderType", CylinderType.class, () -> cylinderType, cylinderType -> this.cylinderType = cylinderType));
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.general.axis", AreaAxis.class, () -> axis, axis -> this.axis = axis));
    }

    @Override
    public void convertFromLegacy(EnumOldAreaType oldAreaType, int typeInfo) {
        switch (oldAreaType) {
            case X_CYLINDER -> axis = AreaAxis.X;
            case Y_CYLINDER -> axis = AreaAxis.Y;
            case Z_CYLINDER -> axis = AreaAxis.Z;
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaTypeCylinder that = (AreaTypeCylinder) o;
        return axis == that.axis && cylinderType == that.cylinderType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(axis, cylinderType);
    }

    public enum CylinderType implements ITranslatableEnum, StringRepresentable {
        FILLED("filled"), HOLLOW("hollow"), TUBE("tube");

        private final String name;

        CylinderType(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.area.type.cylinder.cylinderType." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

}
