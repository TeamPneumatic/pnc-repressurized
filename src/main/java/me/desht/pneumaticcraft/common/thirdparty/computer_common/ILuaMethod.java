package me.desht.pneumaticcraft.common.thirdparty.computer_common;

public interface ILuaMethod {
    String getMethodName();

    Object[] call(Object[] args) throws Exception;
}
