package me.desht.pneumaticcraft.common.thirdparty.computer_common;

/**
 * Used to get constant return values.
 */

public class LuaConstant extends LuaMethod {

    private final Object constant;

    private LuaConstant(String methodName, Object constant) {
        super(methodName);
        this.constant = constant;
    }

    public LuaConstant(String methodName, float constant) {
        this(methodName, (double) constant);
    }

    @Override
    public Object[] call(Object[] args) {
        requireNoArgs(args);
        return new Object[]{constant};
    }

}
