package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

@Mod.EventBusSubscriber
public class TemporaryBlockManager extends WorldSavedData {
    private static final Map<Integer, TemporaryBlockManager> managers = new HashMap<>();
    private static final String DATA_KEY = "PneumaticCraftTempBlocks";

    private final List<TempBlockRecord> tempBlocks = new ArrayList<>();
    private final int dimId;

    private TemporaryBlockManager(String dataKey, int dimId) {
        super(dataKey);
        this.dimId = dimId;
    }

    public static TemporaryBlockManager getManager(World world) {
        return managers.computeIfAbsent(world.provider.getDimension(), TemporaryBlockManager::create);
    }

    private static TemporaryBlockManager create(int dimId) {
        World world = DimensionManager.getWorld(dimId);
        if (world != null) {
            TemporaryBlockManager mgr = (TemporaryBlockManager) world.loadData(TemporaryBlockManager.class, DATA_KEY);
            if (mgr == null) {
                mgr = new TemporaryBlockManager(DATA_KEY, dimId);
                world.setData(DATA_KEY, mgr);
            }
            return mgr;
        }
        return null;
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        managers.remove(event.getWorld().provider.getDimension());
    }

    @SubscribeEvent
    public static void tickAll(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        for (Map.Entry<Integer,TemporaryBlockManager> entries : managers.entrySet()) {
            World world = DimensionManager.getWorld(entries.getKey());
            if (world != null) {
                Iterator<TempBlockRecord> iter = entries.getValue().tempBlocks.iterator();
                while (iter.hasNext()) {
                    TempBlockRecord rec = iter.next();
                    if (world.isBlockLoaded(rec.pos) && rec.endTime <= world.getTotalWorldTime()) {
                        if (world.getBlockState(rec.pos) == rec.tempState) {
                            world.playEvent(2001, rec.pos, Block.getStateId(rec.tempState));
                            world.setBlockState(rec.pos, rec.prevState);
                        }
                        iter.remove();
                    }
                }
            }
        }
    }

    public void setBlock(BlockPos pos, IBlockState state, int durationTicks) {
        World world = DimensionManager.getWorld(dimId);
        if (world != null) {
            IBlockState prevState = world.getBlockState(pos);
            world.setBlockState(pos, state);
            tempBlocks.add(new TempBlockRecord(pos, prevState, state, world.getTotalWorldTime() + durationTicks));
        }
    }

    public boolean trySetBlock(EntityPlayer player, EnumFacing face, BlockPos pos, IBlockState state, int durationTicks) {
        World world = DimensionManager.getWorld(dimId);
        if (world != null) {
            IBlockState prevState = world.getBlockState(pos);
            if (PneumaticCraftUtils.tryPlaceBlock(world, pos, player, face, state)) {
                tempBlocks.add(new TempBlockRecord(pos, prevState, state, world.getTotalWorldTime() + durationTicks));
                return true;
            }
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        tempBlocks.clear();

        NBTTagList list = nbt.getTagList("tempBlocks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            tempBlocks.add(TempBlockRecord.readFromNBT(tag));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (TempBlockRecord record : tempBlocks) {
            NBTTagCompound tag = new NBTTagCompound();
            record.writeToNBT(tag);
            list.appendTag(tag);
        }
        compound.setTag("tempBlocks", list);
        return compound;
    }

    private static class TempBlockRecord {
        final BlockPos pos;
        final IBlockState tempState;
        final IBlockState prevState;
        final long endTime;

        private TempBlockRecord(BlockPos pos, IBlockState prevState, IBlockState tempState, long endTime) {
            this.pos = pos;
            this.prevState = prevState;
            this.tempState = tempState;
            this.endTime = endTime;
        }

        static TempBlockRecord readFromNBT(NBTTagCompound tag) {
            BlockPos pos = NBTUtil.getPosFromTag(tag.getCompoundTag("pos"));
            IBlockState prevState = NBTUtil.readBlockState(tag.getCompoundTag("prevState"));
            IBlockState state = NBTUtil.readBlockState(tag.getCompoundTag("tempState"));
            long endTime = tag.getLong("endTime");
            return new TempBlockRecord(pos, prevState, state, endTime);
        }

        void writeToNBT(NBTTagCompound tag) {
            tag.setTag("pos", NBTUtil.createPosTag(pos));
            tag.setTag("prevState", NBTUtil.writeBlockState(new NBTTagCompound(), prevState));
            tag.setTag("tempState", NBTUtil.writeBlockState(new NBTTagCompound(), tempState));
            tag.setLong("endTime", endTime);
        }
    }
}
