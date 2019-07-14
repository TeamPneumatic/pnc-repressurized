package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class HackableLever implements IHackableBlock {
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
        if (!world.getBlockState(pos).get(LeverBlock.POWERED)) {
            curInfo.add("pneumaticHelmet.hacking.result.activate");
        } else {
            curInfo.add("pneumaticHelmet.hacking.result.deactivate");
        }
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        if (!world.getBlockState(pos).get(LeverBlock.POWERED)) {
            curInfo.add("pneumaticHelmet.hacking.finished.deactivated");
        } else {
            curInfo.add("pneumaticHelmet.hacking.finished.activated");
        }
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 20;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        // button's onBlockActivated ignores the BlockRayTraceResult so we can pass null here
        state.onBlockActivated(world, player, Hand.MAIN_HAND, null);
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        return false;
    }

}
