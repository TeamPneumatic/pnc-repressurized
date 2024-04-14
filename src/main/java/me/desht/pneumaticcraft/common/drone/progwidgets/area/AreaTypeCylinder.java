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

import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeCylinder extends AreaType {

    public static final String ID = "cylinder";

    private EnumAxis axis = EnumAxis.X;
    private EnumCylinderType cylinderType = EnumCylinderType.FILLED;

    private enum EnumCylinderType implements ITranslatableEnum {
        FILLED("filled"), HOLLOW("hollow"), TUBE("tube");

        private final String name;

        EnumCylinderType(String name) {
            this.name = "pneumaticcraft.gui.progWidget.area.type.cylinder.cylinderType." + name;
        }

        @Override
        public String getTranslationKey() {
            return name;
        }
    }

    public AreaTypeCylinder() {
        super(ID);
    }

    @Override
    public String toString() {
        return getName() + "/" + cylinderType + "/" + axis;
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
                                        cylinderType == EnumCylinderType.FILLED ||
                                        cylinderType == EnumCylinderType.HOLLOW && (x == minX || x == maxX)) {

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
                                        cylinderType == EnumCylinderType.FILLED ||
                                        cylinderType == EnumCylinderType.HOLLOW && (y == minY || y == maxY)) {

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
                                        cylinderType == EnumCylinderType.FILLED ||
                                        cylinderType == EnumCylinderType.HOLLOW && (z == minZ || z == maxZ)) {

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
        widgets.add(new AreaTypeWidgetEnum<>("pneumaticcraft.gui.progWidget.area.type.cylinder.cylinderType", EnumCylinderType.class, () -> cylinderType, cylinderType -> this.cylinderType = cylinderType));
        widgets.add(new AreaTypeWidgetEnum<>("pneumaticcraft.gui.progWidget.area.type.general.axis", EnumAxis.class, () -> axis, axis -> this.axis = axis));
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByte("axis", (byte) axis.ordinal());
        tag.putByte("cylinderType", (byte) cylinderType.ordinal());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        axis = EnumAxis.values()[tag.getByte("axis")];
        cylinderType = EnumCylinderType.values()[tag.getByte("cylinderType")];
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        buffer.writeEnum(axis);
        buffer.writeEnum(cylinderType);
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        axis = buf.readEnum(EnumAxis.class);
        cylinderType = buf.readEnum(EnumCylinderType.class);
    }

    @Override
    public void convertFromLegacy(LegacyAreaWidgetConverter.EnumOldAreaType oldAreaType, int typeInfo) {
        switch (oldAreaType) {
            case X_CYLINDER -> axis = EnumAxis.X;
            case Y_CYLINDER -> axis = EnumAxis.Y;
            case Z_CYLINDER -> axis = EnumAxis.Z;
            default -> throw new IllegalArgumentException();
        }
    }
}
