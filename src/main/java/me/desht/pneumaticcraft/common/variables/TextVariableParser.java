/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.variables;

import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
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
        String[] f = StringUtils.splitByWholeSeparator(variable, ".");
        String ext = "";
        if (f.length > 1) {
            ext = f[f.length - 1];
            variable = f.length == 2 ? f[0] : String.join(".", Arrays.copyOf(f, f.length - 1));
        }

        relevantVariables.add(variable);

        if (variableHolder == null) {
            String v1 = variable.startsWith("#") ? variable.substring(1) : variable;
            GlobalVariableManager gvm = GlobalVariableManager.getInstance();
            return gvm.hasItem(v1) ? stackToStr(gvm.getItem(v1), ext.equals("id")) : posToStr(gvm.getPos(v1), ext);
        } else {
            return variableHolder.hasCoordinate(variable) ?
                    posToStr(variableHolder.getCoordinate(variable), ext) :
                    (variableHolder.hasStack(variable) ? stackToStr(variableHolder.getStack(variable), ext.equals("id")) : "");
        }
    }

    private String stackToStr(ItemStack stack, boolean id) {
        return id ? stack.getItem().getRegistryName().toString() : stack.getHoverName().getString();
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
