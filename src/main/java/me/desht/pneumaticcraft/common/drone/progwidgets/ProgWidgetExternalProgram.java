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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIExternalProgram;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public class ProgWidgetExternalProgram extends ProgWidgetAreaItemBase {
    public static final MapCodec<ProgWidgetExternalProgram> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(
                    Codec.BOOL.optionalFieldOf("share_variables", false).forGetter(ProgWidgetExternalProgram::isShareVariables)
            ).apply(builder, ProgWidgetExternalProgram::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetExternalProgram> STREAM_CODEC = StreamCodec.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ByteBufCodecs.BOOL, ProgWidgetExternalProgram::isShareVariables,
            ProgWidgetExternalProgram::new
    );

    public boolean shareVariables;

    private ProgWidgetExternalProgram(PositionFields pos, boolean shareVariables) {
        super(pos);

        this.shareVariables = shareVariables;
    }

    public ProgWidgetExternalProgram() {
        this(PositionFields.DEFAULT, false);
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetExternalProgram(getPosition(), shareVariables);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_EXTERNAL_PROGRAM;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.EXTERNAL_PROGRAM.get();
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAIExternalProgram(drone, aiManager, (ProgWidgetExternalProgram) widget);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get());
    }

    public boolean isShareVariables() {
        return shareVariables;
    }

    @Override
    public boolean canBeRunByComputers(IDrone drone, IProgWidget widget) {
        return false;
    }
}
