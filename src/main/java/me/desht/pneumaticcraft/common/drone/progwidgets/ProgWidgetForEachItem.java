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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class ProgWidgetForEachItem extends ProgWidget implements IJumpBackWidget, IJump, IVariableSetWidget {
    public static final MapCodec<ProgWidgetForEachItem> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(
                    Codec.STRING.optionalFieldOf("var", "").forGetter(ProgWidgetForEachItem::getVariable)
            ).apply(builder, ProgWidgetForEachItem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetForEachItem> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.STRING_UTF8, ProgWidgetForEachItem::getVariable,
            ProgWidgetForEachItem::new
    );

    private String elementVariable;
    private int curIndex; //iterator index
    private DroneAIManager aiManager;

    private ProgWidgetForEachItem(PositionFields pos, String elementVariable) {
        super(pos);
        this.elementVariable = elementVariable;
    }

    public ProgWidgetForEachItem() {
        this(PositionFields.DEFAULT, "");
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetForEachItem(getPosition(), elementVariable);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_FOR_EACH_ITEM;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.ITEM_FILTER.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(elementVariable);
    }

    @Override
    public String getVariable() {
        return elementVariable;
    }

    @Override
    public void setVariable(String variable) {
        elementVariable = variable;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        List<String> locations = getPossibleJumpLocations();
        ItemStack filter = getFilterForIndex(curIndex++);
        if (!locations.isEmpty() && !filter.isEmpty() && (curIndex == 1 || !aiManager.getStack(drone.getOwnerUUID(), elementVariable).isEmpty())) {
            aiManager.setItemStack(elementVariable, filter);
            return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.getFirst());
        }
        curIndex = 0;
        return super.getOutputWidget(drone, allWidgets);
    }

    @Nonnull
    private ItemStack getFilterForIndex(int index) {
        ProgWidgetItemFilter widget = (ProgWidgetItemFilter) getConnectedParameters()[0];
        for (int i = 0; i < index; i++) {
            if (widget == null) return ItemStack.EMPTY;
            widget = (ProgWidgetItemFilter) widget.getConnectedParameters()[0];
        }
        return widget != null ? widget.getFilter() : ItemStack.EMPTY;
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        ProgWidgetText textWidget = widget != null ? (ProgWidgetText) widget : null;
        List<String> locations = new ArrayList<>();
        if (textWidget != null) locations.add(textWidget.string);
        return locations;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.FOR_EACH_ITEM.get();
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(varAsTextComponent(elementVariable));
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProgWidgetForEachItem that = (ProgWidgetForEachItem) o;
        return baseEquals(that) && Objects.equals(elementVariable, that.elementVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), elementVariable);
    }
}
