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

public class AreaTypeTorus extends AreaType {
    public static final MapCodec<AreaTypeTorus> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        StringRepresentable.fromEnum(AreaAxis::values).optionalFieldOf("axis", AreaAxis.X).forGetter(t -> t.axis),
        StringRepresentable.fromEnum(TorusType::values).optionalFieldOf("torus_type", TorusType.FILLED).forGetter(t -> t.torusType)
    ).apply(builder, AreaTypeTorus::new));
    public static final StreamCodec<FriendlyByteBuf, AreaTypeTorus> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(AreaAxis.class), t -> t.axis,
            NeoForgeStreamCodecs.enumCodec(TorusType.class), t -> t.torusType,
            AreaTypeTorus::new
    );

    public static final String ID = "torus";

    private AreaAxis axis;
    private TorusType torusType;

    public AreaTypeTorus(AreaAxis axis, TorusType torusType) {
        super(ID);
        this.axis = axis;
        this.torusType = torusType;
    }

    @Override
    public AreaType copy() {
        return new AreaTypeTorus(axis, torusType);
    }

    public AreaTypeTorus() {
        this(AreaAxis.X, TorusType.FILLED);
    }

    @Override
    public String toString() {
        return getName() + "/" + torusType + "/" + axis;
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_TORUS.get();
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
                double radMinor = Math.abs(p1.getX() - p2.getX());

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
                            if (torusType == TorusType.FILLED) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (ySq + zSq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            } else if (torusType == TorusType.HOLLOW) {
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
                double radMinor = Math.abs(p1.getY() - p2.getY());

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
                            if (torusType == TorusType.FILLED) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (xSq + zSq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            } else if (torusType == TorusType.HOLLOW) {
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
                double radMinor = Math.abs(p1.getZ() - p2.getZ());

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
                            if (torusType == TorusType.FILLED) {
                                if ((xSq + ySq + zSq + RSq - rSq)*(xSq + ySq + zSq + RSq - rSq) < (4 * RSq * (xSq + ySq))) {
                                    areaAdder.accept(new BlockPos(x + pX, y + pY, z + pZ));
                                }
                            } else if (torusType == TorusType.HOLLOW) {
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
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.torus.torusType", TorusType.class, () -> torusType, torusType -> this.torusType = torusType));
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.general.axis", AreaAxis.class, () -> axis, axis -> this.axis = axis));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaTypeTorus that = (AreaTypeTorus) o;
        return axis == that.axis && torusType == that.torusType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(axis, torusType);
    }

    public enum TorusType implements ITranslatableEnum, StringRepresentable {
        FILLED("filled"), HOLLOW("hollow");

        private final String name;

        TorusType(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.area.type.torus.torusType." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
