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

import java.util.function.Supplier;

/**
 * Represents the type of a progwidget. You do not need to use this directly.
 */
public class ProgWidgetType<P extends IProgWidget> {
    private final Supplier<? extends P> defaultSupplier;
    private final MapCodec<? extends IProgWidget> codec;
    private String descriptionId;

    private ProgWidgetType(Supplier<P> defaultSupplier, MapCodec<P> codec) {
        this.defaultSupplier = defaultSupplier;
        this.codec = codec;
    }

    public static <P extends IProgWidget> ProgWidgetType<P> createType(Supplier<P> factory, MapCodec<P> codec) {
        return new ProgWidgetType<>(factory, codec);
    }

    public P create() {
        return defaultSupplier.get();
    }

    public String getTranslationKey() {
        if (this.descriptionId == null) {
            this.descriptionId = create().getTranslationKey();
        }
        return this.descriptionId;
    }

    public P cast(IProgWidget widget) {
        //noinspection unchecked
        return (P) widget;
    }

    public MapCodec<? extends IProgWidget> codec() {
        return codec;
    }
}
