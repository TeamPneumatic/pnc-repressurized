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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class AreaTypeRandom extends AreaType {
    public static final MapCodec<AreaTypeRandom> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("picked_amount").forGetter(t -> t.pickedAmount)
    ).apply(builder, AreaTypeRandom::new));
    public static final StreamCodec<FriendlyByteBuf, AreaTypeRandom> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, t -> t.pickedAmount,
            AreaTypeRandom::new
    );
    public static final String ID = "random";
    private int pickedAmount;

    public AreaTypeRandom() {
        this(0);
    }

    public AreaTypeRandom(int pickedAmount) {
        super(ID);
        this.pickedAmount = pickedAmount;
    }

    @Override
    public String toString() {
        return getName() + "/" + pickedAmount;
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_RANDOM.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int sx = (maxX - minX) + 1;
        int sy = (maxY - minY) + 1;
        int sz = (maxZ - minZ) + 1;
        int size = sx * sy * sz;

        if (pickedAmount >= size) {
            // If we pick >= than there are blocks, just pick all blocks
            BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ).forEach(pos -> areaAdder.accept(pos.immutable()));
        } else {
            Random rand = ThreadLocalRandom.current();
            for (int i = 0; i < pickedAmount; i++) {
                int x = minX + rand.nextInt(sx);
                int y = minY + rand.nextInt(sy);
                int z = minZ + rand.nextInt(sz);
                areaAdder.accept(new BlockPos(x, y, z));
            }
        }
    }

    @Override
    public boolean isDeterministic() {
        return false;
    }

    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets) {
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidget.IntegerField("pneumaticcraft.gui.progWidget.area.type.random.blocksSelected",
                () -> pickedAmount, amount -> pickedAmount = amount));
    }

//    @Override
//    public void writeToNBT(CompoundTag tag) {
//        super.writeToNBT(tag);
//        tag.putInt("pickedAmount", pickedAmount);
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag) {
//        super.readFromNBT(tag);
//        pickedAmount = tag.getInt("pickedAmount");
//    }
//
//    @Override
//    public void writeToPacket(FriendlyByteBuf buffer) {
//        super.writeToPacket(buffer);
//        buffer.writeVarInt(pickedAmount);
//    }
//
//    @Override
//    public void readFromPacket(FriendlyByteBuf buf) {
//        super.readFromPacket(buf);
//        pickedAmount = buf.readVarInt();
//    }

    @Override
    public void convertFromLegacy(EnumOldAreaType oldAreaType, int typeInfo) {
        pickedAmount = typeInfo;
    }
}
