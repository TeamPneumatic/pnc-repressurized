package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.remote.TextVariableParser;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetRename extends ProgWidget implements IRenamingWidget, IVariableWidget {
    private DroneAIManager aiManager;

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
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "rename";
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_RENAME;
    }

    @Override
    public Goal getWidgetAI(final IDroneBase drone, final IProgWidget widget) {
        return new DroneAIRename(drone, (IRenamingWidget) widget);
    }

    private class DroneAIRename extends Goal {
        private final IDroneBase drone;
        private final IRenamingWidget widget;

        DroneAIRename(IDroneBase drone, IRenamingWidget widget) {
            this.drone = drone;
            this.widget = widget;
        }

        @Override
        public boolean shouldExecute() {
            drone.setName(widget.getNewName() != null ? new StringTextComponent(widget.getNewName()) : xlate("entity.PneumaticCraft.Drone.name"));
            return false;
        }

    }

    @Override
    public String getNewName() {
        return getConnectedParameters()[0] != null ? new TextVariableParser(((ProgWidgetString) getConnectedParameters()[0]).string, aiManager).parse() : null;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public void addVariables(Set<String> variables) {
    }

}
