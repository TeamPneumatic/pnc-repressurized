package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;

/**
 * A Tile Entity which makes some of its functionality available via Lua methods for calling from other mods
 * (e.g. ComputerCraft)
 */
public interface ILuaMethodProvider {
    /**
     * Get this TE's method registry object.  This should be created in the TE constructor, but not populated
     * with methods yet.
     * @return the method registry
     */
    LuaMethodRegistry getLuaMethodRegistry();

    /**
     * Get a unique identifier for this type of method provider.  The TE's type (a registry ID) is a good choice.
     * @return a unique string identifier
     */
    String getPeripheralType();

    /**
     * Called lazily to populate the method registry with the methods.
     * @param registry the registry to populate
     */
    void addLuaMethods(LuaMethodRegistry registry);
}
