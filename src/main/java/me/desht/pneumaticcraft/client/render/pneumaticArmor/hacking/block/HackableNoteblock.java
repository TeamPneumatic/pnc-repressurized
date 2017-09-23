package me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class HackableNoteblock implements IHackableBlock {
    @Override
    public String getId() {
        return "noteBlock";
    }

    @Override
    public boolean canHack(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.result.makeSound");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, EntityPlayer player) {
        curInfo.add("pneumaticHelmet.hacking.finished.makingSound");
    }

    @Override
    public int getHackTime(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return 60;
    }

    @Override
    public void onHackFinished(World world, BlockPos pos, EntityPlayer player) {
    }

    @Override
    public boolean afterHackTick(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        state.getBlock().onBlockActivated(world, pos, state, null, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);
        return true;
    }

}
