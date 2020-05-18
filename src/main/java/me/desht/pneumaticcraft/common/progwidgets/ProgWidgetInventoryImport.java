package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneEntityAIInventoryImport;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetInventoryImport extends ProgWidgetInventoryBase {

    public ProgWidgetInventoryImport() {
        super(ModProgWidgets.INVENTORY_IMPORT);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_INV_IM;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneEntityAIInventoryImport(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BLUE;
    }
}
