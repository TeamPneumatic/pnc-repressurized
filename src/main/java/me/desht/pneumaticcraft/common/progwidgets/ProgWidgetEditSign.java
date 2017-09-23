package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIEditSign;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.remote.TextVariableParser;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ProgWidgetEditSign extends ProgWidgetAreaItemBase implements ISignEditWidget {

    @Override
    public String getWidgetString() {
        return "editSign";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_EDIT_SIGN;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIEditSign(drone, (ProgWidgetAreaItemBase) widget);
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.PURPLE;
    }

    @Override
    public String[] getLines() {
        List<String> lines = new ArrayList<String>();
        ProgWidgetString textWidget = (ProgWidgetString) getConnectedParameters()[1];
        while (textWidget != null) {
            lines.add(new TextVariableParser(textWidget.string, aiManager).parse());
            textWidget = (ProgWidgetString) textWidget.getConnectedParameters()[0];
        }
        return lines.toArray(new String[lines.size()]);
    }

    @Override
    public boolean canSetParameter(int index) {
        return index != 3;
    }

}
