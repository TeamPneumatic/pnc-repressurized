package pneumaticCraft.common.remote;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/**
 * Manages global variables. These are prefixed with '#'.
 */
public class GlobalVariableManager extends WorldSavedData{

    //   private static GlobalVariableManager INSTANCE = new GlobalVariableManager();
    private static Map<String, ChunkPosition> globalVars = new HashMap<String, ChunkPosition>();
    public static World overworld;
    public static final String DATA_KEY = "PneumaticCraftGlobalVariables";

    /*   public static GlobalVariableManager getInstance(){
           return INSTANCE;
       }*/

    public static void set(String varName, boolean value){
        set(varName, value ? 1 : 0);
    }

    public static void set(String varName, int value){
        set(varName, new ChunkPosition(value, 0, 0));
    }

    public static void set(String varName, ChunkPosition pos){
        globalVars.put(varName, pos);
        if(overworld != null) {
            GlobalVariableManager manager = (GlobalVariableManager)overworld.loadItemData(GlobalVariableManager.class, DATA_KEY);
            if(manager == null) {
                manager = new GlobalVariableManager(DATA_KEY);
                overworld.setItemData(DATA_KEY, manager);
            }
            manager.markDirty();
        }
    }

    public static boolean getBoolean(String varName){
        return getInteger(varName) != 0;
    }

    public static int getInteger(String varName){
        return getPos(varName).chunkPosX;
    }

    public static ChunkPosition getPos(String varName){
        ChunkPosition pos = globalVars.get(varName);
        //if(pos != null) Log.info("getting var: " + varName + " set to " + pos.chunkPosX + ", " + pos.chunkPosY + ", " + pos.chunkPosZ);
        return pos != null ? pos : new ChunkPosition(0, 0, 0);
    }

    public GlobalVariableManager(String dataKey){
        super(dataKey);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        globalVars.clear();
        NBTTagList list = tag.getTagList("globalVars", 10);
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            globalVars.put(t.getString("varName"), new ChunkPosition(t.getInteger("x"), t.getInteger("y"), t.getInteger("z")));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        NBTTagList list = new NBTTagList();
        for(Map.Entry<String, ChunkPosition> entry : globalVars.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("varName", entry.getKey());
            ChunkPosition pos = entry.getValue();
            t.setInteger("x", pos.chunkPosX);
            t.setInteger("y", pos.chunkPosY);
            t.setInteger("z", pos.chunkPosZ);
            list.appendTag(t);
        }
        tag.setTag("globalVars", list);
    }
}
