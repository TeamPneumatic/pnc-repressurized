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
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ai.DroneAILiquidImport;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLiquidImport extends ProgWidgetInventoryBase implements ILiquidFiltered, IBlockOrdered {
    public static final MapCodec<ProgWidgetLiquidImport> CODEC = RecordCodecBuilder.mapCodec(builder ->
        invParts(builder).and(builder.group(
                StringRepresentable.fromEnum(Ordering::values).optionalFieldOf("order", Ordering.HIGH_TO_LOW).forGetter(ProgWidgetLiquidImport::getOrder),
                Codec.BOOL.optionalFieldOf("void_excess", false).forGetter(ProgWidgetLiquidImport::shouldVoidExcess)
        )
    ).apply(builder, ProgWidgetLiquidImport::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetLiquidImport> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            InvBaseFields.STREAM_CODEC, ProgWidgetInventoryBase::invBaseFields,
            NeoForgeStreamCodecs.enumCodec(Ordering.class), ProgWidgetLiquidImport::getOrder,
            ByteBufCodecs.BOOL, ProgWidgetLiquidImport::shouldVoidExcess,
            ProgWidgetLiquidImport::new
    );

    private Ordering order;
    private boolean voidExcess;

    private ProgWidgetLiquidImport(PositionFields pos, InvBaseFields invBaseFields, Ordering order, boolean voidExcess) {
        super(pos, invBaseFields);
        this.order = order;
        this.voidExcess = voidExcess;
    }

    public ProgWidgetLiquidImport() {
        this(PositionFields.DEFAULT, InvBaseFields.DEFAULT, Ordering.HIGH_TO_LOW, false);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetLiquidImport(getPosition(), invBaseFields().copy(), order, voidExcess);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LIQUID_IM;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.LIQUID_FILTER.get());
    }

    @Override
    public boolean isFluidValid(Fluid fluid) {
        return ProgWidgetLiquidFilter.isLiquidValid(fluid, this, 1);
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAILiquidImport(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BLUE;
    }

    @Override
    public Ordering getOrder() {
        return order;
    }

    @Override
    public void setOrder(Ordering order) {
        this.order = order;
    }

    public boolean shouldVoidExcess() {
        return voidExcess;
    }

    public void setVoidExcess(boolean voidExcess) {
        this.voidExcess = voidExcess;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.LIQUID_IMPORT.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        curTooltip.add(xlate("pneumaticcraft.message.misc.order", xlate(order.getTranslationKey())));
        if (shouldVoidExcess()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.liquidImport.voidExcess"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProgWidgetLiquidImport that = (ProgWidgetLiquidImport) o;
        return baseEquals(that) && voidExcess == that.voidExcess && order == that.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), order, voidExcess);
    }
}
