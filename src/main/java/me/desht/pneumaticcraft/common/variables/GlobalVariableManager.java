package me.desht.pneumaticcraft.common.variables;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages global variables. These are prefixed with '#'.
 */
public class GlobalVariableManager extends WorldSavedData {
    public static final int MAX_VARIABLE_LEN = 64;

    private static final String DATA_KEY = "PneumaticCraftGlobalVariables";
    private static final GlobalVariableManager CLIENT_INSTANCE = new GlobalVariableManager();

    private static ServerWorld overworld;

    private final Map<String, BlockPos> globalVars = new HashMap<>();
    private final Map<String, ItemStack> globalItemVars = new HashMap<>();
    private final Table<UUID, String, BlockPos> playerVars = HashBasedTable.create();
    private final Table<UUID, String, ItemStack> playerItemVars = HashBasedTable.create();
    private final Set<UUID> importDone = new HashSet<>();  // until 1.17; tracks which players have already done a global->player-global import

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
            markDirty();
        }
    }

    void setPos(UUID ownerUUID, String varName, BlockPos coord) {
        if (!varName.isEmpty()) {
            if (coord == null) {
                playerVars.remove(ownerUUID, varName);
            } else {
                playerVars.put(ownerUUID, varName, coord);
            }
            markDirty();
        }
    }

    void setStack(String varName, ItemStack item) {
        if (!varName.isEmpty()) {
            if (item.isEmpty()) {
                globalItemVars.remove(varName);
            } else {
                globalItemVars.put(varName, item);
            }
            markDirty();
        }
    }

    void setStack(UUID ownerUUID, String varName, ItemStack item) {
        if (!varName.isEmpty()) {
            if (item.isEmpty()) {
                playerItemVars.remove(ownerUUID, varName);
            } else {
                playerItemVars.put(ownerUUID, varName, item);
            }
            markDirty();
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

    @Override
    public void read(CompoundNBT tag) {
        globalVars.clear();
        globalItemVars.clear();
        playerVars.clear();
        playerItemVars.clear();

        readPosList(tag.getList("globalVars", Constants.NBT.TAG_COMPOUND)).forEach(globalVars::put);

        readItemList(tag.getList("globalItemVars", Constants.NBT.TAG_COMPOUND)).forEach(globalItemVars::put);

        CompoundNBT playerPos = tag.getCompound("playerVars");
        for (String id : playerPos.keySet()) {
            readPosList(playerPos.getList(id, Constants.NBT.TAG_COMPOUND)).forEach((k, v) -> playerVars.put(UUID.fromString(id), k, v));
        }

        CompoundNBT playerItems = tag.getCompound("playerItemVars");
        for (String id : playerItems.keySet()) {
            readItemList(playerItems.getList(id, Constants.NBT.TAG_COMPOUND)).forEach((k, v) -> playerItemVars.put(UUID.fromString(id), k, v));
        }

        CompoundNBT done = tag.getCompound("importDone");
        done.keySet().forEach(id -> importDone.add(UUID.fromString(id)));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("globalVars", writePosList(globalVars));

        tag.put("globalItemVars", writeItemList(globalItemVars));

        CompoundNBT playerPos = new CompoundNBT();
        for (UUID uuid : playerVars.rowKeySet()) {
            playerPos.put(uuid.toString(), writePosList(playerVars.row(uuid)));
        }
        tag.put("playerVars", playerPos);

        CompoundNBT playerItems = new CompoundNBT();
        for (UUID uuid : playerItemVars.rowKeySet()) {
            playerItems.put(uuid.toString(), writeItemList(playerItemVars.row(uuid)));
        }
        tag.put("playerItemVars", playerItems);

        if (!importDone.isEmpty()) {
            CompoundNBT done = new CompoundNBT();
            importDone.forEach(id -> done.putBoolean(id.toString(), true));
            tag.put("importDone", done);
        }

        return tag;
    }

    private Map<String,BlockPos> readPosList(ListNBT list) {
        Map<String,BlockPos> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT t = list.getCompound(i);
            map.put(t.getString("varName"), new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z")));
        }
        return map;
    }

    private ListNBT writePosList(Map<String,BlockPos> map) {
        ListNBT list = new ListNBT();
        map.forEach((key, pos) -> {
            CompoundNBT t = new CompoundNBT();
            t.putString("varName", key);
            t.putInt("x", pos.getX());
            t.putInt("y", pos.getY());
            t.putInt("z", pos.getZ());
            list.add(t);
        });
        return list;
    }

    private Map<String,ItemStack> readItemList(ListNBT list) {
        Map<String,ItemStack> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT t = list.getCompound(i);
            map.put(t.getString("varName"), ItemStack.read(t.getCompound("item")));
        }
        return map;
    }

    private ListNBT writeItemList(Map<String, ItemStack> map) {
        ListNBT list = new ListNBT();
        for (Map.Entry<String, ItemStack> entry : map.entrySet()) {
            CompoundNBT t = new CompoundNBT();
            t.putString("varName", entry.getKey());
            CompoundNBT itemTag = new CompoundNBT();
            entry.getValue().write(itemTag);
            t.put("item", itemTag);
            list.add(t);
        }
        return list;
    }

    public Collection<String> getAllActiveVariableNames(PlayerEntity player) {
        Set<String> varNames = new HashSet<>();
        varNames.addAll(globalVars.keySet().stream().filter(s -> !s.isEmpty()).map(s -> "%" + s).collect(Collectors.toList()));
        varNames.addAll(globalItemVars.keySet().stream().filter(s -> !s.isEmpty()).map(s -> "%" + s).collect(Collectors.toList()));
        if (player != null) {
            varNames.addAll(playerVars.row(player.getUniqueID()).keySet().stream().filter(s -> !s.isEmpty()).map(s -> "#" + s).collect(Collectors.toList()));
            varNames.addAll(playerItemVars.row(player.getUniqueID()).keySet().stream().filter(s -> !s.isEmpty()).map(s -> "#" + s).collect(Collectors.toList()));
        }
        return varNames;
    }

    public boolean importGlobals(UUID playerID) {
        if (importDone.contains(playerID)) return false;

        globalVars.forEach((varName, pos) -> playerVars.row(playerID).putIfAbsent(varName, pos));
        globalItemVars.forEach((varName, stack) -> playerItemVars.row(playerID).putIfAbsent(varName, stack));

        importDone.add(playerID);
        markDirty();
        return true;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onServerStarting(FMLServerAboutToStartEvent event) {
            // clear reference to the overworld; necessary when using integrated server & changing worlds
            overworld = null;
        }
    }
}
