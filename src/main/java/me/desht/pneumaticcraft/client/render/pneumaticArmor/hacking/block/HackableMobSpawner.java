package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class HackableMobSpawner implements IHackableBlock {
    @Override
    public String getId() {
        return "mobSpawner";
    }

    @Override
    public boolean canHack(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return !isHacked(world, pos);
    }

    public static boolean isHacked(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityMobSpawner && Reflections.getActivatingRangeFromPlayer(((TileEntityMobSpawner) te).getSpawnerBaseLogic()) == 0;
    }

    @Override
    public void addInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return 200;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, EntityPlayer player) {
        if (!world.isRemote) {
            NBTTagCompound tag = new NBTTagCompound();
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                te.writeToNBT(tag);
                tag.setShort("RequiredPlayerRange", (short) 0);
                te.readFromNBT(tag);
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }

    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        MobSpawnerBaseLogic spawner = ((TileEntityMobSpawner) world.getTileEntity(pos)).getSpawnerBaseLogic();
        Reflections.setPrevMobRotation(spawner, spawner.getMobRotation());
        Reflections.setSpawnDelay(spawner, 10);
        return false;
    }
}
