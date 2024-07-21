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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AreaTypeBox extends AreaType {
    public static final MapCodec<AreaTypeBox> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        StringRepresentable.fromEnum(BoxType::values).optionalFieldOf("box_type", BoxType.FILLED).forGetter(AreaTypeBox::boxType)
    ).apply(builder, AreaTypeBox::new));

    public static final StreamCodec<FriendlyByteBuf, AreaTypeBox> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(BoxType.class), AreaTypeBox::boxType,
            AreaTypeBox::new
    );

    public static final String ID = "box";

    private BoxType boxType;

    public AreaTypeBox(BoxType boxType) {
        super(ID);
        this.boxType = boxType;
    }

    public AreaTypeBox() {
        this(BoxType.FILLED);
    }

    @Override
    public AreaType copy() {
        return new AreaTypeBox(boxType);
    }

    public BoxType boxType() {
        return boxType;
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_BOX.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        switch (boxType) {
            case FILLED -> {
                for (int x = minX; x <= maxX; x++) {
                    for (int y = maxY; y >= minY; y--) {
                        for (int z = minZ; z <= maxZ; z++) {
                            areaAdder.accept(new BlockPos(x, y, z));
                        }
                    }
                }
            }
            case FRAME -> {
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            int axisRight = 0;
                            if (x == minX || x == maxX) axisRight++;
                            if (y == minY || y == maxY) axisRight++;
                            if (z == minZ || z == maxZ) axisRight++;
                            if (axisRight > 1) {
                                areaAdder.accept(new BlockPos(x, y, z));
                            }
                        }
                    }
                }
            }
            case HOLLOW -> {
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                                areaAdder.accept(new BlockPos(x, y, z));
                            }
                        }
                    }
                }
            }
            default -> throw new IllegalArgumentException(boxType.toString());
        }
    }

    @Override
    public String toString() {
        return getName() + "/" + boxType;
    }

    @Override
    public void convertFromLegacy(EnumOldAreaType oldAreaType, int typeInfo) {
        switch (oldAreaType) {
            case FILL -> boxType = BoxType.FILLED;
            case WALL -> boxType = BoxType.HOLLOW;
            case FRAME -> boxType = BoxType.FRAME;
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets) {
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.box.boxType", BoxType.class, () -> boxType, boxType -> this.boxType = boxType));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaTypeBox that = (AreaTypeBox) o;
        return boxType == that.boxType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(boxType);
    }

    public enum BoxType implements ITranslatableEnum, StringRepresentable {
        FILLED("filled"), HOLLOW("hollow"), FRAME("frame");

        private final String name;

        BoxType(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.area.type.box.boxType." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
