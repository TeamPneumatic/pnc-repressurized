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
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

public class ProgWidgetWait extends ProgWidget {

    public ProgWidgetWait() {
        super(ModProgWidgets.WAIT.get());
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
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.TEXT.get());
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_WAIT;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return widget instanceof ProgWidgetWait ? widget.getConnectedParameters()[0] != null ? new DroneAIWait((ProgWidgetText) widget.getConnectedParameters()[0]) : null : null;
    }

    private static class DroneAIWait extends Goal {

        private final int maxTicks;
        private int ticks;

        private DroneAIWait(ProgWidgetText widget) {
            String time = widget.string;
            int multiplier = 1;
            if (time.endsWith("s") || time.endsWith("S")) {
                multiplier = 20;
                time = time.substring(0, time.length() - 1);
            } else if (time.endsWith("m") || time.endsWith("M")) {
                multiplier = 1200;
                time = time.substring(0, time.length() - 1);
            }
            maxTicks = NumberUtils.toInt(time) * multiplier;
        }

        @Override
        public boolean canUse() {
            return ticks < maxTicks;
        }

        @Override
        public boolean canContinueToUse() {
            ticks++;
            return canUse();
        }

    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
