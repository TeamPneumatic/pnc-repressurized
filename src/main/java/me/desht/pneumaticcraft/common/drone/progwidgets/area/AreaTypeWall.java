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

import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeWall extends AreaType{

    public static final String ID = "wall";

    private EnumAxis axis = EnumAxis.X;

    public AreaTypeWall(){
        super(ID);
    }

    @Override
    public String toString() {
        return getName() + "/" + axis;
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
        widgets.add(new AreaTypeWidgetEnum<>("pneumaticcraft.gui.progWidget.area.type.general.axis", EnumAxis.class, () -> axis, axis -> this.axis = axis));
    }

    @Override
    public void writeToNBT(CompoundTag tag){
        super.writeToNBT(tag);
        tag.putByte("axis", (byte)axis.ordinal());
    }

    @Override
    public void readFromNBT(CompoundTag tag){
        super.readFromNBT(tag);
        axis = EnumAxis.values()[tag.getByte("axis")];
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        buffer.writeByte(axis.ordinal());
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        axis = EnumAxis.values()[buf.readByte()];
    }

    @Override
    public void convertFromLegacy(LegacyAreaWidgetConverter.EnumOldAreaType oldAreaType, int typeInfo){
        switch (oldAreaType) {
            case X_WALL -> axis = EnumAxis.X;
            case Y_WALL -> axis = EnumAxis.Y;
            case Z_WALL -> axis = EnumAxis.Z;
            default -> throw new IllegalArgumentException();
        }
    }
}
