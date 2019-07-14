package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class HackableTNT implements IHackableBlock {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    @Override
    public void addInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.explode");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.exploded");
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 100;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            world.removeBlock(pos, false);
            TNTEntity tnt = new TNTEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, player);
            tnt.setFuse(1);
            world.addEntity(tnt);
        }
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        return false;
    }

}