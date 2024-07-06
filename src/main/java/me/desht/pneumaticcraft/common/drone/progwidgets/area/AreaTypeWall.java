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
import me.desht.pneumaticcraft.common.registry.ModProgWidgetAreaTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AreaTypeWall extends AreaType {
    public static final MapCodec<AreaTypeWall> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            StringRepresentable.fromEnum(AreaAxis::values).fieldOf("axis").forGetter(t -> t.axis)
    ).apply(builder, AreaTypeWall::new));
    public static final StreamCodec<FriendlyByteBuf, AreaTypeWall> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(AreaAxis.class), t -> t.axis,
            AreaTypeWall::new
    );

    public static final String ID = "wall";

    private AreaAxis axis;

    public AreaTypeWall() {
        this(AreaAxis.X);
    }

    public AreaTypeWall(AreaAxis axis) {
        super(ID);
        this.axis = axis;
    }

    @Override
    public AreaType copy() {
        return new AreaTypeWall(axis);
    }

    @Override
    public String toString() {
        return getName() + "/" + axis;
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_WALL.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        switch (axis) {
            case X -> {
                Vec3 lineVec = new Vec3(0, p2.getY() - p1.getY(), p2.getZ() - p1.getZ()).normalize();
                lineVec = new Vec3(lineVec.x, lineVec.y / 10, lineVec.z / 10);
                double curY = p1.getY() + 0.5;
                double curZ = p1.getZ() + 0.5;
                double totalDistance = 0;
                double maxDistance = Math.sqrt(Math.pow(p1.getY() - p2.getY(), 2) + Math.pow(p1.getZ() - p2.getZ(), 2));
                while (totalDistance <= maxDistance) {
                    totalDistance += 0.1;
                    curY += lineVec.y;
                    curZ += lineVec.z;
                    for (int z = minX; z <= maxX; z++) {
                        areaAdder.accept(BlockPos.containing(z, curY, curZ));
                    }
                }
            }
            case Y -> {
                Vec3 lineVec = new Vec3(p2.getX() - p1.getX(), 0, p2.getZ() - p1.getZ()).normalize();
                lineVec = new Vec3(lineVec.x, lineVec.y / 10, lineVec.z / 10);
                double curX = p1.getX() + 0.5;
                double curZ = p1.getZ() + 0.5;
                double totalDistance = 0;
                double maxDistance = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getZ() - p2.getZ(), 2));
                while (totalDistance <= maxDistance) {
                    totalDistance += 0.1;
                    curX += lineVec.x;
                    curZ += lineVec.z;
                    for (int y = minY; y <= maxY; y++) {
                        areaAdder.accept(BlockPos.containing(curX, y, curZ));
                    }
                }
            }
            case Z -> {
                Vec3 lineVec = new Vec3(p2.getX() - p1.getX(), p2.getY() - p1.getY(), 0).normalize();
                lineVec = new Vec3(lineVec.x / 10, lineVec.y / 10, lineVec.z);
                double curX = p1.getX() + 0.5;
                double curY = p1.getY() + 0.5;
                double totalDistance = 0;
                double maxDistance = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
                while (totalDistance <= maxDistance) {
                    totalDistance += 0.1;
                    curX += lineVec.x;
                    curY += lineVec.y;
                    for (int z = minZ; z <= maxZ; z++) {
                        areaAdder.accept(BlockPos.containing(curX, curY, z));
                    }
                }
            }
            default -> throw new IllegalArgumentException(axis.toString());
        }
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets){
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.general.axis", AreaAxis.class,
                () -> axis, axis -> this.axis = axis));
    }

    @Override
    public void convertFromLegacy(EnumOldAreaType oldAreaType, int typeInfo){
        switch (oldAreaType) {
            case X_WALL -> axis = AreaAxis.X;
            case Y_WALL -> axis = AreaAxis.Y;
            case Z_WALL -> axis = AreaAxis.Z;
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaTypeWall that = (AreaTypeWall) o;
        return axis == that.axis;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(axis);
    }
}
