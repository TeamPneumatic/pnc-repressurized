package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAILogistics;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetLogistics extends ProgWidgetAreaItemBase {

    @Override
    public String getWidgetString() {
        return "logistics";
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LOGISTICS;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAILogistics(drone, (ProgWidgetAreaItemBase) widget);
    }

}
