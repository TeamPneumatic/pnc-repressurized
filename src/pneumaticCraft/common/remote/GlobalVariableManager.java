package pneumaticCraft.common.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * Manages global variables. These are prefixed with '#'.
 */
public class GlobalVariableManager extends WorldSavedData{

    public static final String DATA_KEY = "PneumaticCraftGlobalVariables";
    private static GlobalVariableManager CLIENT_INSTANCE = new GlobalVariableManager(DATA_KEY);
    private final Map<String, ChunkPosition> globalVars = new HashMap<String, ChunkPosition>();
    private final Map<String, ItemStack> globalItemVars = new HashMap<String, ItemStack>();
    public static World overworld;

    public static GlobalVariableManager getInstance(){
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return CLIENT_INSTANCE;
        } else {
            if(overworld != null) {
                GlobalVariableManager manager = (GlobalVariableManager)overworld.loadItemData(GlobalVariableManager.class, DATA_KEY);
                if(manager == null) {
                    manager = new GlobalVariableManager(DATA_KEY);
                    overworld.setItemData(DATA_KEY, manager);
                }
                return manager;
            } else {
                throw new IllegalStateException("Overworld not initialized");
            }
        }
    }

    public void set(String varName, boolean value){
        set(varName, value ? 1 : 0);
    }

    public void set(String varName, int value){
        set(varName, value, 0, 0);
    }

    public void set(String varName, int x, int y, int z){
        set(varName, new ChunkPosition(x, y, z));
    }

    public void set(String varName, ChunkPosition pos){
        globalVars.put(varName, pos);
        save();
    }

    public void set(String varName, ItemStack item){
        globalItemVars.put(varName, item);
        save();
    }

    private void save(){
        markDirty();
    }

    public boolean getBoolean(String varName){
        return getInteger(varName) != 0;
    }

    public int getInteger(String varName){
        return getPos(varName).chunkPosX;
    }

    public ChunkPosition getPos(String varName){
        ChunkPosition pos = globalVars.get(varName);
        //if(pos != null) Log.info("getting var: " + varName + " set to " + pos.chunkPosX + ", " + pos.chunkPosY + ", " + pos.chunkPosZ);
        return pos != null ? pos : new ChunkPosition(0, 0, 0);
    }

    public ItemStack getItem(String varName){
        return globalItemVars.get(varName);
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

        readItemVars(tag, globalItemVars);
    }

    public static void readItemVars(NBTTagCompound tag, Map<String, ItemStack> map){
        map.clear();
        NBTTagList list = tag.getTagList("globalItemVars", 10);
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            map.put(t.getString("varName"), ItemStack.loadItemStackFromNBT(t.getCompoundTag("item")));
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

        writeItemVars(tag, globalItemVars);
    }

    public void writeItemVars(NBTTagCompound tag, Map<String, ItemStack> map){
        NBTTagList list = new NBTTagList();
        for(Map.Entry<String, ItemStack> entry : globalItemVars.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("varName", entry.getKey());
            NBTTagCompound itemTag = new NBTTagCompound();
            entry.getValue().writeToNBT(itemTag);
            t.setTag("item", itemTag);
            list.appendTag(t);
        }
        tag.setTag("globalItemVars", list);
    }

    public String[] getAllActiveVariableNames(){
        Set<String> varNames = new HashSet<String>();
        varNames.addAll(globalVars.keySet());
        varNames.addAll(globalItemVars.keySet());
        return varNames.toArray(new String[varNames.size()]);
    }

}
