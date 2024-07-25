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
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.item.GPSToolItem;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinate extends ProgWidget implements IVariableWidget {
    public static final MapCodec<ProgWidgetCoordinate> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(builder.group(
                    BlockPos.CODEC.optionalFieldOf("coord", BlockPos.ZERO).forGetter(p -> p.coord),
                    Codec.STRING.optionalFieldOf("var", "").forGetter(ProgWidgetCoordinate::getVariable),
                    Codec.BOOL.optionalFieldOf("using_var", false).forGetter(ProgWidgetCoordinate::isUsingVariable)
            )).apply(builder, ProgWidgetCoordinate::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetCoordinate> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            BlockPos.STREAM_CODEC, p -> p.coord,
            ByteBufCodecs.STRING_UTF8, ProgWidgetCoordinate::getVariable,
            ByteBufCodecs.BOOL, ProgWidgetCoordinate::isUsingVariable,
            ProgWidgetCoordinate::new
    );

    private BlockPos coord = BlockPos.ZERO;
    private String variable = "";
    private boolean useVariable;
    private DroneAIManager aiManager;

    public ProgWidgetCoordinate() {
        super(PositionFields.DEFAULT);
    }

    private ProgWidgetCoordinate(PositionFields pos, BlockPos coord, String variable, boolean useVariable) {
        super(pos);

        this.coord = coord;
        this.variable = variable;
        this.useVariable = useVariable;
    }

    public static ProgWidgetCoordinate fromPos(BlockPos pos) {
        ProgWidgetCoordinate w = new ProgWidgetCoordinate();
        w.setCoordinate(pos);
        return w;
    }

    public static ProgWidgetCoordinate fromGPSTool(ItemStack gpsTool) {
        ProgWidgetCoordinate w = new ProgWidgetCoordinate();
        w.loadFromGPSTool(gpsTool);
        return w;
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetCoordinate(getPosition(), coord, variable, useVariable);
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgetTypes.COORDINATE.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.COORDINATE.get());
    }

    @Override
    public void addWarnings(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addWarnings(curInfo, widgets);
        if (!useVariable && coord == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.coordinate.warning.noCoordinate"));
        }
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (useVariable && variable.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.general.error.emptyVariable"));
        }
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GREEN;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_COORDINATE;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    public Optional<BlockPos> getCoordinate() {
        if (useVariable && aiManager != null) {
            return aiManager.getCoordinate(aiManager.getDrone().getOwnerUUID(), variable);
        } else {
            return getRawCoordinate();
        }
    }

    public Optional<BlockPos> getRawCoordinate() {
        return Optional.ofNullable(coord);
    }

    public void setCoordinate(BlockPos pos) {
        coord = pos;
    }

    public void setVariable(String varName) {
        variable = varName;
    }

    public String getVariable() {
        return variable;
    }

    public boolean isUsingVariable() {
        return useVariable;
    }

    public void setUsingVariable(boolean useVariable) {
        this.useVariable = useVariable;
    }
    
    public void loadFromGPSTool(ItemStack gpsTool){
        String variable = GPSToolItem.getVariable(gpsTool);
        if (variable.isEmpty()) {
            setCoordinate(GPSToolItem.getGPSLocation(gpsTool).orElse(BlockPos.ZERO));
            setUsingVariable(false);
        } else {
            setVariable(variable);
            setUsingVariable(true);
        }
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.COORDINATE.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        if (useVariable) {
            curTooltip.add(Component.literal("XYZ: var '" + variable + "'"));
        } else {
            curTooltip.add(Component.literal(PneumaticCraftUtils.posToString(coord)));
        }
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return useVariable ?
                Collections.singletonList(varAsTextComponent(variable)) :
                Collections.singletonList(Component.literal(PneumaticCraftUtils.posToString(coord)));
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgWidgetCoordinate that = (ProgWidgetCoordinate) o;
        return baseEquals(that) && useVariable == that.useVariable && Objects.equals(coord, that.coord) && Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), coord, variable, useVariable);
    }
}
