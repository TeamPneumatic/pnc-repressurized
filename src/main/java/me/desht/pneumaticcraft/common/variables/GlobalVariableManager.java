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

package me.desht.pneumaticcraft.common.variables;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.desht.pneumaticcraft.common.progwidgets.IVariableProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Manages global variables. These are prefixed with '#'.
 */
public class GlobalVariableManager extends SavedData implements IVariableProvider {
    public static final int MAX_VARIABLE_LEN = 64;

    private static final String DATA_KEY = "PneumaticCraftGlobalVariables";
    private static final GlobalVariableManager CLIENT_INSTANCE = new GlobalVariableManager();

    private static ServerLevel overworld;

    private final Map<String, BlockPos> globalVars = new HashMap<>();
    private final Map<String, ItemStack> globalItemVars = new HashMap<>();
    private final Table<UUID, String, BlockPos> playerVars = HashBasedTable.create();
    private final Table<UUID, String, ItemStack> playerItemVars = HashBasedTable.create();

    public static GlobalVariableManager getInstance() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            return CLIENT_INSTANCE;
        } else {
            return getOverworld().getDataStorage().computeIfAbsent(GlobalVariableManager::load, GlobalVariableManager::new, DATA_KEY);
        }
    }

    private GlobalVariableManager() {
    }

    private static ServerLevel getOverworld() {
        if (overworld == null) {
            overworld = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
            if (overworld == null) {
                throw new IllegalStateException("Overworld not initialized!");
            }
        }
        return overworld;
    }

    public static GlobalVariableManager load(CompoundTag tag) {
        GlobalVariableManager gvm = new GlobalVariableManager();
        gvm.readFromNBT(tag);
        return gvm;
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

    private void readFromNBT(CompoundTag tag) {
        globalVars.clear();
        ListTag list = tag.getList("globalVars", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            globalVars.put(t.getString("varName"), new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z")));
        }

        readItemVars(tag, globalItemVars);
    }

    public static void readItemVars(CompoundTag tag, Map<String, ItemStack> map) {
        map.clear();
        ListTag list = tag.getList("globalItemVars", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            map.put(t.getString("varName"), ItemStack.of(t.getCompound("item")));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<String, BlockPos> entry : globalVars.entrySet()) {
            CompoundTag t = new CompoundTag();
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

    public void writeItemVars(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<String, ItemStack> entry : globalItemVars.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putString("varName", entry.getKey());
            CompoundTag itemTag = new CompoundTag();
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
