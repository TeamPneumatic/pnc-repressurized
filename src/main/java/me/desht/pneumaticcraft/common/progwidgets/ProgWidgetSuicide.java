package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.DroneSuicideEvent;
import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft.DamageSourceDroneOverload;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class ProgWidgetSuicide extends ProgWidget {

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
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return null;
    }

    @Override
    public String getWidgetString() {
        return "suicide";
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
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAISuicide((EntityDrone) drone);
    }

    private static class DroneAISuicide extends Goal {
        private final EntityDrone drone;

        DroneAISuicide(EntityDrone drone) {
            this.drone = drone;
        }

        @Override
        public boolean shouldExecute() {
            MinecraftForge.EVENT_BUS.post(new DroneSuicideEvent(drone));
            drone.attackEntityFrom(new DamageSourceDroneOverload("suicide"), 2000.0F);
            return false;
        }
    }
}
