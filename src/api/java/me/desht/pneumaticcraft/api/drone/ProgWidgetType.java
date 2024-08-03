/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.drone;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.function.Supplier;

/**
 * Handles serialization of progwidgets, as well as default instance creation.
 */
public class ProgWidgetType<P extends IProgWidget> {
    private final Supplier<P> defaultSupplier;
    private final MapCodec<P> codec;
    private final StreamCodec<RegistryFriendlyByteBuf,P> streamCodec;
    private final Lazy<String> descriptionId = Lazy.of(() -> create().getTranslationKey());

    private ProgWidgetType(Supplier<P> defaultSupplier, MapCodec<P> codec, StreamCodec<RegistryFriendlyByteBuf,P> streamCodec) {
        this.defaultSupplier = defaultSupplier;
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public static <P extends IProgWidget> ProgWidgetType<P> createType(Supplier<P> factory, MapCodec<P> codec, StreamCodec<RegistryFriendlyByteBuf,P> streamCodec) {
        return new ProgWidgetType<>(factory, codec, streamCodec);
    }

    public P create() {
        return defaultSupplier.get();
    }

    public String getTranslationKey() {
        return descriptionId.get();
    }

    public P cast(IProgWidget widget) {
        //noinspection unchecked
        return (P) widget;
    }

    public MapCodec<? extends IProgWidget> codec() {
        return codec;
    }

    public StreamCodec<RegistryFriendlyByteBuf, ? extends IProgWidget> streamCodec() {
        return streamCodec;
    }
}
