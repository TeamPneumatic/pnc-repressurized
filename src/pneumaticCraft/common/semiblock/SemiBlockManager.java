package pneumaticCraft.common.semiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketDescription;
import pneumaticCraft.common.network.PacketSetSemiBlock;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;

import com.google.common.collect.HashBiMap;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class SemiBlockManager{
    private final Map<Chunk, Map<ChunkPosition, ISemiBlock>> semiBlocks = new HashMap<Chunk, Map<ChunkPosition, ISemiBlock>>();
    private final List<ISemiBlock> addingBlocks = new ArrayList<ISemiBlock>();
    private final Map<Chunk, Set<EntityPlayerMP>> syncList = new HashMap<Chunk, Set<EntityPlayerMP>>();
    private final Set<Chunk> chunksMarkedForRemoval = new HashSet<Chunk>();
    public static final int SYNC_DISTANCE = 64;
    private static final HashBiMap<String, Class<? extends ISemiBlock>> registeredTypes = HashBiMap.create();
    private static final HashBiMap<Class<? extends ISemiBlock>, Item> semiBlockToItems = HashBiMap.create();
    private static final SemiBlockManager INSTANCE = new SemiBlockManager();
    private static final SemiBlockManager CLIENT_INSTANCE = new SemiBlockManager();

    public static SemiBlockManager getServerInstance(){
        return INSTANCE;
    }

    public static SemiBlockManager getClientOldInstance(){
        return CLIENT_INSTANCE;
    }

    public static SemiBlockManager getInstance(World world){
        return world.isRemote ? CLIENT_INSTANCE : INSTANCE;
    }

    public static Item registerSemiBlock(String key, Class<? extends ISemiBlock> semiBlock, boolean addItem){
        if(registeredTypes.containsKey(key)) throw new IllegalArgumentException("Duplicate registration key: " + key);
        registeredTypes.put(key, semiBlock);

        if(addItem) {
            ItemSemiBlockBase item = new ItemSemiBlockBase(key);
            GameRegistry.registerItem(item, key);
            PneumaticCraft.proxy.registerSemiBlockRenderer(item);
            registerSemiBlockToItemMapping(semiBlock, item);
            return item;
        } else {
            return null;
        }
    }

    public static void registerSemiBlockToItemMapping(Class<? extends ISemiBlock> semiBlock, Item item){
        semiBlockToItems.put(semiBlock, item);
    }

    public static Item getItemForSemiBlock(ISemiBlock semiBlock){
        return getItemForSemiBlock(semiBlock.getClass());
    }

    public static Item getItemForSemiBlock(Class<? extends ISemiBlock> semiBlock){
        return semiBlockToItems.get(semiBlock);
    }

    public static Class<? extends ISemiBlock> getSemiBlockForItem(Item item){
        return semiBlockToItems.inverse().get(item);
    }

    public static String getKeyForSemiBlock(ISemiBlock semiBlock){
        return getKeyForSemiBlock(semiBlock.getClass());
    }

    public static String getKeyForSemiBlock(Class<? extends ISemiBlock> semiBlock){
        return registeredTypes.inverse().get(semiBlock);
    }

    public static ISemiBlock getSemiBlockForKey(String key){
        try {
            Class<? extends ISemiBlock> clazz = registeredTypes.get(key);
            if(clazz != null) {
                return clazz.newInstance();
            } else {
                Log.warning("Semi Block with id \"" + key + "\" isn't registered!");
                return null;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    public void onChunkUnLoad(ChunkEvent.Unload event){
        if(!event.world.isRemote) {
            chunksMarkedForRemoval.add(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event){
        Map<ChunkPosition, ISemiBlock> map = semiBlocks.get(event.getChunk());
        if(map != null && map.size() > 0) {
            NBTTagList tagList = new NBTTagList();
            for(Map.Entry<ChunkPosition, ISemiBlock> entry : map.entrySet()) {
                NBTTagCompound t = new NBTTagCompound();
                entry.getValue().writeToNBT(t);
                t.setInteger("x", entry.getKey().chunkPosX);
                t.setInteger("y", entry.getKey().chunkPosY);
                t.setInteger("z", entry.getKey().chunkPosZ);
                t.setString("type", getKeyForSemiBlock(entry.getValue()));
                tagList.appendTag(t);
            }
            event.getData().setTag("SemiBlocks", tagList);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event){
        try {
            if(!event.world.isRemote) {
                if(event.getData().hasKey("SemiBlocks")) {
                    Map<ChunkPosition, ISemiBlock> map = getOrCreateMap(event.getChunk());
                    map.clear();
                    NBTTagList tagList = event.getData().getTagList("SemiBlocks", 10);
                    for(int i = 0; i < tagList.tagCount(); i++) {
                        NBTTagCompound t = tagList.getCompoundTagAt(i);
                        ISemiBlock semiBlock = getSemiBlockForKey(t.getString("type"));
                        if(semiBlock != null) {
                            semiBlock.readFromNBT(t);
                            setSemiBlock(event.world, t.getInteger("x"), t.getInteger("y"), t.getInteger("z"), semiBlock, event.getChunk());
                        }
                    }
                }
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event){
        for(ISemiBlock semiBlock : addingBlocks) {
            Chunk chunk = semiBlock.getWorld().getChunkFromBlockCoords(semiBlock.getPos().chunkPosX, semiBlock.getPos().chunkPosZ);
            getOrCreateMap(chunk).put(semiBlock.getPos(), semiBlock);
            chunk.setChunkModified();

            for(EntityPlayerMP player : syncList.get(chunk)) {
                NetworkHandler.sendTo(new PacketSetSemiBlock(semiBlock), player);
                PacketDescription descPacket = semiBlock.getDescriptionPacket();
                if(descPacket != null) NetworkHandler.sendTo(descPacket, player);
            }
        }
        addingBlocks.clear();

        for(Chunk removingChunk : chunksMarkedForRemoval) {
            if(!removingChunk.isChunkLoaded) {
                semiBlocks.remove(removingChunk);
                syncList.remove(removingChunk);
            }
        }
        chunksMarkedForRemoval.clear();

        for(Map<ChunkPosition, ISemiBlock> map : semiBlocks.values()) {
            for(ISemiBlock semiBlock : map.values()) {
                if(!semiBlock.isInvalid()) semiBlock.update();
            }
            Iterator<ISemiBlock> iterator = map.values().iterator();
            while(iterator.hasNext()) {
                if(iterator.next().isInvalid()) {
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event){
        if(this == getServerInstance()) getClientOldInstance().onClientTick(event);
        else {
            EntityPlayer player = PneumaticCraft.proxy.getPlayer();
            if(player != null) {
                for(ISemiBlock semiBlock : addingBlocks) {
                    Chunk chunk = semiBlock.getWorld().getChunkFromBlockCoords(semiBlock.getPos().chunkPosX, semiBlock.getPos().chunkPosZ);
                    getOrCreateMap(chunk).put(semiBlock.getPos(), semiBlock);
                }
                addingBlocks.clear();

                Iterator<Map.Entry<Chunk, Map<ChunkPosition, ISemiBlock>>> iterator = semiBlocks.entrySet().iterator();
                while(iterator.hasNext()) {
                    Map.Entry<Chunk, Map<ChunkPosition, ISemiBlock>> entry = iterator.next();
                    if(PneumaticCraftUtils.distBetween(player.posX, 0, player.posZ, entry.getKey().xPosition * 16 - 8, 0, entry.getKey().zPosition * 16 - 8) > SYNC_DISTANCE + 10) {
                        iterator.remove();
                    } else {
                        for(ISemiBlock semiBlock : entry.getValue().values()) {
                            if(!semiBlock.isInvalid()) semiBlock.update();
                        }
                        Iterator<ISemiBlock> it = entry.getValue().values().iterator();
                        while(it.hasNext()) {
                            if(it.next().isInvalid()) {
                                it.remove();
                            }
                        }
                    }
                }
            } else {
                semiBlocks.clear();
            }
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event){
        if(!event.world.isRemote) {
            syncWithPlayers(event.world);
        }
    }

    private void syncWithPlayers(World world){
        List<EntityPlayerMP> players = world.playerEntities;
        for(Map.Entry<Chunk, Set<EntityPlayerMP>> entry : syncList.entrySet()) {
            Chunk chunk = entry.getKey();
            Set<EntityPlayerMP> syncedPlayers = entry.getValue();
            int chunkX = chunk.xPosition * 16 - 8;
            int chunkZ = chunk.zPosition * 16 - 8;
            for(EntityPlayerMP player : players) {
                if(chunk.worldObj == world) {
                    double dist = PneumaticCraftUtils.distBetween(player.posX, 0, player.posZ, chunkX, 0, chunkZ);
                    if(dist < SYNC_DISTANCE) {
                        if(syncedPlayers.add(player)) {
                            for(ISemiBlock semiBlock : semiBlocks.get(chunk).values()) {
                                if(!semiBlock.isInvalid()) {
                                    NetworkHandler.sendTo(new PacketSetSemiBlock(semiBlock), player);
                                    PacketDescription descPacket = semiBlock.getDescriptionPacket();
                                    if(descPacket != null) NetworkHandler.sendTo(descPacket, player);
                                }
                            }
                        }
                    } else if(dist > SYNC_DISTANCE + 5) {
                        syncedPlayers.remove(player);
                    }
                } else {
                    syncedPlayers.remove(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onInteraction(PlayerInteractEvent event){
        if(!event.world.isRemote && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            ItemStack curItem = event.entityPlayer.getCurrentEquippedItem();
            if(curItem != null && curItem.getItem() instanceof ISemiBlockItem) {
                if(getSemiBlock(event.world, event.x, event.y, event.z) != null) {
                    if(event.entityPlayer.capabilities.isCreativeMode) {
                        setSemiBlock(event.world, event.x, event.y, event.z, null);
                    } else {
                        breakSemiBlock(event.world, event.x, event.y, event.z, event.entityPlayer);
                    }
                    event.setCanceled(true);
                } else {
                    ISemiBlock newBlock = ((ISemiBlockItem)curItem.getItem()).getSemiBlock(event.world, event.x, event.y, event.z, curItem);
                    newBlock.initialize(event.world, new ChunkPosition(event.x, event.y, event.z));
                    if(newBlock.canPlace()) {
                        setSemiBlock(event.world, event.x, event.y, event.z, newBlock);
                        newBlock.onPlaced(event.entityPlayer, curItem);
                        event.world.playSoundEffect(event.x + 0.5, event.y + 0.5, event.z + 0.5, Block.soundTypeGlass.func_150496_b(), (Block.soundTypeGlass.getVolume() + 1.0F) / 2.0F, Block.soundTypeGlass.getPitch() * 0.8F);
                        if(!event.entityPlayer.capabilities.isCreativeMode) {
                            curItem.stackSize--;
                            if(curItem.stackSize <= 0) event.entityPlayer.setCurrentItemOrArmor(0, null);
                        }
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private Map<ChunkPosition, ISemiBlock> getOrCreateMap(Chunk chunk){
        Map<ChunkPosition, ISemiBlock> map = semiBlocks.get(chunk);
        if(map == null) {
            map = new HashMap<ChunkPosition, ISemiBlock>();
            semiBlocks.put(chunk, map);
            syncList.put(chunk, new HashSet<EntityPlayerMP>());
        }
        return map;
    }

    public void breakSemiBlock(World world, int x, int y, int z){
        breakSemiBlock(world, x, y, z, null);
    }

    public void breakSemiBlock(World world, int x, int y, int z, EntityPlayer player){
        ISemiBlock semiBlock = getSemiBlock(world, x, y, z);
        if(semiBlock != null) {
            List<ItemStack> drops = new ArrayList<ItemStack>();
            semiBlock.addDrops(drops);
            for(ItemStack stack : drops) {
                EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack);
                world.spawnEntityInWorld(item);
                if(player != null) item.onCollideWithPlayer(player);
            }
            setSemiBlock(world, x, y, z, null);
        }
    }

    public void setSemiBlock(World world, int x, int y, int z, ISemiBlock semiBlock){
        setSemiBlock(world, x, y, z, semiBlock, world.getChunkFromBlockCoords(x, z));
    }

    private void setSemiBlock(World world, int x, int y, int z, ISemiBlock semiBlock, Chunk chunk){
        if(semiBlock != null && !registeredTypes.containsValue(semiBlock.getClass())) throw new IllegalStateException("ISemiBlock \"" + semiBlock + "\" was not registered!");
        ChunkPosition pos = new ChunkPosition(x, y, z);
        if(semiBlock != null) {
            semiBlock.initialize(world, pos);
            addingBlocks.add(semiBlock);
        } else {
            ISemiBlock removedBlock = getOrCreateMap(chunk).get(pos);
            if(removedBlock != null) {
                removedBlock.invalidate();
                for(EntityPlayerMP player : syncList.get(chunk)) {
                    NetworkHandler.sendTo(new PacketSetSemiBlock(pos, null), player);
                }
            }
        }
        chunk.setChunkModified();
    }

    public ISemiBlock getSemiBlock(World world, int x, int y, int z){
        for(ISemiBlock semiBlock : addingBlocks) {
            if(semiBlock.getWorld() == world && semiBlock.getPos().equals(new ChunkPosition(x, y, z))) return semiBlock;
        }

        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        Map<ChunkPosition, ISemiBlock> map = semiBlocks.get(chunk);
        if(map != null) {
            return map.get(new ChunkPosition(x, y, z));
        } else {
            return null;
        }
    }

    public Map<Chunk, Map<ChunkPosition, ISemiBlock>> getSemiBlocks(){
        return semiBlocks;
    }
}
