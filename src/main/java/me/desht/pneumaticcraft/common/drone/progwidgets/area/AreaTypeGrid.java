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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeWidget;
import me.desht.pneumaticcraft.api.drone.area.EnumOldAreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeSerializer;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetAreaTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AreaTypeGrid extends AreaType {
    public static final MapCodec<AreaTypeGrid> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("interval").forGetter(t -> t.interval)
    ).apply(builder, AreaTypeGrid::new));

    public static final StreamCodec<FriendlyByteBuf, AreaTypeGrid> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, t -> t.interval,
            AreaTypeGrid::new
    );

    public static final String ID = "grid";

    private int interval;

    public AreaTypeGrid(int interval) {
        super(ID);
        this.interval = interval;
    }

    public AreaTypeGrid() {
        this(0);
    }

    @Override
    public AreaType copy() {
        return new AreaTypeGrid(interval);
    }

    @Override
    public String toString() {
        return getName() + "/" + interval;
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_GRID.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        if (p1.equals(p2) || interval <= 0) {
            areaAdder.accept(p1);
        } else {
            for (int x = minX; x <= maxX; x += interval) {
                for (int y = minY; y <= maxY; y += interval) {
                    for (int z = minZ; z <= maxZ; z += interval) {
                        areaAdder.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets){
        super.addUIWidgets(widgets);

        widgets.add(new AreaTypeWidget.IntegerField("pneumaticcraft.gui.progWidget.area.type.grid.interval",
                () -> interval, interval -> this.interval = interval));
    }

    @Override
    public void convertFromLegacy(EnumOldAreaType oldAreaType, int typeInfo){
        interval = typeInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaTypeGrid that = (AreaTypeGrid) o;
        return interval == that.interval;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(interval);
    }
}
