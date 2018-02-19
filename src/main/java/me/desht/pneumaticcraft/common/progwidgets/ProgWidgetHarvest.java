package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIHarvest;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetHarvest extends ProgWidgetDigAndPlace {

    public ProgWidgetHarvest() {
        super(ProgWidgetDigAndPlace.EnumOrder.CLOSEST);
    }

    @Override
    public String getWidgetString() {
        return "harvest";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_HARVEST;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIHarvest(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.BROWN;
    }

}
