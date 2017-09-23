package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ProgWidgetLightCondition extends ProgWidgetCondition {

    @Override
    public String getWidgetString() {
        return "conditionLight";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_LIGHT;
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {

            @Override
            protected boolean evaluate(BlockPos pos) {
                int lightLevel = drone.world().getLight(pos);
                int requiredLight = ((ICondition) widget).getRequiredCount();
                return ((ICondition) widget).getOperator() == ICondition.Operator.EQUALS ? requiredLight == lightLevel : lightLevel >= requiredLight;
            }

        };
    }
}
