package me.desht.pneumaticcraft.common.hacking.block;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HackableTripwire implements IHackableBlock {
    @Override
    public ResourceLocation getHackableId() {
        return RL("tripwire");
    }

    @Override
    public void addInfo(IBlockReader world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.result.neutralize");
    }

    @Override
    public void addPostHackInfo(IBlockReader world, BlockPos pos, List<String> curInfo, PlayerEntity player) {
        curInfo.add("pneumaticcraft.armor.hacking.finished.neutralized");
    }

    @Override
    public int getHackTime(IBlockReader world, BlockPos pos, PlayerEntity player) {
        return 40;
    }

    @Override
    public void onHackComplete(World world, BlockPos pos, PlayerEntity player) {
        world.destroyBlock(pos, true);
    }
}
