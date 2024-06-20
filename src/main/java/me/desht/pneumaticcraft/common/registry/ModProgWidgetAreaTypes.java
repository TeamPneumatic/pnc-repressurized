package me.desht.pneumaticcraft.common.registry;

import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeSerializer;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModProgWidgetAreaTypes {
    public static final DeferredRegister<AreaTypeSerializer<?>> PROG_WIDGET_AREA_SERIALIZER_DEFERRED
            = DeferredRegister.create(PNCRegistries.AREA_TYPE_SERIALIZER_REGISTRY, Names.MOD_ID);

    public static final Supplier<AreaTypeSerializer<AreaTypeBox>> AREA_TYPE_BOX
            = registerAreaType(AreaTypeBox.ID, AreaTypeBox::new, AreaTypeBox.CODEC, AreaTypeBox.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypeCylinder>> AREA_TYPE_CYLINDER
            = registerAreaType(AreaTypeCylinder.ID, AreaTypeCylinder::new, AreaTypeCylinder.CODEC, AreaTypeCylinder.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypeGrid>> AREA_TYPE_GRID
            = registerAreaType(AreaTypeGrid.ID, AreaTypeGrid::new, AreaTypeGrid.CODEC, AreaTypeGrid.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypeLine>> AREA_TYPE_LINE
            = registerAreaType(AreaTypeLine.ID, AreaTypeLine::instance, AreaTypeLine.CODEC, AreaTypeLine.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypePyramid>> AREA_TYPE_PYRAMID
            = registerAreaType(AreaTypePyramid.ID, AreaTypePyramid::new, AreaTypePyramid.CODEC, AreaTypePyramid.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypeRandom>> AREA_TYPE_RANDOM
            = registerAreaType(AreaTypeRandom.ID, AreaTypeRandom::new, AreaTypeRandom.CODEC, AreaTypeRandom.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypeSphere>> AREA_TYPE_SPHERE
            = registerAreaType(AreaTypeSphere.ID, AreaTypeSphere::new, AreaTypeSphere.CODEC, AreaTypeSphere.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypeTorus>> AREA_TYPE_TORUS
            = registerAreaType(AreaTypeTorus.ID, AreaTypeTorus::new, AreaTypeTorus.CODEC, AreaTypeTorus.STREAM_CODEC);
    public static final Supplier<AreaTypeSerializer<AreaTypeWall>> AREA_TYPE_WALL
            = registerAreaType(AreaTypeWall.ID, AreaTypeWall::new, AreaTypeWall.CODEC, AreaTypeWall.STREAM_CODEC);

    private static <A extends AreaType, T extends AreaTypeSerializer<A>> Supplier<T> registerAreaType(String name, Supplier<A> defaultSupplier, MapCodec<A> codec, StreamCodec<? super RegistryFriendlyByteBuf, A> streamCodec) {
        //noinspection unchecked
        return PROG_WIDGET_AREA_SERIALIZER_DEFERRED.register(name, () -> (T) AreaTypeSerializer.createType(defaultSupplier, codec, streamCodec));
    }
}
