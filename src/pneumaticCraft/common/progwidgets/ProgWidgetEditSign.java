package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneAIEditSign;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.remote.TextVariableParser;
import pneumaticCraft.lib.Textures;

public class ProgWidgetEditSign extends ProgWidgetAreaItemBase implements ISignEditWidget{

    @Override
    public String getWidgetString(){
        return "editSign";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_EDIT_SIGN;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAIEditSign(drone, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.ENDER_PLANT_DAMAGE;
    }

    @Override
    public String[] getLines(){
        List<String> lines = new ArrayList<String>();
        ProgWidgetString textWidget = (ProgWidgetString)getConnectedParameters()[1];
        while(textWidget != null) {
            lines.add(new TextVariableParser(textWidget.string, aiManager).parse());
            textWidget = (ProgWidgetString)textWidget.getConnectedParameters()[0];
        }
        return lines.toArray(new String[lines.size()]);
    }

    @Override
    public boolean canSetParameter(int index){
        return index != 3;
    }

}
