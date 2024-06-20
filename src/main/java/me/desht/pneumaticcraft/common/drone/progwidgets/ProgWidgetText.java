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

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.List;

public class ProgWidgetText extends ProgWidget {
    public static final MapCodec<ProgWidgetText> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(
                    Codec.STRING.fieldOf("string").forGetter(ProgWidgetText::getString)
            ).apply(builder, ProgWidgetText::new));

    protected String string = "";

    public ProgWidgetText() {
        super(PositionFields.DEFAULT);
    }

    protected ProgWidgetText(PositionFields positionFields, String string) {
        super(positionFields);

        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public static ProgWidgetText withText(String string){
        ProgWidgetText widget = new ProgWidgetText();
        widget.string = string;
        return widget;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.TEXT.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (addToTooltip()) {
            curTooltip.addAll(getExtraStringInfo());
        }
    }

    protected boolean addToTooltip() {
        return true;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(varAsTextComponent(string));
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgetTypes.TEXT.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_TEXT;
    }

//    @Override
//    public void writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.writeToNBT(tag, provider);
//        tag.putString("string", string);
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.readFromNBT(tag, provider);
//        string = tag.getString("string");
//    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeUtf(string);
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        super.readFromPacket(buf);
        string = buf.readUtf(32768);

    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIGHT_BLUE;
    }
}
