package me.desht.pneumaticcraft.common.variables;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages global variables. These are prefixed with '#'.
 */
public class GlobalVariableManager extends WorldSavedData {
    public static final int MAX_VARIABLE_LEN = 64;

    private static final String DATA_KEY = "PneumaticCraftGlobalVariables";
    private static final GlobalVariableManager CLIENT_INSTANCE = new GlobalVariableManager();

    private final Map<String, BlockPos> globalVars = new HashMap<>();
    private final Map<String, ItemStack> globalItemVars = new HashMap<>();
    private static ServerWorld overworld;

    public static GlobalVariableManager getInstance() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            return CLIENT_INSTANCE;
        } else {
            return getOverworld().getSavedData().getOrCreate(GlobalVariableManager::new, DATA_KEY);
        }
    }

    private GlobalVariableManager() {
        super(DATA_KEY);
    }

    private static ServerWorld getOverworld() {
        if (overworld == null) {
            overworld = ServerLifecycleHooks.getCurrentServer().getWorld(World.OVERWORLD);
            if (overworld == null) {
                throw new IllegalStateException("Overworld not initialized!");
            }
        }
        return overworld;
    }

    public void set(String varName, boolean value) {
        set(varName, value ? 1 : 0);
    }

    public void set(String varName, int value) {
        set(varName, value, 0, 0);
    }

    public void set(String varName, int x, int y, int z) {
        set(varName, new BlockPos(x, y, z));
    }

    public void set(String varName, BlockPos pos) {
        globalVars.put(varName, pos);
        save();
    }

    public void set(String varName, ItemStack item) {
        globalItemVars.put(varName, item);
        save();
    }

    private void save() {
        markDirty();
    }

    public boolean getBoolean(String varName) {
        return getInteger(varName) != 0;
    }

    public int getInteger(String varName) {
        return getPos(varName).getX();
    }

    public BlockPos getPos(String varName) {
        BlockPos pos = globalVars.get(varName);
        //if(pos != null) Log.info("getting var: " + varName + " set to " + pos.chunkPosX + ", " + pos.chunkPosY + ", " + pos.chunkPosZ);
        return pos != null ? pos : BlockPos.ZERO;
    }

    public ItemStack getItem(String varName) {
        return globalItemVars.getOrDefault(varName, ItemStack.EMPTY);
    }

    @Override
    public void read(CompoundNBT tag) {
        globalVars.clear();
        ListNBT list = tag.getList("globalVars", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT t = list.getCompound(i);
            globalVars.put(t.getString("varName"), new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z")));
        }

        readItemVars(tag, globalItemVars);
    }

    public static void readItemVars(CompoundNBT tag, Map<String, ItemStack> map) {
        map.clear();
        ListNBT list = tag.getList("globalItemVars", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT t = list.getCompound(i);
            map.put(t.getString("varName"), ItemStack.read(t.getCompound("item")));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        for (Map.Entry<String, BlockPos> entry : globalVars.entrySet()) {
            CompoundNBT t = new CompoundNBT();
            t.putString("varName", entry.getKey());
            BlockPos pos = entry.getValue();
            t.putInt("x", pos.getX());
            t.putInt("y", pos.getY());
            t.putInt("z", pos.getZ());
            list.add(t);
        }
        tag.put("globalVars", list);

        writeItemVars(tag);
        return tag;
    }

    public void writeItemVars(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        for (Map.Entry<String, ItemStack> entry : globalItemVars.entrySet()) {
            CompoundNBT t = new CompoundNBT();
            t.putString("varName", entry.getKey());
            CompoundNBT itemTag = new CompoundNBT();
            entry.getValue().write(itemTag);
            t.put("item", itemTag);
            list.add(t);
        }
        tag.put("globalItemVars", list);
    }

    public String[] getAllActiveVariableNames() {
        Set<String> varNames = new HashSet<>();
        varNames.addAll(globalVars.keySet());
        varNames.addAll(globalItemVars.keySet());
        return varNames.toArray(new String[0]);
    }

}
