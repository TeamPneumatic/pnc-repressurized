package me.desht.pneumaticcraft.common.variables;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GlobalVariableHelper {
    /**
     * Retrieve a blockpos variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @param def default value if not present in the GVM
     * @return the variable's value
     */
    public static BlockPos getPos(UUID id, String varName, BlockPos def) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("#")) {
            return id != null && gvm.hasPos(id, varName.substring(1)) ? gvm.getPos(id, varName.substring(1)) : def;
        } else if (varName.startsWith("%")) {
            return gvm.hasPos(varName.substring(1)) ? gvm.getPos(varName.substring(1)) : def;
        } else {
            return gvm.hasPos(id, varName) ? gvm.getPos(id, varName) : def;
        }
    }

    /**
     * Retrieve a blockpos variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @return the variable's value
     */
    public static BlockPos getPos(UUID id, String varName) {
        return getPos(id, varName, null);
    }

    public static ItemStack getStack(UUID id, String varName, ItemStack def) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("#")) {
            return gvm.hasItem(id, varName.substring(1)) ? gvm.getItem(id, varName.substring(1)) : def;
        } else if (varName.startsWith("%")) {
            return gvm.hasItem(varName.substring(1)) ? gvm.getItem(varName.substring(1)) : def;
        } else {
            return gvm.hasItem(id, varName) ? gvm.getItem(id, varName) : def;
        }
    }

    public static ItemStack getStack(UUID id, String varName) {
        return getStack(id, varName, ItemStack.EMPTY);
    }

    public static void setPos(UUID id, String varName, BlockPos pos) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("#") && id != null) {
            gvm.set(id, varName.substring(1), pos);
        } else if (varName.startsWith("%")) {
            gvm.set(varName.substring(1), pos);
        } else if (id != null) {
            gvm.set(id, varName, pos);
        }
    }

    public static boolean getBool(UUID id, String varName) {
        return getInt(id, varName) != 0;
    }

    public static int getInt(UUID id, String varName) {
        return getPos(id, varName, BlockPos.ZERO).getX();
    }

    public static void setBool(UUID id, String varName, boolean val) {
        setInt(id, varName, val ? 1 : 0);
    }

    public static void setInt(UUID id, String varName, int val) {
        setPos(id, varName, new BlockPos(val, 0, 0));
    }

    /**
     * Given a plain variable name, add the "#" or "%" prefix as appropriate
     * @param varName the variable
     * @param playerGlobal true if a player-global, false if a server-global
     * @return the prefixed var name
     */
    public static String getPrefixedVar(String varName, boolean playerGlobal) {
        return varName.isEmpty() ? "" : getVarPrefix(playerGlobal) + varName;
    }

    /**
     * Get the correct var prefix
     * @param playerGlobal true if a player-global, false if a server-global
     * @return the var prefix
     */
    public static String getVarPrefix(boolean playerGlobal) {
        return playerGlobal ? "#" : "%";
    }

    /**
     * Strip the prefix character from a var name
     * @param varName the var name
     * @return the var name without a prefix
     */
    public static String stripVarPrefix(String varName) {
        return hasPrefix(varName) ? varName.substring(1) : varName;
    }

    /**
     * Check if this varname has a prefix character
     * @param varName the var name
     * @return true if prefixed, false otherwise
     */
    public static boolean hasPrefix(String varName) {
        return varName.startsWith("#") || varName.startsWith("%");
    }

    /**
     * Given a list of var names, return a corresponding list of varname with the right prefix
     * @param varnames the var names
     * @param playerGlobal true if player-global, false if server-global
     * @return a list of prefixed var names
     */
    public static List<String> extractVarnames(String[] varnames, boolean playerGlobal) {
        List<String> res = new ArrayList<>();
        for (String v : varnames) {
            if (playerGlobal && v.startsWith("#") || !playerGlobal && v.startsWith("%")) {
                res.add(v.substring(1));
            }
        }
        return res;
    }
}
