package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFluidEtchingAcid extends FlowingFluidBlock {

    public BlockFluidEtchingAcid(Properties props) {
        super(() -> (FlowingFluid) ModFluids.ETCHING_ACID.get(), props);
    }

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && entity.tickCount % 10 == 0) {
            entity.hurt(DamageSourcePneumaticCraft.ETCHING_ACID, 1);
        }
    }
}
