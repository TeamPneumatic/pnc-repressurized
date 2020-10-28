package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import net.minecraft.util.Direction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public abstract class LuaMethod implements ILuaMethod {
    private final String methodName;

    protected LuaMethod(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    protected Direction getDirForString(String luaParm) {
        return Direction.valueOf(luaParm.toUpperCase(Locale.ROOT));
    }

    LinkedHashMap<Integer, String> getStringTable(List<String> list) {
        LinkedHashMap<Integer, String> table = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            table.put(i + 1, list.get(i));
        }
        return table;
    }

    protected void requireArgs(Object[] args, int min, int max, String desc) {
        Validate.isTrue(args.length >= min && args.length <= max,
                String.format("Method '%s' takes between %d and %d arguments! (%s)", getMethodName(), min, max, desc));
    }

    protected void requireArgs(Object[] args, int len, String desc) {
        Validate.isTrue(args.length == len,
                String.format("Method '%s' takes exactly %d arguments! (%s)", getMethodName(), len, desc));
    }

    protected void requireArgs(Object[] args, int[] argcount, String desc) {
        for (int a : argcount) {
            if (args.length == a) return;
        }

        throw new IllegalArgumentException(String.format("Method '%s' takes either %s arguments! (%s)",
                getMethodName(), StringUtils.join(ArrayUtils.toObject(argcount), " or "), desc));
    }

    protected void requireNoArgs(Object[] args) {
        Validate.isTrue(args.length == 0, String.format("Method '%s' takes no arguments!", getMethodName()));
    }
}
