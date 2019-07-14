package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class HackableSecurityStation implements IHackableBlock {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean canHack(IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntitySecurityStation te = (TileEntitySecurityStation) world.getTileEntity(pos);
        return !te.doesAllowPlayer(player);
    }

    @Override
    public void addInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.result.access");
    }

    @Override
    public void addPostHackInfo(World world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticHelmet.hacking.finished.accessed");
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 100;
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
