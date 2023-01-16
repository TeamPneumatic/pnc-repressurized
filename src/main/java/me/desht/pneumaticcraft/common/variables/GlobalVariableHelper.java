package me.desht.pneumaticcraft.common.variables;

import me.desht.pneumaticcraft.common.drone.progwidgets.IVariableProvider;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GlobalVariableHelper {
    /**
     * Retrieve a blockpos variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @param def default value if not present in the GVM
     * @return the variable's value
     */
    public static BlockPos getPos(@Nullable UUID id, String varName, BlockPos def) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("%")) {
            return gvm.hasPos(varName.substring(1)) ? gvm.getPos(varName.substring(1)) : def;
        }
        if (id == null) {
            Log.warning("querying player-global var %s with no player context?", varName);
            return def;
        }
        if (varName.startsWith("#")) {
            varName = varName.substring(1);
        }
        return gvm.hasPos(id, varName) ? gvm.getPos(id, varName) : def;
    }

    /**
     * Retrieve a blockpos variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @return the variable's value
     */
    public static BlockPos getPos(@Nullable UUID id, String varName) {
        return getPos(id, varName, null);
    }

    /**
     * Retrieve an itemstack variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global "#".
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @param def default value if not present in the GVM
     * @return the variable's value
     */
    public static ItemStack getStack(@Nullable UUID id, String varName, ItemStack def) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("%")) {
            return gvm.hasStack(varName.substring(1)) ? gvm.getStack(varName.substring(1)) : def;
        }
        if (id == null) {
            Log.warning("querying player-global var %s with no player context?", varName);
            return def;
        }
        if (varName.startsWith("#")) {
            varName = varName.substring(1);
        }
        return gvm.hasStack(id, varName) ? gvm.getStack(id, varName) : def;
    }

    /**
     * Retrieve an itemstack variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @return the variable's value
     */
    public static ItemStack getStack(@Nullable UUID id, String varName) {
        return getStack(id, varName, ItemStack.EMPTY);
    }

    public static void setPos(UUID id, String varName, BlockPos pos) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("#") && id != null) {
            gvm.setPos(id, varName.substring(1), pos);
        } else if (varName.startsWith("%")) {
            gvm.setPos(varName.substring(1), pos);
        } else if (id != null) {
            gvm.setPos(id, varName, pos);
        }
    }

    public static void setStack(UUID id, String varName, ItemStack stack) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("#") && id != null) {
            gvm.setStack(id, varName.substring(1), stack);
        } else if (varName.startsWith("%")) {
            gvm.setStack(varName.substring(1), stack);
        } else if (id != null) {
            gvm.setStack(id, varName, stack);
        }
    }

    public static boolean getBool(UUID id, String varName) {
        return getInt(id, varName) != 0;
    }

    public static int getInt(UUID id, String varName) {
        return getPos(id, varName, BlockPos.ZERO).getX();
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
        return varName.length() > 1 && (varName.startsWith("#") || varName.startsWith("%"));
    }

    /**
     * Given a list of prefixed var names, return a corresponding list of var names with a matching prefix character
     * @param varnames the var names
     * @param playerGlobal true to extract player-global, false for server-global
     * @return a list of unprefixed var names
     */
    public static List<String> extractVarnames(String[] varnames, boolean playerGlobal) {
        return Arrays.stream(varnames)
                .filter(v -> playerGlobal && v.startsWith("#") || !playerGlobal && v.startsWith("%"))
                .map(v -> v.substring(1))
                .collect(Collectors.toList());
    }

    public static IVariableProvider getVariableProvider() {
        return VariableProviderWrapper.INSTANCE;
    }

    private enum VariableProviderWrapper implements IVariableProvider {
        INSTANCE;

        @Override
        public Optional<BlockPos> getCoordinate(UUID id, String varName) {
            return Optional.ofNullable(GlobalVariableHelper.getPos(id, varName));
        }

        @Nonnull
        @Override
        public ItemStack getStack(UUID id, String varName) {
            return GlobalVariableHelper.getStack(id, varName);
        }
    }
}
