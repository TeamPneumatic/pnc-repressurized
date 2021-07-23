package me.desht.pneumaticcraft.common.variables;

import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TextVariableParser {
    private final String orig;
    private final DroneAIManager variableHolder;
    private final Set<String> relevantVariables = new HashSet<>();
    private final UUID playerID;

    public TextVariableParser(String str, UUID playerID) {
        this.orig = str;
        this.variableHolder = null;
        this.playerID = playerID;
    }

    public TextVariableParser(String str, DroneAIManager variableHolder) {
        this.orig = str;
        this.variableHolder = variableHolder;
        this.playerID = variableHolder.getDrone().getOwnerUUID();
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
        String[] f = StringUtils.splitByWholeSeparator(variable, ".");
        String ext = "";
        if (f.length > 1) {
            ext = f[f.length - 1];
            variable = f.length == 2 ? f[0] : String.join(".", Arrays.copyOf(f, f.length - 1));
        }

        relevantVariables.add(variable);

        if (variableHolder == null) {
            BlockPos pos = GlobalVariableHelper.getPos(playerID, variable, BlockPos.ZERO);
            ItemStack stack = GlobalVariableHelper.getStack(playerID, variable);
            return stack.isEmpty() ? posToStr(pos, ext) : stackToStr(stack, ext.equals("id"));
        } else {
            return variableHolder.hasCoordinate(playerID, variable) ?
                    posToStr(variableHolder.getCoordinate(playerID, variable), ext) :
                    (variableHolder.hasStack(playerID, variable) ? stackToStr(variableHolder.getStack(playerID, variable), ext.equals("id")) : "");
        }
    }

    private String stackToStr(ItemStack stack, boolean id) {
        return id ? stack.getItem().getRegistryName().toString() : stack.getDisplayName().getString();
    }

    private String posToStr(BlockPos pos, String ext) {
        switch (ext) {
            case "x": return Integer.toString(pos.getX());
            case "y": return Integer.toString(pos.getY());
            case "z": return Integer.toString(pos.getZ());
            default: return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        }
    }
}
