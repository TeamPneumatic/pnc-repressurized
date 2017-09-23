package igwmod;

import igwmod.api.VariableRetrievalEvent;
import net.minecraftforge.common.MinecraftForge;

public class VariableHandler{

    public static String getVariable(String varName){
        VariableRetrievalEvent event = new VariableRetrievalEvent(varName);
        MinecraftForge.EVENT_BUS.post(event);
        return event.replacementValue != null ? event.replacementValue : varName;
    }
}
