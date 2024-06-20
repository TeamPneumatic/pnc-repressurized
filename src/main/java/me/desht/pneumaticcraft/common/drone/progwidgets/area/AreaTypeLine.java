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
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeSerializer;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetAreaTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class AreaTypeLine extends AreaType {
    public static final MapCodec<AreaTypeLine> CODEC = MapCodec.unit(AreaTypeLine.INSTANCE);
    public static final StreamCodec<FriendlyByteBuf, AreaTypeLine> STREAM_CODEC = StreamCodec.unit(AreaTypeLine.INSTANCE);

    public static final String ID = "line";

    private static final AreaTypeLine INSTANCE = new AreaTypeLine();

    public static AreaTypeLine instance() {
        return INSTANCE;
    }

    private AreaTypeLine() {
        super(ID);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_LINE.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        Vec3 lineVec = new Vec3(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ()).normalize().scale(0.1);
        double curX = p1.getX() + 0.5;
        double curY = p1.getY() + 0.5;
        double curZ = p1.getZ() + 0.5;
        double totalDistance = 0;
        double maxDistance = PneumaticCraftUtils.distBetween(p1, p2);
        while (totalDistance <= maxDistance) {
            totalDistance += 0.1;
            curX += lineVec.x;
            curY += lineVec.y;
            curZ += lineVec.z;
            areaAdder.accept(BlockPos.containing(curX, curY, curZ));
        }
    }
}
