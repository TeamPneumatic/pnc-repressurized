package me.desht.pneumaticcraft.common.variables;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.desht.pneumaticcraft.common.progwidgets.IVariableProvider;
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

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Manages global variables. These are prefixed with '#'.
 */
public class GlobalVariableManager extends WorldSavedData implements IVariableProvider {
    public static final int MAX_VARIABLE_LEN = 64;

    private static final String DATA_KEY = "PneumaticCraftGlobalVariables";
    private static final GlobalVariableManager CLIENT_INSTANCE = new GlobalVariableManager();

    private static ServerWorld overworld;

    private final Map<String, BlockPos> globalVars = new HashMap<>();
    private final Map<String, ItemStack> globalItemVars = new HashMap<>();
    private final Table<UUID, String, BlockPos> playerVars = HashBasedTable.create();
    private final Table<UUID, String, ItemStack> playerItemVars = HashBasedTable.create();

    public static GlobalVariableManager getInstance() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            return CLIENT_INSTANCE;
        } else {
            return getOverworld().getDataStorage().computeIfAbsent(GlobalVariableManager::new, DATA_KEY);
        }
    }

    private GlobalVariableManager() {
        super(DATA_KEY);
    }

    private static ServerWorld getOverworld() {
        if (overworld == null) {
            overworld = ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD);
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
        if (!varName.isEmpty()) {
            globalVars.put(varName, pos);
            setDirty();
        }
    }

    public void set(UUID ownerUUID, String varName, BlockPos coord) {
        if (!varName.isEmpty()) {
            playerVars.put(ownerUUID, varName, coord);
            setDirty();
        }
    }

    public void set(String varName, ItemStack item) {
        if (!varName.isEmpty()) {
            globalItemVars.put(varName, item);
            setDirty();
        }
    }

    public void set(UUID ownerUUID, String varName, ItemStack item) {
        if (!varName.isEmpty()) {
            playerItemVars.put(ownerUUID, varName, item);
            setDirty();
        }
    }

    public boolean getBoolean(String varName) {
        return getInteger(varName) != 0;
    }

    public int getInteger(String varName) {
        return getPos(varName).getX();
    }

    public boolean hasPos(String varName) {
        return globalVars.containsKey(varName);
    }

    public boolean hasItem(String varName) {
        return globalItemVars.containsKey(varName);
    }

    public BlockPos getPos(String varName) {
        return globalVars.getOrDefault(varName, BlockPos.ZERO);
    }

    public BlockPos getPos(UUID ownerUUID, String varName) {
        BlockPos pos = playerVars.get(ownerUUID, varName);
        return pos == null ? BlockPos.ZERO : pos;
    }

    public ItemStack getItem(String varName) {
        return globalItemVars.getOrDefault(varName, ItemStack.EMPTY);
    }

    private ItemStack getItem(UUID ownerUUID, String varName) {
        ItemStack stack = playerItemVars.get(ownerUUID, varName);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public void load(CompoundNBT tag) {
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
            map.put(t.getString("varName"), ItemStack.of(t.getCompound("item")));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
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
            entry.getValue().save(itemTag);
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

    @Override
    public boolean hasCoordinate(String varName) {
        return globalVars.containsKey(varName);
    }

    @Override
    public BlockPos getCoordinate(String varName) {
        return getPos(varName);
    }

    @Override
    public boolean hasStack(String varName) {
        return globalItemVars.containsKey(varName);
    }

    @Nonnull
    @Override
    public ItemStack getStack(String varName) {
        return getItem(varName);
    }

    public ItemStack getStack(UUID ownerUUID, String varName) {
        return getItem(ownerUUID, varName);
    }
}
