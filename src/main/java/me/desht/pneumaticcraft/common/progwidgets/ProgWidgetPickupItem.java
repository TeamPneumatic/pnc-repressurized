package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneEntityAIPickupItems;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetPickupItem extends ProgWidgetAreaItemBase {

    @Override
    public String getWidgetString() {
        return "pickupItem";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_PICK_ITEM;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneEntityAIPickupItems(drone, (ProgWidgetAreaItemBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PINK;
    }
}
