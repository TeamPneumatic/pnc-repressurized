package me.desht.pneumaticcraft.common.variables;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

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
            return gvm.hasPos(id, varName.substring(1)) ? gvm.getPos(id, varName.substring(1)) : def;
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
        if (varName.startsWith("#")) {
            gvm.set(id, varName.substring(1), pos);
        } else if (varName.startsWith("%")) {
            gvm.set(varName.substring(1), pos);
        } else {
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
}
