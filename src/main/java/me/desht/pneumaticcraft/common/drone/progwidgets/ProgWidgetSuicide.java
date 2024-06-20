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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.DroneSuicideEvent;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.List;

public class ProgWidgetSuicide extends ProgWidget {
    public static final MapCodec<ProgWidgetSuicide> CODEC = RecordCodecBuilder.mapCodec(builder ->
        baseParts(builder).apply(builder, ProgWidgetSuicide::new));

    private ProgWidgetSuicide(PositionFields pos) {
        super(pos);
    }

    public ProgWidgetSuicide() {
        super(PositionFields.DEFAULT);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.SUICIDE.get();
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public boolean hasStepOutput() {
        return false;
    }

    @Override
    public int getWidth() {
        return 40;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIME;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_SUICIDE;
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAISuicide(drone);
    }

    private static class DroneAISuicide extends Goal {
        private final IDrone drone;

        DroneAISuicide(IDrone drone) {
            this.drone = drone;
        }

        @Override
        public boolean canUse() {
            NeoForge.EVENT_BUS.post(new DroneSuicideEvent(drone));
            drone.overload("suicide");
            return false;
        }
    }
}
