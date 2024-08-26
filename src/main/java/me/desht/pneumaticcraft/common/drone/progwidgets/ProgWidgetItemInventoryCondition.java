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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgWidgetItemInventoryCondition extends ProgWidgetCondition {
    public static final MapCodec<ProgWidgetItemInventoryCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            condParts(builder).apply(builder, ProgWidgetItemInventoryCondition::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetItemInventoryCondition> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            InvBaseFields.STREAM_CODEC, ProgWidgetInventoryBase::invBaseFields,
            ConditionFields.STREAM_CODEC, ProgWidgetCondition::conditionFields,
            ProgWidgetItemInventoryCondition::new
    );

    public ProgWidgetItemInventoryCondition() {
    }

    public ProgWidgetItemInventoryCondition(PositionFields pos, InvBaseFields inv, ConditionFields cond) {
        super(pos, inv, cond);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetItemInventoryCondition(getPosition(), invBaseFields().copy(), conditionFields());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.ITEM_FILTER.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDrone drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                BlockEntity te = drone.getDroneLevel().getBlockEntity(pos);

                // item handlers won't typically override hashCode/equals, but this should be OK: we just
                // want a set of distinct item handler objects, which Object#hashCode() should give us
                Set<IItemHandler> handlers = new HashSet<>();
                for (Direction dir : DirectionUtil.VALUES) {
                    if (((ISidedWidget) progWidget).isSideSelected(dir)) {
                        IOHelper.getInventoryForBlock(te, dir).ifPresent(handlers::add);
                    }
                }

                int count = 0;
                for (IItemHandler handler : handlers) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (progWidget.isItemValidForFilters(stack)) {
                            count += stack.getCount();
                        }
                    }
                }
                maybeRecordMeasuredVal(drone, count);
                return ((ICondition) progWidget).getOperator().evaluate(count, ((ICondition) progWidget).getRequiredCount());
            }

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ITEM_INVENTORY;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CONDITION_ITEM_INVENTORY.get();
    }
}
