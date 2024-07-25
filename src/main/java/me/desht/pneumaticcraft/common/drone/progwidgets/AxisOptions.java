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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.util.CodecUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;

import java.util.BitSet;
import java.util.Objects;

public class AxisOptions {
    public static final Codec<AxisOptions> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            CodecUtil.bitSetCodec(3).fieldOf("axes").forGetter(a -> a.options)
    ).apply(builder, AxisOptions::new));
    public static final StreamCodec<ByteBuf, AxisOptions> STREAM_CODEC = StreamCodec.composite(
            CodecUtil.bitSetStreamCodec(3), a -> a.options,
            AxisOptions::new
    );

    public static final AxisOptions TRUE = new AxisOptions(true, true, true);

    private final BitSet options;

    private AxisOptions(BitSet options) {
        this.options = options;
    }

    public AxisOptions(boolean x, boolean y, boolean z) {
        options = new BitSet(3);
        options.set(0, x);
        options.set(1, y);
        options.set(2, z);
    }

    public boolean shouldCheck(Direction.Axis axis) {
        return options.get(axis.ordinal());
    }

    public void setCheck(Direction.Axis axis, boolean check) {
        options.set(axis.ordinal(), check);
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putBoolean("checkX", shouldCheck(Direction.Axis.X));
        nbt.putBoolean("checkY", shouldCheck(Direction.Axis.Y));
        nbt.putBoolean("checkZ", shouldCheck(Direction.Axis.Z));
    }

    public void readFromNBT(CompoundTag nbt, boolean def) {
        setCheck(Direction.Axis.X, nbt.contains("checkX") ? nbt.getBoolean("checkX") : def);
        setCheck(Direction.Axis.Y, nbt.contains("checkY") ? nbt.getBoolean("checkY") : def);
        setCheck(Direction.Axis.Z, nbt.contains("checkZ") ? nbt.getBoolean("checkZ") : def);
    }

    public AxisOptions copy() {
        return new AxisOptions(BitSet.valueOf(options.toByteArray()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AxisOptions that = (AxisOptions) o;
        return Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(options);
    }
}
