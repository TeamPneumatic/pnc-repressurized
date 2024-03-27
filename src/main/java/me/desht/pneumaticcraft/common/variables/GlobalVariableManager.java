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
import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * Manages global variables. These are prefixed with '#' or '%'.
 */
public class GlobalVariableManager extends SavedData {
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
            return getOverworld().getDataStorage().computeIfAbsent(new Factory<>(GlobalVariableManager::new, GlobalVariableManager::load), DATA_KEY);
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

    boolean hasPos(String varName) {
        return globalVars.containsKey(varName);
    }

    boolean hasPos(UUID ownerUUID, String varName) {
        return playerVars.contains(ownerUUID, varName);
    }

    BlockPos getPos(String varName) {
        return globalVars.getOrDefault(varName, BlockPos.ZERO);
    }

    BlockPos getPos(UUID ownerUUID, String varName) {
        BlockPos pos = playerVars.get(ownerUUID, varName);
        return pos == null ? BlockPos.ZERO : pos;
    }

    void setPos(String varName, BlockPos pos) {
        if (!varName.isEmpty()) {
            if (pos == null) {
                globalVars.remove(varName);
            } else {
                globalVars.put(varName, pos);
            }
            setDirty();
        }
    }

    void setPos(UUID ownerUUID, String varName, BlockPos coord) {
        if (!varName.isEmpty()) {
            if (coord == null) {
                playerVars.remove(ownerUUID, varName);
            } else {
                playerVars.put(ownerUUID, varName, coord);
            }
            setDirty();
        }
    }

    void setStack(String varName, ItemStack item) {
        if (!varName.isEmpty()) {
            if (item.isEmpty()) {
                globalItemVars.remove(varName);
            } else {
                globalItemVars.put(varName, item);
            }
            setDirty();
        }
    }

    void setStack(UUID ownerUUID, String varName, ItemStack item) {
        if (!varName.isEmpty()) {
            if (item.isEmpty()) {
                playerItemVars.remove(ownerUUID, varName);
            } else {
                playerItemVars.put(ownerUUID, varName, item);
            }
            setDirty();
        }
    }

    boolean hasStack(String varName) {
        return globalItemVars.containsKey(varName);
    }

    boolean hasStack(UUID ownerUUID, String varName) {
        return playerItemVars.contains(ownerUUID, varName);
    }

    ItemStack getStack(String varName) {
        return globalItemVars.getOrDefault(varName, ItemStack.EMPTY);
    }

    ItemStack getStack(UUID ownerUUID, String varName) {
        ItemStack stack = playerItemVars.get(ownerUUID, varName);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private void readFromNBT(CompoundTag tag) {
        globalVars.clear();
        globalItemVars.clear();
        playerVars.clear();
        playerItemVars.clear();

        globalVars.putAll(readPosList(tag.getList("globalVars", Tag.TAG_COMPOUND)));

        globalItemVars.putAll(readItemList(tag.getList("globalItemVars", Tag.TAG_COMPOUND)));

        CompoundTag playerPos = tag.getCompound("playerVars");
        for (String id : playerPos.getAllKeys()) {
            readPosList(playerPos.getList(id, Tag.TAG_COMPOUND)).forEach((k, v) -> playerVars.put(UUID.fromString(id), k, v));
        }

        CompoundTag playerItems = tag.getCompound("playerItemVars");
        for (String id : playerItems.getAllKeys()) {
            readItemList(playerItems.getList(id, Tag.TAG_COMPOUND)).forEach((k, v) -> playerItemVars.put(UUID.fromString(id), k, v));
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("globalVars", writePosList(globalVars));

        tag.put("globalItemVars", writeItemList(globalItemVars));

        CompoundTag playerPos = new CompoundTag();
        for (UUID uuid : playerVars.rowKeySet()) {
            playerPos.put(uuid.toString(), writePosList(playerVars.row(uuid)));
        }
        tag.put("playerVars", playerPos);

        CompoundTag playerItems = new CompoundTag();
        for (UUID uuid : playerItemVars.rowKeySet()) {
            playerItems.put(uuid.toString(), writeItemList(playerItemVars.row(uuid)));
        }
        tag.put("playerItemVars", playerItems);

        return tag;
    }

    private Map<String,BlockPos> readPosList(ListTag list) {
        Map<String,BlockPos> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            map.put(t.getString("varName"), new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z")));
        }
        return map;
    }

    private ListTag writePosList(Map<String,BlockPos> map) {
        ListTag list = new ListTag();
        map.forEach((key, pos) -> {
            CompoundTag t = new CompoundTag();
            t.putString("varName", key);
            t.putInt("x", pos.getX());
            t.putInt("y", pos.getY());
            t.putInt("z", pos.getZ());
            list.add(t);
        });
        return list;
    }

    private Map<String,ItemStack> readItemList(ListTag list) {
        Map<String,ItemStack> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            map.put(t.getString("varName"), ItemStack.of(t.getCompound("item")));
        }
        return map;
    }

    private ListTag writeItemList(Map<String, ItemStack> map) {
        ListTag list = new ListTag();
        for (Map.Entry<String, ItemStack> entry : map.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putString("varName", entry.getKey());
            CompoundTag itemTag = new CompoundTag();
            entry.getValue().save(itemTag);
            t.put("item", itemTag);
            list.add(t);
        }
        return list;
    }
    
    public Collection<String> getAllActiveVariableNames(Player player) {
        Set<String> varNames = new HashSet<>();
        varNames.addAll(globalVars.keySet().stream().filter(s -> !s.isEmpty()).map(s -> "%" + s).toList());
        varNames.addAll(globalItemVars.keySet().stream().filter(s -> !s.isEmpty()).map(s -> "%" + s).toList());
        if (player != null) {
            varNames.addAll(playerVars.row(player.getUUID()).keySet().stream().filter(s -> !s.isEmpty()).map(s -> "#" + s).toList());
            varNames.addAll(playerItemVars.row(player.getUUID()).keySet().stream().filter(s -> !s.isEmpty()).map(s -> "#" + s).toList());
        }
        return varNames;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onServerStarting(ServerAboutToStartEvent event) {
            // clear reference to the overworld; necessary when using integrated server & changing worlds
            overworld = null;
        }
    }
}
