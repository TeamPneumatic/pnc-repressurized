package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetCondition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetEntityCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionEntity";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget){
        return null;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        List<Entity> entities = getValidEntities(drone.getWorld());
        boolean result = getOperator() == Operator.EQUALS ? entities.size() == getRequiredCount() : entities.size() >= getRequiredCount();
        if(result) {
            drone.addDebugEntry("gui.progWidget.condition.evaluatedTrue");
        } else {
            drone.addDebugEntry("gui.progWidget.condition.evaluatedFalse");
        }
        return ProgWidgetJump.jumpToLabel(drone, allWidgets, this, result);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_ENTITY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetCondition(this, guiProgrammer){
            @Override
            protected boolean isSidedWidget(){
                return false;
            }

            @Override
            protected boolean isUsingAndOr(){
                return false;
            }
        };
    }
}
