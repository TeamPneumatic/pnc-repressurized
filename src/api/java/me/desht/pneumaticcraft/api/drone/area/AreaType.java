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

package me.desht.pneumaticcraft.api.drone.area;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.function.Consumer;

public abstract class AreaType {
    public static final Codec<AreaType> CODEC = PNCRegistries.AREA_TYPE_SERIALIZER_REGISTRY.byNameCodec().dispatch(
            AreaType::getSerializer,
            AreaTypeSerializer::codec
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,AreaType> STREAM_CODEC
            = ByteBufCodecs.registry(PNCRegistries.AREA_TYPE_SERIALIZER_KEY)
            .dispatch(AreaType::getSerializer, AreaTypeSerializer::streamCodec);

    private final String translationKey;
    private final String name;

    public AreaType(String name) {
        this.name = name;
        this.translationKey = String.format("pneumaticcraft.gui.progWidget.area.type.%s.name", name);
    }

    public abstract AreaTypeSerializer<? extends AreaType> getSerializer();

    /**
     * Implement this to return a copy of this area type, ensuring any mutable fields of the object are copied!
     *
     * @return a copy of this area type
     */
    public abstract AreaType copy();

    public String getName() {
        return name;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * Actually add the positions defined by this area to the given blockpos set
     *
     * @param areaAdder the adder; call {@code adder.accept(pos)} to add a blockpos
     * @param p1 the first raw blockpos of the area
     * @param p2 the second raw blockpos of the area
     * @param minX min X coord
     * @param minY min Y coord
     * @param minZ min Z coord
     * @param maxX max X coord
     * @param maxY max Y coord
     * @param maxZ max Z coord
     */
    public abstract void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    /**
     * Called when loading old-style programs from pastebin etc.  Convert any old-fashioned area representations to
     * their modern equivalents.
     *
     * @param oldType the old-style area type
     * @param typeInfo extra integer type information used by some types
     */
    public void convertFromLegacy(EnumOldAreaType oldType, int typeInfo) {
    }

    /**
     * Returns whether the area added in addArea is deterministic (used to determine if positions can be cached).
     * @return true if deterministic, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDeterministic(){
        return true;
    }

    public void addUIWidgets(List<AreaTypeWidget> widgets) {
    }

    public enum AreaAxis implements ITranslatableEnum, StringRepresentable {
        X("x"), Y("y"), Z("z");

        private final String name;

        AreaAxis(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.area.type.axis." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
