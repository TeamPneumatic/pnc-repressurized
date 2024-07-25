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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.Objects;

public class ProgWidgetComment extends ProgWidgetText {
    public static final MapCodec<ProgWidgetComment> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(
                    Codec.STRING.fieldOf("string").forGetter(ProgWidgetComment::getString)
            ).apply(builder, ProgWidgetComment::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetComment> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.STRING_UTF8, ProgWidgetComment::getString,
            ProgWidgetComment::new
    );

    public ProgWidgetComment() {
    }

    public ProgWidgetComment(PositionFields positionFields, String string) {
        super(positionFields, string);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.COMMENT.get();
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return List.of();
    }

    @Override
    protected boolean addToTooltip() {
        return false;
    }

    @Override
    public int getHeight() {
        return super.getHeight() + 10;
    }

    @Override
    public int getWidth() {
        return super.getWidth() + 10;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_COMMENT;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return List.of(Component.literal(string));
    }

    @Override
    public boolean freeToUse() {
        return true;
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetComment(positionFields, string);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetComment that = (ProgWidgetComment) o;
        return baseEquals(that) && Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), string);
    }
}
