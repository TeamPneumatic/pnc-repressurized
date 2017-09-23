package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import net.minecraft.util.EnumFacing;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class LuaMethod implements ILuaMethod {
    private final String methodName;

    public LuaMethod(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    protected EnumFacing getDirForString(String luaParm) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (dir.toString().toLowerCase().equals(luaParm.toLowerCase())) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Side can only be up, down, north, east, south or west!");
    }

    protected LinkedHashMap<Integer, String> getStringTable(List<String> list) {
        LinkedHashMap<Integer, String> table = new LinkedHashMap<Integer, String>();
        for (int i = 0; i < list.size(); i++) {
            table.put(i + 1, list.get(i));
        }
        return table;
    }
}
