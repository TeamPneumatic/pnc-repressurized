package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class HackableDispenser implements IHackableBlock {

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
        curInfo.add("pneumaticHelmet.hacking.result.dispense");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.dispensed");
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 40;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        state.tick(world, pos, player.getRNG());
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        return false;
    }

}
