package me.desht.pneumaticcraft.api.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface IGlobalVariableHelper {
    /**
     * Retrieve a blockpos variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @param def default value if not present in the GVM
     * @return the variable's value
     */
    BlockPos getPos(@Nullable UUID id, String varName, BlockPos def);

    /**
     * Retrieve a blockpos variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @return the variable's value
     */
    BlockPos getPos(@Nullable UUID id, String varName);

    /**
     * Retrieve an itemstack variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global "#".
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @param def default value if not present in the GVM
     * @return the variable's value
     */
    ItemStack getStack(@Nullable UUID id, String varName, ItemStack def);

    /**
     * Retrieve an itemstack variable from the GVM. The variable may start with "#" or "%" to indicate player-global
     * or global respectively. Missing prefix defaults to player-global.
     * @param id the ID of the owning player (ignored for "%" global variables, must be a valid player UUID otherwise)
     * @param varName the variable name, optionally prefixed with "%" or "#"
     * @return the variable's value
     */
    ItemStack getStack(@Nullable UUID id, String varName);

    int getInt(UUID id, String varName);

    void setPos(UUID id, String varName, BlockPos pos);

    void setStack(UUID id, String varName, ItemStack stack);

    boolean getBool(UUID id, String varName);

    /**
     * Given a plain variable name, add the "#" or "%" prefix as appropriate
     * @param varName the variable
     * @param playerGlobal true if a player-global, false if a server-global
     * @return the prefixed var name
     */
    String getPrefixedVar(String varName, boolean playerGlobal);

    /**
     * Get the correct var prefix
     * @param playerGlobal true if a player-global, false if a server-global
     * @return the var prefix
     */
    String getVarPrefix(boolean playerGlobal);

    /**
     * Strip the prefix character from a var name
     * @param varName the var name
     * @return the var name without a prefix
     */
    String stripVarPrefix(String varName);

    /**
     * Check if this varname has a prefix character
     * @param varName the var name
     * @return true if prefixed, false otherwise
     */
    boolean hasPrefix(String varName);

    /**
     * Parse a string, and extract a set of global variables (both player-global and server-global) referred to in the
     * string via {@code ${varname}} notation.
     *
     * @param string   the string to parse
     * @param playerId UUID of the player
     * @return a set of global variable names
     */
    Set<String> getRelevantVariables(String string, UUID playerId);
}
