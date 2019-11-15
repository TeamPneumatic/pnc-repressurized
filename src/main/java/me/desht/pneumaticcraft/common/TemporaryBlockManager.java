package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber
public class TemporaryBlockManager extends WorldSavedData {
    private static final String DATA_KEY = "PneumaticCraftTempBlocks";

    private final List<TempBlockRecord> tempBlocks = new ArrayList<>();

    private TemporaryBlockManager() {
        super(DATA_KEY);
    }

    public static TemporaryBlockManager getManager(World world) {
        return ((ServerWorld) world).getSavedData().getOrCreate(TemporaryBlockManager::new, DATA_KEY);
    }

    @SubscribeEvent
    public static void tickAll(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START || !(event.world instanceof ServerWorld)) return;

        ServerWorld world = (ServerWorld) event.world;
        TemporaryBlockManager mgr = world.getSavedData().get(TemporaryBlockManager::new, DATA_KEY);
        if (mgr != null) {
            Iterator<TempBlockRecord> iter = mgr.tempBlocks.iterator();
            while (iter.hasNext()) {
                TempBlockRecord rec = iter.next();
                if (world.isAreaLoaded(rec.pos, 0) && rec.endTime <= world.getGameTime()) {
                    if (world.getBlockState(rec.pos) == rec.tempState) {
                        world.playEvent(2001, rec.pos, Block.getStateId(rec.tempState));
                        world.setBlockState(rec.pos, rec.prevState);
                    }
                    iter.remove();
                }
            }
        }
    }

    public void setBlock(World world, BlockPos pos, BlockState state, int durationTicks) {
        if (world != null) {
            BlockState prevState = world.getBlockState(pos);
            world.setBlockState(pos, state);
            tempBlocks.add(new TempBlockRecord(pos, prevState, state, world.getGameTime() + durationTicks));
        }
    }

    public boolean trySetBlock(World world, PlayerEntity player, Direction face, BlockPos pos, BlockState state, int durationTicks) {
        if (world != null) {
            BlockState prevState = world.getBlockState(pos);
            if (PneumaticCraftUtils.tryPlaceBlock(world, pos, player, face, state)) {
                tempBlocks.add(new TempBlockRecord(pos, prevState, state, world.getGameTime() + durationTicks));
                return true;
            }
        }
        return false;
    }

    @Override
    public void read(CompoundNBT nbt) {
        tempBlocks.clear();

        ListNBT list = nbt.getList("tempBlocks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT tag = list.getCompound(i);
            tempBlocks.add(TempBlockRecord.readFromNBT(tag));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (TempBlockRecord record : tempBlocks) {
            CompoundNBT tag = new CompoundNBT();
            record.writeToNBT(tag);
            list.add(tag);
        }
        compound.put("tempBlocks", list);
        return compound;
    }

    private static class TempBlockRecord {
        final BlockPos pos;
        final BlockState tempState;
        final BlockState prevState;
        final long endTime;

        private TempBlockRecord(BlockPos pos, BlockState prevState, BlockState tempState, long endTime) {
            this.pos = pos;
            this.prevState = prevState;
            this.tempState = tempState;
            this.endTime = endTime;
        }

        static TempBlockRecord readFromNBT(CompoundNBT tag) {
            BlockPos pos = NBTUtil.readBlockPos(tag.getCompound("pos"));
            BlockState prevState = NBTUtil.readBlockState(tag.getCompound("prevState"));
            BlockState state = NBTUtil.readBlockState(tag.getCompound("tempState"));
            long endTime = tag.getLong("endTime");
            return new TempBlockRecord(pos, prevState, state, endTime);
        }

        void writeToNBT(CompoundNBT tag) {
            tag.put("pos", NBTUtil.writeBlockPos(pos));
            tag.put("prevState", NBTUtil.writeBlockState(prevState));
            tag.put("tempState", NBTUtil.writeBlockState(tempState));
            tag.putLong("endTime", endTime);
        }
    }
}
