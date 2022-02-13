/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import me.desht.pneumaticcraft.common.block.entity.ILuaMethodProvider;
import org.apache.commons.lang3.Validate;

import java.util.*;

public class LuaMethodRegistry {
    private final List<ILuaMethod> luaMethods = new ArrayList<>();
    private final Map<String, Integer> luaMethodMap = new HashMap<>();  // index into luaMethods list
    private final ILuaMethodProvider provider;
    private String[] luaMethodNames = null;
    private boolean inited = false;

    public LuaMethodRegistry(ILuaMethodProvider provider) {
        this.provider = provider;
    }

    private void init() {
        if (!inited) {
            provider.addLuaMethods(this);
            inited = true;
        }
    }

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
        init();
        if (luaMethodNames == null) {
            luaMethodNames = new String[luaMethods.size()];
            Arrays.setAll(luaMethodNames, i -> luaMethods.get(i).getMethodName());
        }
        return luaMethodNames;
    }

    public ILuaMethod getMethod(String methodName) {
        init();
        Validate.isTrue(luaMethodMap.containsKey(methodName), "Attempt to get unregistered method '" + methodName + "'.");
        return luaMethods.get(luaMethodMap.get(methodName));
    }

    public ILuaMethod getMethod(int methodIndex) {
        init();
        return luaMethods.get(methodIndex);
    }
}
