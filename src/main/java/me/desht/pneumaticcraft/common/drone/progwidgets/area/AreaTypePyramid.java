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
import me.desht.pneumaticcraft.api.drone.area.AreaTypeWidget;
import me.desht.pneumaticcraft.api.drone.area.EnumOldAreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeSerializer;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetAreaTypes;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AreaTypePyramid extends AreaType {
    public static final MapCodec<AreaTypePyramid> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            StringRepresentable.fromEnum(AreaAxis::values).optionalFieldOf("axis", AreaAxis.X).forGetter(t -> t.axis),
            StringRepresentable.fromEnum(PyramidType::values).optionalFieldOf("pyramid_type", PyramidType.FILLED).forGetter(t -> t.pyramidType)
    ).apply(builder, AreaTypePyramid::new));
    public static final StreamCodec<FriendlyByteBuf, AreaTypePyramid> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(AreaAxis.class), t -> t.axis,
            NeoForgeStreamCodecs.enumCodec(PyramidType.class), t -> t.pyramidType,
            AreaTypePyramid::new
    );

    public static final String ID = "pyramid";

    private AreaAxis axis;
    private PyramidType pyramidType;

    public AreaTypePyramid() {
        this(AreaAxis.X, PyramidType.FILLED);
    }

    public AreaTypePyramid(AreaAxis axis, PyramidType pyramidType) {
        super(ID);
        this.axis = axis;
        this.pyramidType = pyramidType;
    }

    @Override
    public AreaType copy() {
        return new AreaTypePyramid(axis, pyramidType);
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_PYRAMID.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        switch (axis) {
            case X -> {
                if (p2.getX() != p1.getX()) {
                    Vec3 lineVec = new Vec3(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ()).normalize();
                    lineVec = new Vec3(lineVec.x, lineVec.y / lineVec.x, lineVec.z / lineVec.x);
                    double curY = p1.getY() - lineVec.y;
                    int x = p1.getX() + (p2.getX() > p1.getX() ? -1 : 1);
                    double curZ = p1.getZ() - lineVec.z;
                    int prevDY = -1;
                    int prevDZ = -1;
                    while (x != p2.getX()) {

                        x += p2.getX() > p1.getX() ? 1 : -1;
                        curY += lineVec.y;
                        curZ += lineVec.z;

                        int dY = Math.abs((int) (curY - p1.getY()));
                        int dZ = Math.abs((int) (curZ - p1.getZ()));
                        if (dY == prevDY) prevDY--;
                        if (dZ == prevDZ) prevDZ--;

                        for (int y = p1.getY() - dY; y <= p1.getY() + dY; y++) {
                            for (int z = p1.getZ() - dZ; z <= p1.getZ() + dZ; z++) {
                                if (pyramidType == PyramidType.FILLED || x == p2.getX() || z < p1.getZ() - prevDZ || z > p1.getZ() + prevDZ || y < p1.getY() - prevDY || y > p1.getY() + prevDY)
                                    areaAdder.accept(new BlockPos(x, y, z));
                            }
                        }
                        prevDY = dY;
                        prevDZ = dZ;
                    }
                }
            }
            case Y -> {
                if (p2.getY() != p1.getY()) {
                    Vec3 lineVec = new Vec3(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ()).normalize();
                    lineVec = new Vec3(lineVec.x / lineVec.y, lineVec.y, lineVec.z / lineVec.y);
                    double curX = p1.getX() - lineVec.x;
                    int y = p1.getY() + (p2.getY() > p1.getY() ? -1 : 1);
                    double curZ = p1.getZ() - lineVec.z;
                    int prevDX = -1;
                    int prevDZ = -1;
                    while (y != p2.getY()) {

                        y += p2.getY() > p1.getY() ? 1 : -1;
                        curX += lineVec.x;
                        curZ += lineVec.z;

                        int dX = Math.abs((int) (curX - p1.getX()));
                        int dZ = Math.abs((int) (curZ - p1.getZ()));
                        if (dX == prevDX) prevDX--;
                        if (dZ == prevDZ) prevDZ--;

                        int miniX = p1.getX() - dX;
                        int maxiX = p1.getX() + dX;
                        int miniZ = p1.getZ() - dZ;
                        int maxiZ = p1.getZ() + dZ;
                        for (int x = miniX; x <= maxiX; x++) {
                            for (int z = miniZ; z <= maxiZ; z++) {
                                if (pyramidType == PyramidType.FILLED || y == p2.getY() || z < p1.getZ() - prevDZ || z > p1.getZ() + prevDZ || x < p1.getX() - prevDX || x > p1.getX() + prevDX)
                                    areaAdder.accept(new BlockPos(x, y, z));
                            }
                        }
                        prevDX = dX;
                        prevDZ = dZ;
                    }
                }
            }
            case Z -> {
                if (p2.getZ() != p1.getZ()) {
                    Vec3 lineVec = new Vec3(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ()).normalize();
                    lineVec = new Vec3(lineVec.x / lineVec.z, lineVec.y / lineVec.z, lineVec.z);
                    double curX = p1.getX() - lineVec.x;
                    int z = p1.getZ() + (p2.getZ() > p1.getZ() ? -1 : 1);
                    double curY = p1.getY() - lineVec.y;
                    int prevDX = -1;
                    int prevDY = -1;
                    while (z != p2.getZ()) {

                        z += p2.getZ() > p1.getZ() ? 1 : -1;
                        curX += lineVec.x;
                        curY += lineVec.y;
                        int dX = Math.abs((int) (curX - p1.getX()));
                        int dY = Math.abs((int) (curY - p1.getY()));
                        if (dX == prevDX) prevDX--;
                        if (dY == prevDY) prevDY--;

                        for (int x = p1.getX() - dX; x <= p1.getX() + dX; x++) {
                            for (int y = p1.getY() - dY; y <= p1.getY() + dY; y++) {
                                if (pyramidType == PyramidType.FILLED || z == p2.getZ() || x < p1.getX() - prevDX || x > p1.getX() + prevDX || y < p1.getY() - prevDY || y > p1.getY() + prevDY)
                                    areaAdder.accept(new BlockPos(x, y, z));
                            }
                        }
                        prevDX = dX;
                        prevDY = dY;
                    }
                }
            }
            default -> throw new IllegalArgumentException(axis.toString());
        }
    }

    @Override
    public String toString() {
        return getName() + "/" + pyramidType + "/" + axis;
    }

    @Override
    public void convertFromLegacy(EnumOldAreaType oldAreaType, int typeInfo) {
        switch (oldAreaType) {
            case X_PYRAMID -> axis = AreaAxis.X;
            case Y_PYRAMID -> axis = AreaAxis.Y;
            case Z_PYRAMID -> axis = AreaAxis.Z;
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets) {
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.general.axis", AreaAxis.class, () -> axis, axis -> this.axis = axis));
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.pyramid.pyramidType", PyramidType.class, () -> pyramidType, pyramidType -> this.pyramidType = pyramidType));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaTypePyramid that = (AreaTypePyramid) o;
        return axis == that.axis && pyramidType == that.pyramidType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(axis, pyramidType);
    }

    public enum PyramidType implements ITranslatableEnum, StringRepresentable {
        FILLED("filled"), HOLLOW("hollow");

        private final String name;

        PyramidType(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.area.type.pyramid.pyramidType." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
