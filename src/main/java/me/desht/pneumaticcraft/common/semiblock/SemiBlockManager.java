package me.desht.pneumaticcraft.common.semiblock;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Streams;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAddSemiBlock;
import me.desht.pneumaticcraft.common.network.PacketDescription;
import me.desht.pneumaticcraft.common.network.PacketRemoveSemiBlock;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.StreamUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.SoundType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public class SemiBlockManager {
    private final Map<Chunk, Map<BlockPos, List<ISemiBlock>>> semiBlocks = new HashMap<>();
    private final List<ISemiBlock> addingBlocks = new LinkedList<>();
    private final Map<Chunk, Set<EntityPlayer>> syncList = new HashMap<>();
    private final Set<Chunk> chunksMarkedForRemoval = new HashSet<>();
    private static final int SYNC_DISTANCE_SQ = 64 * 64;
    private static final int SYNC_DISTANCE_SQ5 = 69 * 69;
    private static final int SYNC_DISTANCE_SQ10 = 74 * 74;
    private static final HashBiMap<String, Class<? extends ISemiBlock>> registeredTypes = HashBiMap.create();
    private static final HashBiMap<Class<? extends ISemiBlock>, ItemSemiBlockBase> semiBlockToItems = HashBiMap.create();
    private static final SemiBlockManager INSTANCE = new SemiBlockManager();
    private static final SemiBlockManager CLIENT_INSTANCE = new SemiBlockManager();

    private static SemiBlockManager getServerInstance() {
        return INSTANCE;
    }

    public static void registerEventHandler(boolean isClient) {
        MinecraftForge.EVENT_BUS.register(isClient ? CLIENT_INSTANCE : INSTANCE);
    }

    private static SemiBlockManager getClientOldInstance() {
        return CLIENT_INSTANCE;
    }

    public static SemiBlockManager getInstance(World world) {
        return world.isRemote ? CLIENT_INSTANCE : INSTANCE;
    }

    static void registerSemiBlock(String key, Class<? extends ISemiBlock> semiBlock) {
        registerSemiBlock(key, semiBlock, ItemSemiBlockBase.class);
    }

    static void registerSemiBlock(String key, Class<? extends ISemiBlock> semiBlock, Class<? extends ItemSemiBlockBase> itemClass) {
        // called in preInit phase
        Validate.isTrue(!registeredTypes.containsKey(key), "Duplicate registration key: " + key);
        registeredTypes.put(key, semiBlock);

        // stash the item objects for registration when the registry event is received in onItemRegistration()
        try {
            Constructor<? extends ItemSemiBlockBase> ctor = itemClass.getDeclaredConstructor(String.class);
            semiBlockToItems.put(semiBlock, itemClass.cast(ctor.newInstance(key)));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            Log.error("Failed to register semiblock " + key + " of class " + semiBlock.getCanonicalName());
            e.printStackTrace();
        }
    }

    static ItemSemiBlockBase getItemForSemiBlock(ISemiBlock semiBlock) {
        return semiBlockToItems.get(semiBlock.getClass());
    }

    public static Class<? extends ISemiBlock> getSemiBlockForItem(ItemSemiBlockBase item) {
        return semiBlockToItems.inverse().get(item);
    }

    public static String getKeyForSemiBlock(ISemiBlock semiBlock) {
        return getKeyForSemiBlock(semiBlock.getClass());
    }

    public static String getKeyForSemiBlock(Class<? extends ISemiBlock> semiBlock) {
        return registeredTypes.inverse().get(semiBlock);
    }

    public static ISemiBlock getSemiBlockForKey(String key) {
        try {
            Class<? extends ISemiBlock> clazz = registeredTypes.get(key);
            if (clazz != null) {
                return clazz.newInstance();
            } else {
                Log.warning("Semi Block with id \"" + key + "\" isn't registered!");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        for (ItemSemiBlockBase item : semiBlockToItems.values()) {
            Itemss.registerItem(event.getRegistry(), item);
        }
    }

    @SubscribeEvent
    public void onChunkUnLoad(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            chunksMarkedForRemoval.add(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        Map<BlockPos, List<ISemiBlock>> map = semiBlocks.get(event.getChunk());
        if (map != null && map.size() > 0) {
            NBTTagList tagList = new NBTTagList();
            for (Map.Entry<BlockPos, List<ISemiBlock>> entry : map.entrySet()) {
                for(ISemiBlock semiBlock : entry.getValue()){
                    NBTTagCompound t = new NBTTagCompound();
                    semiBlock.writeToNBT(t);
                    NBTUtil.setPos(t, entry.getKey());
                    t.setString("type", getKeyForSemiBlock(semiBlock));
                    tagList.appendTag(t);
                }
            }
            event.getData().setTag("SemiBlocks", tagList);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        try {
            if (!event.getWorld().isRemote) {
                if (event.getData().hasKey("SemiBlocks")) {
                    //Posting on the queue because of suspicion of mods off-thread loading chunks https://github.com/TeamPneumatic/pnc-repressurized/issues/234
                    PneumaticCraftRepressurized.proxy.addScheduledTask(() -> {
                        Map<BlockPos, List<ISemiBlock>> map = getOrCreateMap(event.getChunk());
                        map.clear();
                        NBTTagList tagList = event.getData().getTagList("SemiBlocks", 10);
                        for (int i = 0; i < tagList.tagCount(); i++) {
                            NBTTagCompound t = tagList.getCompoundTagAt(i);
                            ISemiBlock semiBlock = getSemiBlockForKey(t.getString("type"));
                            if (semiBlock != null) {
                                semiBlock.readFromNBT(t);
                                addSemiBlock(event.getWorld(), NBTUtil.getPos(t), semiBlock, event.getChunk());
                            }
                        }
                    }, true);                    
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        for (ISemiBlock semiBlock : addingBlocks) {
            Chunk chunk = semiBlock.getWorld().getChunkFromBlockCoords(semiBlock.getPos());
            addPendingBlock(chunk, semiBlock);
            chunk.markDirty();

            for (EntityPlayer player : syncList.get(chunk)) {
                NetworkHandler.sendTo(new PacketAddSemiBlock(semiBlock), (EntityPlayerMP) player);
                PacketDescription descPacket = semiBlock.getDescriptionPacket();
                if (descPacket != null) NetworkHandler.sendTo(descPacket, (EntityPlayerMP) player);
            }
        }
        addingBlocks.clear();

        for (Chunk removingChunk : chunksMarkedForRemoval) {
            if (!removingChunk.isLoaded()) {
                semiBlocks.remove(removingChunk);
                syncList.remove(removingChunk);
            }
        }
        chunksMarkedForRemoval.clear();

        semiBlocks.values().forEach(this::update);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        if (this == getServerInstance()) {
            getClientOldInstance().onClientTick(event);
        } else {
            EntityPlayer player = PneumaticCraftRepressurized.proxy.getClientPlayer();
            if (player != null) {
                for (Iterator<ISemiBlock> iterator = addingBlocks.iterator(); iterator.hasNext(); ) {
                    // on the client, we can't assume the chunk is actually available yet; if we get an empty
                    // chunk for the given blockpos, don't add the semiblock but leave it in the pending list
                    // and try again next tick
                    ISemiBlock semiBlock = iterator.next();
                    Chunk chunk = semiBlock.getWorld().getChunkFromBlockCoords(semiBlock.getPos());
                    if (!chunk.isEmpty()) {
                        addPendingBlock(chunk, semiBlock);
                        iterator.remove();
                    }
                }

                Iterator<Map.Entry<Chunk, Map<BlockPos, List<ISemiBlock>>>> iterator = semiBlocks.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Chunk, Map<BlockPos, List<ISemiBlock>>> entry = iterator.next();
                    if (PneumaticCraftUtils.distBetweenSq(player.posX, 0, player.posZ, entry.getKey().x * 16 - 8, 0, entry.getKey().z * 16 - 8) > SYNC_DISTANCE_SQ10) {
                        iterator.remove();
                    } else {
                        update(entry.getValue());
                    }
                }
            } else {
                semiBlocks.clear();
            }
        }
    }

    private void addPendingBlock(Chunk chunk, ISemiBlock semiBlock){
        Map<BlockPos, List<ISemiBlock>> map = getOrCreateMap(chunk);
        List<ISemiBlock> semiBlocksForPos = map.computeIfAbsent(semiBlock.getPos(), k -> new ArrayList<>());
        for (int i = 0; i < semiBlocksForPos.size(); i++) {
            // can't have multiple semiblocks of the same type; replace any such existing semiblock
            if (semiBlocksForPos.get(i).getClass() == semiBlock.getClass()) {
                semiBlocksForPos.set(i, semiBlock);
                return;
            }
        }
        semiBlocksForPos.add(semiBlock);
    }
    
    private void update(Map<BlockPos, List<ISemiBlock>> map){
        for (List<ISemiBlock> semiBlocks : map.values()) {
            for(ISemiBlock semiBlock : semiBlocks){
                if (!semiBlock.isInvalid()) semiBlock.update();
            }
            semiBlocks.removeIf(ISemiBlock::isInvalid);
        }
        map.values().removeIf(List::isEmpty);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            syncWithPlayers(event.world);
        }
    }

    private void syncWithPlayers(World world) {
        List<EntityPlayer> players = world.playerEntities;
        for (Map.Entry<Chunk, Set<EntityPlayer>> entry : syncList.entrySet()) {
            Chunk chunk = entry.getKey();
            Set<EntityPlayer> syncedPlayers = entry.getValue();
            int chunkX = chunk.x * 16 - 8;
            int chunkZ = chunk.z * 16 - 8;
            for (EntityPlayer player : players) {
                if (chunk.getWorld() == world) {
                    double dist = PneumaticCraftUtils.distBetweenSq(player.posX, 0, player.posZ, chunkX, 0, chunkZ);
                    if (dist < SYNC_DISTANCE_SQ) {
                        if (syncedPlayers.add(player)) {
                            for(List<ISemiBlock> semiBlockList : semiBlocks.get(chunk).values()){
                                for (ISemiBlock semiBlock : semiBlockList) {
                                    if (!semiBlock.isInvalid()) {
                                        NetworkHandler.sendTo(new PacketAddSemiBlock(semiBlock), (EntityPlayerMP) player);
                                        PacketDescription descPacket = semiBlock.getDescriptionPacket();
                                        if (descPacket != null) NetworkHandler.sendTo(descPacket, (EntityPlayerMP) player);
                                    }
                                }
                            }
                        }
                    } else if (dist > SYNC_DISTANCE_SQ5) {
                        syncedPlayers.remove(player);
                    }
                } else {
                    syncedPlayers.remove(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onInteraction(PlayerInteractEvent.RightClickBlock event) {
        ItemStack curItem = event.getEntityPlayer().getHeldItemMainhand();
        if (!event.getWorld().isRemote && event.getHand() == EnumHand.MAIN_HAND) {
            if (curItem.getItem() instanceof ISemiBlockItem) {
                boolean success = interact(event, curItem, event.getPos());
                
                //If the block can't be placed in the pos, then try to place it next to the block.
                if (!success && event.getFace() != null)
                    success = interact(event, curItem, event.getPos().offset(event.getFace()));

                // Still can't be placed? If it has a GUI, open it.
                if (!success) {
                    ISemiBlock block = ((ISemiBlockItem) curItem.getItem()).getSemiBlock(event.getWorld(), null, curItem);
                    if (block.getGuiID() != null) {
                        event.getEntityPlayer().openGui(PneumaticCraftRepressurized.instance, block.getGuiID().ordinal(), event.getWorld(),
                                event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
                        success = true;
                    }
                }
                if (success) event.setCanceled(true);

            }
        } else if (event.getWorld().isRemote && curItem.getItem() instanceof ISemiBlockItem) {
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private boolean interact(PlayerInteractEvent.RightClickBlock event, ItemStack curItem, BlockPos pos){
        ISemiBlock newBlock = ((ISemiBlockItem) curItem.getItem()).getSemiBlock(event.getWorld(), pos, curItem);
        newBlock.initialize(event.getWorld(), pos);
        newBlock.prePlacement(event.getEntityPlayer(), curItem, event.getFace());
        
        Stream<ISemiBlock> existingSemiblocks = getSemiBlocks(event.getWorld(), pos);
        List<ISemiBlock> collidingBlocks = existingSemiblocks.filter(s -> !s.canCoexistInSameBlock(newBlock)).collect(Collectors.toList());
        
        if (!collidingBlocks.isEmpty()) {
            for(ISemiBlock collidingBlock : collidingBlocks){
                if (event.getEntityPlayer().capabilities.isCreativeMode) {
                    removeSemiBlock(collidingBlock);
                } else {
                    breakSemiBlock(collidingBlock, event.getEntityPlayer());
                }
            }
            
            return true;
        } else {            
            if (newBlock.canPlace(event.getFace())) {
                addSemiBlock(event.getWorld(), pos, newBlock);
                newBlock.onPlaced(event.getEntityPlayer(), curItem, event.getFace());
                event.getWorld().playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundType.GLASS.getPlaceSound(), SoundCategory.BLOCKS,
                        (SoundType.GLASS.getVolume() + 1.0F) / 2.0F, SoundType.GLASS.getPitch() * 0.8F,
                        false);
                if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                    curItem.shrink(1);
                    if (curItem.getCount() <= 0) event.getEntityPlayer().setHeldItem(event.getHand(), ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    private Map<BlockPos, List<ISemiBlock>> getOrCreateMap(Chunk chunk) {
        Map<BlockPos, List<ISemiBlock>> map = semiBlocks.get(chunk);
        if (map == null) {
            map = new HashMap<>();
            semiBlocks.put(chunk, map);
            syncList.put(chunk, new HashSet<>());
        }
        return map;
    }

    void breakSemiBlock(ISemiBlock semiBlock) {
        breakSemiBlock(semiBlock, null);
    }

    public void breakSemiBlock(ISemiBlock semiBlock, EntityPlayer player) {
        if (!semiBlock.isInvalid()) {
            World world = semiBlock.getWorld();
            BlockPos pos = semiBlock.getPos();
            NonNullList<ItemStack> drops = NonNullList.create();
            semiBlock.addDrops(drops);
            for (ItemStack stack : drops) {
                EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                world.spawnEntity(item);
                if (player != null) item.onCollideWithPlayer(player);
            }
            removeSemiBlock(semiBlock);
        }
    }
    
    public void removeSemiBlock(ISemiBlock semiBlock){
        Validate.notNull(semiBlock);
        
        int index = semiBlock.getIndex();
        semiBlock.invalidate();
        
        //Notify other semi blocks in the same pos
        World world = semiBlock.getWorld();
        BlockPos pos = semiBlock.getPos();
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        List<ISemiBlock> currentBlocks = getOrCreateMap(chunk).get(pos);
        currentBlocks.forEach(s -> s.onSemiBlockRemovedFromThisPos(semiBlock));
        for (EntityPlayer player : syncList.get(chunk)) {
            NetworkHandler.sendTo(new PacketRemoveSemiBlock(semiBlock, index), (EntityPlayerMP) player);
        }
        chunk.markDirty();
    }

    public void addSemiBlock(World world, BlockPos pos, ISemiBlock semiBlock) {
        addSemiBlock(world, pos, semiBlock, world.getChunkFromBlockCoords(pos));
    }

    /**
     * Queue an addition of a semi block. Don't do it immediately, because CME's.
     * @param world the world
     * @param pos blockpos to add semiblock at
     * @param semiBlock the semiblock to add
     * @param chunk the chunk that the blockpos is in
     */
    private void addSemiBlock(World world, BlockPos pos, ISemiBlock semiBlock, Chunk chunk) {
        Validate.notNull(semiBlock);
        if (!registeredTypes.containsValue(semiBlock.getClass()))
            throw new IllegalStateException("ISemiBlock \"" + semiBlock + "\" was not registered!");

        semiBlock.initialize(world, pos);
        addingBlocks.add(semiBlock);

        chunk.markDirty();
    }
    
    public <T extends ISemiBlock> T getSemiBlock(Class<T> clazz, World world, BlockPos pos){
        return getSemiBlocks(clazz, world, pos).findFirst().orElse(null);
    }
    
    <T extends ISemiBlock> Stream<T> getSemiBlocks(Class<T> clazz, World world, BlockPos pos) {
        return StreamUtils.ofType(clazz, getSemiBlocks(world, pos));
    }
    
    public List<ISemiBlock> getSemiBlocksAsList(World world, BlockPos pos) {
        return getSemiBlocks(world, pos).collect(Collectors.toList());
    }
    
    public <T extends ISemiBlock> List<T> getSemiBlocksAsList(Class<T> clazz, World world, BlockPos pos) {
        return getSemiBlocks(clazz, world, pos).collect(Collectors.toList());
    }

    public Stream<ISemiBlock> getSemiBlocks(World world, BlockPos pos) {
        Stream<ISemiBlock> stream = null;
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        Map<BlockPos, List<ISemiBlock>> map = semiBlocks.get(chunk);
        if (map != null) {
            List<ISemiBlock> semiblocks = map.get(pos);
            if(semiblocks != null){ 
                stream = semiblocks.stream().filter(semiBlock -> !semiBlock.isInvalid());
            }
        }

        // Semiblocks that _just_ have been added, but not the the chunk maps yet.
        Stream<ISemiBlock> addingStream = addingBlocks.stream()
                .filter(semiBlock -> semiBlock.getWorld() == world &&
                        semiBlock.getPos().equals(pos) &&
                        !semiBlock.isInvalid());
        if (stream == null) {
            return addingStream;
        }else{
            return Streams.concat(stream, addingStream);
        }
    }
    
    public Stream<ISemiBlock> getSemiBlocksInArea(World world, AxisAlignedBB aabb){
        List<Chunk> applicableChunks = new ArrayList<>();
        int minX = (int)aabb.minX;
        int minY = (int)aabb.minY;
        int minZ = (int)aabb.minZ;
        int maxX = (int)aabb.maxX;
        int maxY = (int)aabb.maxY;
        int maxZ = (int)aabb.maxZ;
        
        //Get the relevant chunks.
        for (int x = minX; x < maxX + 16; x += 16) {
            for (int z = minZ; z < maxZ + 16; z += 16) {
                Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(x, 0, z));
                applicableChunks.add(chunk);
            }
        }
        
        //Retrieve all semi block storages from the relevant chunks
        Stream<Map<BlockPos, List<ISemiBlock>>> chunkMaps = applicableChunks.stream()
                                                                            .map(chunk -> getSemiBlocks().get(chunk))
                                                                            .filter(Objects::nonNull);
        
        Stream<List<ISemiBlock>> semiBlocksPerPos = chunkMaps.flatMap(map -> map.values().stream());
        Stream<ISemiBlock> existingSemiBlocksInArea = semiBlocksPerPos.flatMap(Collection::stream);
        Stream<ISemiBlock> allSemiBlocksInArea = Streams.concat(existingSemiBlocksInArea, addingBlocks.stream());
        return allSemiBlocksInArea.filter(s -> !s.isInvalid() &&
                                               minX <= s.getPos().getX() && s.getPos().getX() <= maxX &&
                                               minY <= s.getPos().getY() && s.getPos().getY() <= maxY &&
                                               minZ <= s.getPos().getZ() && s.getPos().getZ() <= maxZ);
    }

    public Map<Chunk, Map<BlockPos, List<ISemiBlock>>> getSemiBlocks() {
        return semiBlocks;
    }

    public void clearAll() {
        semiBlocks.clear();
    }
}
