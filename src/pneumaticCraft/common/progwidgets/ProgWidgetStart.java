package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetStart extends ProgWidget{

    @Override
    public boolean hasStepInput(){
        return false;
    }

    @Override
    public boolean hasStepOutput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return null;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_START;
    }

    @Override
    public String getWidgetString(){
        return "start";
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.REPULSION_PLANT_DAMAGE;
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        for(IProgWidget widget : widgets) {
            if(widget != this && widget instanceof ProgWidgetStart) {
                curInfo.add("gui.progWidget.general.error.multipleStartPieces");
                break;
            }
        }
    }
}
