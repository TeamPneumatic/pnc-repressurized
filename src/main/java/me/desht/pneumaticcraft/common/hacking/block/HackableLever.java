package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HackableLever implements IHackableBlock {
    @Override
    public ResourceLocation getHackableId() {
        return RL("lever");
    }

    @Override
    public void addInfo(IBlockReader world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        if (world.getBlockState(pos).get(LeverBlock.POWERED)) {
            curInfo.add("pneumaticcraft.armor.hacking.result.deactivate");
        } else {
            curInfo.add("pneumaticcraft.armor.hacking.result.activate");
        }
    }

    @Override
    public void addPostHackInfo(IBlockReader world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        if (world.getBlockState(pos).get(LeverBlock.POWERED)) {
            curInfo.add("pneumaticcraft.armor.hacking.finished.activated");
        } else {
            curInfo.add("pneumaticcraft.armor.hacking.finished.deactivated");
        }
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 20;
    }

    @Override
    public void onHackComplete(World world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        fakeRayTrace(player, pos).ifPresent(rtr -> state.onBlockActivated(world, player, Hand.MAIN_HAND, rtr));
    }
}
