package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackableSilverfish implements IHackableBlock {

    @Override
    public ResourceLocation getHackableId() {
        return RL("silverfish");
    }

    @Override
    public void addInfo(IBlockReader world, BlockPos pos, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.result.neutralize"));
    }

    @Override
    public void addPostHackInfo(IBlockReader world, BlockPos pos, List<ITextComponent> curInfo, PlayerEntity player) {
        curInfo.add(xlate("pneumaticcraft.armor.hacking.finished.neutralized"));
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 40;
    }

    @Override
    public void onHackComplete(World world, BlockPos pos, PlayerEntity player) {
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof SilverfishBlock) {
            Block newBlock = ((SilverfishBlock) state.getBlock()).getMimickedBlock();
            world.setBlockState(pos, newBlock.getDefaultState(), 3);
        }
    }
}
