package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class HackableTNT implements IHackableBlock {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.explode");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.exploded");
    }

    @Override
    public int getHackTime(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return 100;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, EntityPlayer player) {
        if (!world.isRemote) {
            world.setBlockToAir(pos);
            EntityTNTPrimed tnt = new EntityTNTPrimed(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, player);
            tnt.setFuse(1);
            world.spawnEntity(tnt);
        }
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        return false;
    }

}