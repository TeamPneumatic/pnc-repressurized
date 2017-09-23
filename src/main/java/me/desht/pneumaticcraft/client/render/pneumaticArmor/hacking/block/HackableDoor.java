package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class HackableDoor implements IHackableBlock {
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
        if (!world.getBlockState(pos).getValue(BlockDoor.OPEN)) {
            curInfo.add("pneumaticHelmet.hacking.result.open");
        } else {
            curInfo.add("pneumaticHelmet.hacking.result.close");
        }
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        if (!world.getBlockState(pos).getValue(BlockDoor.OPEN)) {
            curInfo.add("pneumaticHelmet.hacking.finished.closed");
        } else {
            curInfo.add("pneumaticHelmet.hacking.finished.opened");
        }
    }

    @Override
    public int getHackTime(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return 20;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, EntityPlayer player) {
        IBlockState state = world.getBlockState(pos);
        state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        return false;
    }

}
