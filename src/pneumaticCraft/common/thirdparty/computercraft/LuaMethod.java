package pneumaticCraft.common.thirdparty.computercraft;

import java.util.LinkedHashMap;
import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;

public abstract class LuaMethod implements ILuaMethod{
    private final String methodName;

    public LuaMethod(String methodName){
        this.methodName = methodName;
    }

    @Override
    public String getMethodName(){
        return methodName;
    }

    protected ForgeDirection getDirForString(String luaParm){
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if(dir.toString().toLowerCase().equals(luaParm.toLowerCase())) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Side can only be up, down, north, east, south or west!");
    }

    protected LinkedHashMap<Integer, String> getStringTable(List<String> list){
        LinkedHashMap<Integer, String> table = new LinkedHashMap<Integer, String>();
        for(int i = 0; i < list.size(); i++) {
            table.put(i + 1, list.get(i));
        }
        return table;
    }
}
