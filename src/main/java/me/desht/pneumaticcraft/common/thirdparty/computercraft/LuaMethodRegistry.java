package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import org.apache.commons.lang3.Validate;

import java.util.*;

public class LuaMethodRegistry {
    private final List<ILuaMethod> luaMethods = new ArrayList<>();
    private final Map<String, Integer> luaMethodMap = new HashMap<>();  // index into luaMethods list
    private String[] luaMethodNames = null;

    public void registerLuaMethod(ILuaMethod method) {
        Integer idx = luaMethodMap.get(method.getMethodName());

        if (idx == null) {
            // add new
            luaMethods.add(method);
            luaMethodMap.put(method.getMethodName(), luaMethods.size() - 1);
        } else {
            // override previous
            luaMethods.set(idx, method);
        }
    }

    public String[] getMethodNames() {
        if (luaMethodNames == null) {
            luaMethodNames = new String[luaMethods.size()];
            Arrays.setAll(luaMethodNames, i -> luaMethods.get(i).getMethodName());
        }
        return luaMethodNames;
    }

    public ILuaMethod getMethod(String methodName) {
        Validate.isTrue(luaMethodMap.containsKey(methodName), "Attempt to get unregistered method '" + methodName + "'.");
        return luaMethods.get(luaMethodMap.get(methodName));
    }

    public ILuaMethod getMethod(int methodIndex) {
        return luaMethods.get(methodIndex);
    }

    public boolean isInited() {
        return !luaMethods.isEmpty();
    }
}
