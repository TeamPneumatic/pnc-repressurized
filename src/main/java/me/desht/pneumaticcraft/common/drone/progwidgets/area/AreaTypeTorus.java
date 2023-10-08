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
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeTorus extends AreaType {

    public static final String ID = "torus";

    private EnumAxis axis = EnumAxis.Y;
    private EnumTorusType torusType = EnumTorusType.FILLED;

    private enum EnumTorusType implements ITranslatableEnum {
        FILLED("filled"), HOLLOW("hollow");

        private final String name;

        EnumTorusType(String name) {
            this.name = "pneumaticcraft.gui.progWidget.area.type.torus.torusType." + name;
        }

        @Override
        public String getTranslationKey() {
            return name;
        }
    }

    public AreaTypeTorus() {
        super(ID);
    }

    @Override
    public String toString() {
        return getName() + "/" + torusType + "/" + axis;
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        switch (axis) {
            case X -> {
                // Calculations from https://en.wikipedia.org/wiki/Torus
                // Equation that describe the surface of the torus radially symmetric about the X-axis:
                // (x^2 + y^2 + z^2 + R^2 - r^2)^2 = 4 * R^2 * (y^2 + z^2)

                // Major radius (R) is from center of torus to center of tube
                double radMajor = Math.round(PneumaticCraftUtils.distBetween(p1.getY(), p1.getZ(), p2.getY(), p2.getZ()));
                // Minor radius (r) is radius of tube
                double radMinor = (double)Math.abs(p1.getX() - p2.getX());

                int RSq = (int)(radMajor*radMajor);
                int rSq = (int)(radMinor*radMinor);
                // The 'inner' radius (squared) of the tube, used for hollow shape
                int rSqI = (int)((radMinor-1)*(radMinor-1));

                int pX = p1.getX();
                int pY = p1.getY();
                int pZ = p1.getZ();

                int apoX = (int)(radMinor);
                int apoY = (int)(radMajor + radMinor);
                int apoZ = (int)(radMajor + radMinor);

                for (int y = -apoY; y <= apoY; y++) {
                    for (int z = -apoZ; z <= apoZ; z++) {
                        for (int x = -apoX; x <= apoX; x++) {
                            int xSq = x*x;
                            int ySq = y*y;
                            int zSq = z*z;
                            if (torusType == EnumTorusType.FILLED) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (ySq + zSq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            } else if (torusType == EnumTorusType.HOLLOW) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (ySq + zSq)) &&
                                    (xSq + ySq + zSq + RSq - rSqI)*(xSq + ySq + zSq + RSq - rSqI) >= (4 * RSq * (ySq + zSq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            }
                        }
                    }
                }
            }
            case Y -> {
                // Calculations from https://en.wikipedia.org/wiki/Torus
                // Equation that describe the surface of the torus radially symmetric about the Y-axis:
                // (x^2 + y^2 + z^2 + R^2 - r^2)^2 = 4 * R^2 * (x^2 + z^2)

                // Major radius (R) is from center of torus to center of tube
                double radMajor = Math.round(PneumaticCraftUtils.distBetween(p1.getX(), p1.getZ(), p2.getX(), p2.getZ()));
                // Minor radius (r) is radius of tube
                double radMinor = (double)Math.abs(p1.getY() - p2.getY());

                int RSq = (int)(radMajor*radMajor);
                int rSq = (int)(radMinor*radMinor);
                // The 'inner' radius (squared) of the tube, used for hollow shape
                int rSqI = (int)((radMinor-1)*(radMinor-1));

                int pX = p1.getX();
                int pY = p1.getY();
                int pZ = p1.getZ();

                int apoX = (int)(radMajor + radMinor);
                int apoY = (int)(radMinor);
                int apoZ = (int)(radMajor + radMinor);

                for (int x = -apoX; x <= apoX; x++) {
                    for (int z = -apoZ; z <= apoZ; z++) {
                        for (int y = -apoY; y <= apoY; y++) {
                            int xSq = x*x;
                            int ySq = y*y;
                            int zSq = z*z;
                            if (torusType == EnumTorusType.FILLED) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (xSq + zSq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            } else if (torusType == EnumTorusType.HOLLOW) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (xSq + zSq)) &&
                                    (xSq + ySq + zSq + RSq - rSqI)*(xSq + ySq + zSq + RSq - rSqI) >= (4 * RSq * (xSq + zSq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            }
                        }
                    }
                }
            }
            case Z -> {
                // Calculations from https://en.wikipedia.org/wiki/Torus
                // Equation that describe the surface of the torus radially symmetric about the Z-axis:
                // (x^2 + y^2 + z^2 + R^2 - r^2)^2 = 4 * R^2 * (x^2 + y^2)

                // Major radius (R) is from center of torus to center of tube
                double radMajor = Math.round(PneumaticCraftUtils.distBetween(p1.getX(), p1.getY(), p2.getX(), p2.getY()));
                // Minor radius (r) is radius of tube
                double radMinor = (double)Math.abs(p1.getZ() - p2.getZ());

                int RSq = (int)(radMajor*radMajor);
                int rSq = (int)(radMinor*radMinor);
                // The 'inner' radius (squared) of the tube, used for hollow shape
                int rSqI = (int)((radMinor-1)*(radMinor-1));

                int pX = p1.getX();
                int pY = p1.getY();
                int pZ = p1.getZ();

                int apoX = (int)(radMajor + radMinor);
                int apoY = (int)(radMajor + radMinor);
                int apoZ = (int)(radMinor);

                for (int x = -apoX; x <= apoX; x++) {
                    for (int y = -apoY; y <= apoY; y++) {
                        for (int z = -apoZ; z <= apoZ; z++) {
                            int xSq = x*x;
                            int ySq = y*y;
                            int zSq = z*z;
                            if (torusType == EnumTorusType.FILLED) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (xSq + ySq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            } else if (torusType == EnumTorusType.HOLLOW) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (xSq + ySq)) &&
                                    (xSq + ySq + zSq + RSq - rSqI)*(xSq + ySq + zSq + RSq - rSqI) >= (4 * RSq * (xSq + ySq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
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
        widgets.add(new AreaTypeWidgetEnum<>("pneumaticcraft.gui.progWidget.area.type.torus.torusType", EnumTorusType.class, () -> torusType, torusType -> this.torusType = torusType));
        widgets.add(new AreaTypeWidgetEnum<>("pneumaticcraft.gui.progWidget.area.type.general.axis", EnumAxis.class, () -> axis, axis -> this.axis = axis));
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByte("axis", (byte) axis.ordinal());
        tag.putByte("torusType", (byte) torusType.ordinal());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        axis = EnumAxis.values()[tag.getByte("axis")];
        torusType = EnumTorusType.values()[tag.getByte("torusType")];
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        buffer.writeByte(axis.ordinal());
        buffer.writeByte(torusType.ordinal());
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        axis = EnumAxis.values()[buf.readByte()];
        torusType = EnumTorusType.values()[buf.readByte()];
    }
}
