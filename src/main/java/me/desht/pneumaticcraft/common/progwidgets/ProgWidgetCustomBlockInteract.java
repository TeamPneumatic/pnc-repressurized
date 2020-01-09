package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAICustomBlockInteract;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetCustomBlockInteract extends ProgWidgetInventoryBase {

    private ICustomBlockInteract interactor;

    public ProgWidgetCustomBlockInteract() {
        super(ModProgWidgets.CUSTOM_BLOCK_INTERACT);
    }

    public ProgWidgetCustomBlockInteract setInteractor(ICustomBlockInteract interactor) {
        this.interactor = interactor;
        return this;
    }

    @Override
    public IProgWidget copy() {
        ProgWidgetCustomBlockInteract widget = (ProgWidgetCustomBlockInteract) super.copy();
        widget.setInteractor(interactor);
        return widget;
    }

    @Override
    public ResourceLocation getTexture() {
        return interactor.getTexture();
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICustomBlockInteract(drone, (ProgWidgetInventoryBase) widget, interactor);
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA);
    }

    @Override
    public DyeColor getColor() {
        return interactor.getColor();
    }

}
