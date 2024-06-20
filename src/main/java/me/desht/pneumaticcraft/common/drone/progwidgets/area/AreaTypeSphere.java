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
import me.desht.pneumaticcraft.common.registry.ModProgWidgetAreaTypes;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.function.Consumer;

public class AreaTypeSphere extends AreaType {
    public static final MapCodec<AreaTypeSphere> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        StringRepresentable.fromEnum(SphereType::values).optionalFieldOf("sphere_type", SphereType.FILLED).forGetter(t -> t.sphereType)
    ).apply(builder, AreaTypeSphere::new));
    public static final StreamCodec<FriendlyByteBuf, AreaTypeSphere> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(SphereType.class), t -> t.sphereType,
            AreaTypeSphere::new
    );

    public static final String ID = "sphere";
    
    private SphereType sphereType;
    
    public AreaTypeSphere(){
        this(SphereType.FILLED);
    }

    public AreaTypeSphere(SphereType sphereType) {
        super(ID);
        this.sphereType = sphereType;
    }

    @Override
    public String toString() {
        return getName() + "/" + sphereType;
    }

    @Override
    public AreaTypeSerializer<? extends AreaType> getSerializer() {
        return ModProgWidgetAreaTypes.AREA_TYPE_SPHERE.get();
    }

    @Override
    public void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        double radius = PneumaticCraftUtils.distBetween(p1, p2);
        double radiusSq = radius * radius;
        double innerRadius = sphereType == SphereType.HOLLOW ? radius - 1 : 0;
        double innerRadiusSq = innerRadius * innerRadius;
        minX = (int) (p1.getX() - radius - 1);
        minY = (int) (p1.getY() - radius - 1);
        minZ = (int) (p1.getZ() - radius - 1);
        maxX = (int) (p1.getX() + radius + 1);
        maxY = (int) (p1.getY() + radius + 1);
        maxZ = (int) (p1.getZ() + radius + 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double centerDistSq = PneumaticCraftUtils.distBetweenSq(p1, x + 0.5, y + 0.5, z + 0.5);
                    if (centerDistSq <= radiusSq && centerDistSq >= innerRadiusSq) { //Only add blocks between a certain radius
                        areaAdder.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }
    
    @Override
    public void addUIWidgets(List<AreaTypeWidget> widgets){
        super.addUIWidgets(widgets);
        widgets.add(new AreaTypeWidget.EnumSelectorField<>("pneumaticcraft.gui.progWidget.area.type.sphere.sphereType", SphereType.class, () -> sphereType, sphereType -> this.sphereType = sphereType));
    }
    
//    @Override
//    public void writeToNBT(CompoundTag tag){
//        super.writeToNBT(tag);
//        tag.putByte("sphereType", (byte)sphereType.ordinal());
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag){
//        super.readFromNBT(tag);
//        sphereType = SphereType.values()[tag.getByte("sphereType")];
//    }
//
//    @Override
//    public void writeToPacket(FriendlyByteBuf buffer) {
//        super.writeToPacket(buffer);
//        buffer.writeEnum(sphereType);
//    }
//
//    @Override
//    public void readFromPacket(FriendlyByteBuf buf) {
//        super.readFromPacket(buf);
//        sphereType = buf.readEnum(SphereType.class);
//    }

    private enum SphereType implements ITranslatableEnum, StringRepresentable {
        FILLED("filled"), HOLLOW("hollow");

        private final String name;

        SphereType(String name) {
            this.name = "pneumaticcraft.gui.progWidget.area.type.sphere.sphereType." + name;
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
