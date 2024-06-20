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

package me.desht.pneumaticcraft.client.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record PointXY(int x, int y) {
    public static final Codec<PointXY> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        Codec.INT.fieldOf("x").forGetter(PointXY::x),
        Codec.INT.fieldOf("y").forGetter(PointXY::y)
    ).apply(builder, PointXY::new));
    public static final StreamCodec<ByteBuf, PointXY> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PointXY::x,
            ByteBufCodecs.INT, PointXY::y,
            PointXY::new
    );

    public static final PointXY ZERO = new PointXY(0, 0);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointXY pointXY)) return false;
        return x == pointXY.x && y == pointXY.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public double distance(PointXY p2) {
        double px = p2.x - this.x;
        double py = p2.y - this.y;
        return Math.sqrt(px * px + py * py);
    }

    public PointXY add(int x, int y) {
        return new PointXY(this.x + x, this.y + y);
    }
}
