package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class HackableDoor implements IHackableBlock {
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
        if (!world.getBlockState(pos).get(DoorBlock.OPEN)) {
            curInfo.add("pneumaticHelmet.hacking.result.open");
        } else {
            curInfo.add("pneumaticHelmet.hacking.result.close");
        }
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        if (!world.getBlockState(pos).get(DoorBlock.OPEN)) {
            curInfo.add("pneumaticHelmet.hacking.finished.closed");
        } else {
            curInfo.add("pneumaticHelmet.hacking.finished.opened");
        }
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 20;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        state.onBlockActivated(world, player, Hand.MAIN_HAND, null);
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        return false;
    }

}
