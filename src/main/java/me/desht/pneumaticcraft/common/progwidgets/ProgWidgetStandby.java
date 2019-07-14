package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetStandby extends ProgWidget {
    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return null;
    }

    @Override
    public String getWidgetString() {
        return "standby";
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
