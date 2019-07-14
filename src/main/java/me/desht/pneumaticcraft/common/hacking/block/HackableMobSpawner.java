package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

import java.util.List;

public class HackableMobSpawner implements IHackableBlock {
    @Override
    public String getId() {
        return "mobSpawner";
    }

    @Override
    public boolean canHack(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return !isHacked(world, pos);
    }

    public static boolean isHacked(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof MobSpawnerTileEntity && ((MobSpawnerTileEntity) te).getSpawnerBaseLogic().activatingRangeFromPlayer == 0;
    }

    @Override
    public void addInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 200;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            CompoundNBT tag = new CompoundNBT();
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                te.write(tag);
                tag.putShort("RequiredPlayerRange", (short) 0);
                te.read(tag);
                BlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }

    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        AbstractSpawner spawner = ((MobSpawnerTileEntity) world.getTileEntity(pos)).getSpawnerBaseLogic();
        spawner.prevMobRotation = spawner.mobRotation;
        spawner.spawnDelay = 10;
        return false;
    }
}
