package me.desht.pneumaticcraft.common.thirdparty.computercraft;

public interface ILuaMethod {
    String getMethodName();

    Object[] call(Object[] args) throws Exception;
}
