package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class ProgWidgetStandby extends ProgWidget {
    public ProgWidgetStandby() {
        super(ModProgWidgets.STANDBY);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType> getParameters() {
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
        return Textures.PROG_WIDGET_STANDBY;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIStandby((EntityDrone) drone);
    }

    public static class DroneAIStandby extends Goal {
        private final EntityDrone drone;

        DroneAIStandby(EntityDrone drone) {
            this.drone = drone;
        }

        @Override
        public boolean shouldExecute() {
            drone.setStandby(true);
            return false;
        }
    }
}
