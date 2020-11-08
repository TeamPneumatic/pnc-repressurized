package me.desht.pneumaticcraft.common.variables;

import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class TextVariableParser {
    private final String orig;
    private final DroneAIManager variableHolder;
    private final Set<String> relevantVariables = new HashSet<>();

    public TextVariableParser(String str) {
        this(str, null);
    }

    public TextVariableParser(String str, DroneAIManager variableHolder) {
        orig = str;
        this.variableHolder = variableHolder;
    }

    public String parse() {
        int index;
        String ret = orig;
        while ((index = ret.indexOf("${")) >= 0) {
            int secondIndex = ret.indexOf("}", index);
            if (secondIndex >= 0) {
                String varValue = getVariableValue(ret.substring(index + 2, secondIndex));
                ret = ret.substring(0, index) + varValue + ret.substring(secondIndex + 1);
            } else {
                return ret.substring(0, index) + "Parsing error: Missing '}'";
            }
        }
        return ret;
    }

    public Set<String> getRelevantVariables() {
        return relevantVariables;
    }

    private String getVariableValue(String variable) {
        boolean x = variable.endsWith(".x");
        boolean y = variable.endsWith(".y");
        boolean z = variable.endsWith(".z");
        if (x || y || z) variable = variable.substring(0, variable.length() - 2);

        relevantVariables.add(variable);

        if (variableHolder == null) {
            String v1 = variable.startsWith("#") ? variable.substring(1) : variable;
            GlobalVariableManager gvm = GlobalVariableManager.getInstance();
            return gvm.hasItem(v1) ? gvm.getItem(v1).getDisplayName().getString() : posToStr(gvm.getPos(v1), x, y, z);
        } else {
            return variableHolder.hasCoordinate(variable) ?
                    posToStr(variableHolder.getCoordinate(variable), x, y, z) :
                    (variableHolder.hasStack(variable) ? variableHolder.getStack(variable).getDisplayName().getString() : "");
        }
    }

    private String posToStr(BlockPos pos, boolean x, boolean y, boolean z) {
        if (x)
            return Integer.toString(pos.getX());
        else if (y)
            return Integer.toString(pos.getY());
        else if (z)
            return Integer.toString(pos.getZ());
        else
            return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
