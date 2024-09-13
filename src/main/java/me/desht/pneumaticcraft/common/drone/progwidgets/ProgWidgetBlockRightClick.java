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
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIRightClickBlock;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.Objects;

public class ProgWidgetBlockRightClick extends ProgWidgetPlace implements IBlockRightClicker, ISidedWidget {

    public static final MapCodec<ProgWidgetBlockRightClick> CODEC = RecordCodecBuilder.mapCodec(builder ->
            digPlaceParts(builder).and(builder.group(
                    Direction.CODEC.optionalFieldOf("side", Direction.UP).forGetter(ProgWidgetBlockRightClick::getClickSide),
                    Codec.BOOL.optionalFieldOf("sneaking", false).forGetter(ProgWidgetBlockRightClick::isSneaking),
                    StringRepresentable.fromEnum(RightClickType::values).optionalFieldOf("click_type", RightClickType.CLICK_ITEM).forGetter(ProgWidgetBlockRightClick::getClickType)
            )
    ).apply(builder, ProgWidgetBlockRightClick::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetBlockRightClick> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            DigPlaceFields.STREAM_CODEC, p -> p.digPlaceFields,
            Direction.STREAM_CODEC, ProgWidgetBlockRightClick::getClickSide,
            ByteBufCodecs.BOOL, ProgWidgetBlockRightClick::isSneaking,
            NeoForgeStreamCodecs.enumCodec(RightClickType.class), ProgWidgetBlockRightClick::getClickType,
            ProgWidgetBlockRightClick::new
    );

    private Direction clickSide = Direction.UP;
    private boolean sneaking;
    private RightClickType clickType = RightClickType.CLICK_ITEM;

    public ProgWidgetBlockRightClick(PositionFields pos, DigPlaceFields digPlaceFields, Direction clickSide, boolean sneaking, RightClickType clickType) {
        super(pos, digPlaceFields);
        this.clickSide = clickSide;
        this.sneaking = sneaking;
        this.clickType = clickType;
    }

    public ProgWidgetBlockRightClick() {
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_BLOCK_RIGHT_CLICK;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.BLOCK_RIGHT_CLICK.get();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIRightClickBlock(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public boolean supportsMaxActions() {
        return false;
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public RightClickType getClickType() {
        return clickType;
    }

    public void setClickType(RightClickType clickType) {
        this.clickType = clickType;
    }

    public Direction getClickSide() {
        return clickSide;
    }

    public void setClickSide(Direction clickSide) {
        this.clickSide = clickSide;
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.blockRightClick.clickSide")
                .append(": " + ClientUtils.translateDirection(clickSide)));
        if (sneaking) {
            curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.blockRightClick.sneaking"));
        }
        curTooltip.add(Component.translatable("pneumaticcraft.gui.progWidget.blockRightClick.operation")
                .append(": ")
                .append(Component.translatable(clickType.getTranslationKey())));
    }

    @Override
    public void setSides(boolean[] sides) {
        clickSide = ISidedWidget.getDirForSides(sides);
    }

    @Override
    public boolean[] getSides() {
        return ISidedWidget.getSidesFromDir(clickSide);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetBlockRightClick(getPosition(), digPlaceFields, clickSide, sneaking, clickType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetBlockRightClick that = (ProgWidgetBlockRightClick) o;
        return baseEquals(that) && sneaking == that.sneaking && clickSide == that.clickSide && clickType == that.clickType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), clickSide, sneaking, clickType);
    }
}
