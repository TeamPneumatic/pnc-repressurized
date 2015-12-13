package pneumaticCraft.common.remote;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIManager;

public class TextVariableParser{
    private final String orig;
    private final DroneAIManager variableHolder;
    private final Set<String> relevantVariables = new HashSet<String>();

    public TextVariableParser(String str){
        this(str, null);
    }

    public TextVariableParser(String str, DroneAIManager variableHolder){
        orig = str;
        this.variableHolder = variableHolder;
    }

    public String parse(){
        int index;
        String ret = orig;
        while((index = ret.indexOf("${")) >= 0) {
            int secondIndex = ret.indexOf("}", index);
            if(secondIndex >= 0) {
                String varValue = getVariableValue(ret.substring(index + 2, secondIndex));
                ret = ret.substring(0, index) + varValue + ret.substring(secondIndex + 1);
            } else {
                return ret.substring(0, index) + "Parsing error: Missing '}'";
            }
        }
        return ret;
    }

    public Set<String> getRelevantVariables(){
        return relevantVariables;
    }

    private String getVariableValue(String variable){
        boolean x = variable.endsWith(".x");
        boolean y = variable.endsWith(".y");
        boolean z = variable.endsWith(".z");
        if(x || y || z) variable = variable.substring(0, variable.length() - 2);
        relevantVariables.add(variable);
        ChunkPosition pos = variableHolder != null ? variableHolder.getCoordinate(variable) : GlobalVariableManager.getInstance().getPos(variable.startsWith("#") ? variable.substring(1) : variable);
        if(x) return pos.chunkPosX + "";
        if(y) return pos.chunkPosY + "";
        if(z) return pos.chunkPosZ + "";
        return pos.chunkPosX + ", " + pos.chunkPosY + ", " + pos.chunkPosZ;
    }
}
