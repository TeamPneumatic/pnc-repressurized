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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIForEachCoordinate;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgWidgetForEachCoordinate extends ProgWidgetAreaItemBase implements IJumpBackWidget, IJump,
        IVariableSetWidget {
    public static final MapCodec<ProgWidgetForEachCoordinate> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(
                    Codec.STRING.optionalFieldOf("var", "").forGetter(ProgWidgetForEachCoordinate::getVariable)
            ).apply(builder, ProgWidgetForEachCoordinate::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetForEachCoordinate> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.STRING_UTF8, ProgWidgetForEachCoordinate::getVariable,
            ProgWidgetForEachCoordinate::new
    );

    private String elementVariable;
    private final Set<BlockPos> traversedPositions = new HashSet<>();
    private DroneAIForEachCoordinate ai;

    private ProgWidgetForEachCoordinate(PositionFields pos, String elementVariable) {
        super(pos);
        this.elementVariable = elementVariable;
    }

    public ProgWidgetForEachCoordinate() {
        this(PositionFields.DEFAULT, "");
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetForEachCoordinate(getPosition(), elementVariable);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.YELLOW;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_FOR_EACH_COORDINATE;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    public void addVariables(Set<String> variables) {
        super.addVariables(variables);
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
        // In 1.16.5 and earlier, setting variable to (0,0,0) broke out of the loop, which worked most of the time
        // Now in 1.18+, (0,0,0) is even more likely to be a perfectly valid blockpos
        //   So to break out of a loop now, set the variable to any position outside this world's build height
        BlockPos varPos = aiManager.getCoordinate(drone.getOwnerUUID(), elementVariable).orElse(PneumaticCraftUtils.invalidPos());
        if (!locations.isEmpty() && ai != null
                && (traversedPositions.size() == 1 || !drone.getDroneLevel().isOutsideBuildHeight(varPos))) {
            BlockPos pos = ai.getCurCoord();
            if (pos != null) {
                aiManager.setCoordinate(elementVariable, pos);
                return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.getFirst());
            }
        }
        traversedPositions.clear();
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        return widget instanceof ProgWidgetText t ? Collections.singletonList(t.string) : Collections.emptyList();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return ai = new DroneAIForEachCoordinate(drone, (ProgWidgetForEachCoordinate) widget);
    }

    public boolean isValidPosition(BlockPos pos) {
        return traversedPositions.add(pos);
    }

    @Override
    public boolean canBeRunByComputers(IDrone drone, IProgWidget widget) {
        return false;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.FOR_EACH_COORDINATE.get();
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(varAsTextComponent(elementVariable));
    }

    @Override
    public boolean canSetParameter(int index) {
        return index != 2;//Don't use the blacklist side of the jump parameter.
    }
}
