package pneumaticCraft.common.thirdparty.computercraft;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

/**
 * Used to get constant return values.
 */

public class LuaConstant extends LuaMethod{

    private final Object constant;

    public LuaConstant(String methodName, Object constant){
        super(methodName);
        this.constant = constant;
    }

    public LuaConstant(String methodName, float constant){
        this(methodName, (double)constant);
    }

    @Override
    public Object[] call(IComputerAccess computer, ILuaContext context, Object[] args) throws LuaException, InterruptedException{
        if(args.length == 0) {
            return new Object[]{constant};
        } else {
            throw new IllegalArgumentException(getMethodName() + " doesn't take any arguments!");
        }
    }

}
