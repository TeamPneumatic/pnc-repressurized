package pneumaticCraft.common.thirdparty.computercraft;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public interface ILuaMethod{
    public String getMethodName();

    public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args)  throws LuaException, InterruptedException;
}
