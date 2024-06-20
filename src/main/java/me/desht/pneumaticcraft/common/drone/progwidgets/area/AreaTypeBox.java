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
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeBox extends AreaType {
    public static final MapCodec<AreaTypeBox> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        StringRepresentable.fromEnum(EnumBoxType::values).optionalFieldOf("box_type", EnumBoxType.FILLED).forGetter(AreaTypeBox::boxType)
    ).apply(builder, AreaTypeBox::new));

    public static final StreamCodec<FriendlyByteBuf, AreaTypeBox> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(EnumBoxType.class), AreaTypeBox::boxType,
            AreaTypeBox::new
    );

    public static final String ID = "box";

    private EnumBoxType boxType;

    public AreaTypeBox(EnumBoxType boxType) {
        super(ID);
        this.boxType = boxType;
    }

    public AreaTypeBox() {
        this(EnumBoxType.FILLED);
    }

    public EnumBoxType boxType() {
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
            case FILL -> boxType = EnumBoxType.FILLED;
            case WALL -> boxType = EnumBoxType.HOLLOW;
            case FRAME -> boxType = EnumBoxType.FRAME;
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets) {
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.box.boxType", EnumBoxType.class, () -> boxType, boxType -> this.boxType = boxType));
    }

//    @Override
//    public void writeToNBT(CompoundTag tag) {
//        super.writeToNBT(tag);
//        tag.putByte("boxType", (byte) boxType.ordinal());
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag) {
//        super.readFromNBT(tag);
//        boxType = EnumBoxType.values()[tag.getByte("boxType")];
//    }
//
//    @Override
//    public void writeToPacket(FriendlyByteBuf buffer) {
//        super.writeToPacket(buffer);
//        buffer.writeEnum(boxType);
//    }
//
//    @Override
//    public void readFromPacket(FriendlyByteBuf buf) {
//        super.readFromPacket(buf);
//        boxType = buf.readEnum(EnumBoxType.class);
//    }

    public enum EnumBoxType implements ITranslatableEnum, StringRepresentable {
        FILLED("filled"), HOLLOW("hollow"), FRAME("frame");

        private final String name;

        EnumBoxType(String name) {
            this.name = "pneumaticcraft.gui.progWidget.area.type.box.boxType." + name;
        }

        @Override
        public String getTranslationKey() {
            return name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
