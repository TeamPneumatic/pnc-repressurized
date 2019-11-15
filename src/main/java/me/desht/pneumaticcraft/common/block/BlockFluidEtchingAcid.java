package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class BlockFluidEtchingAcid extends BlockFluidPneumaticCraft {

    public BlockFluidEtchingAcid(Supplier<? extends FlowingFluid> supplier, Properties props) {
        super(supplier, props, "etching_acid_block");
    }

//    public BlockFluidEtchingAcid(Fluid fluid) {
//        super(fluid, new MaterialLiquid(MaterialColor.EMERALD) {
//            @Override
//            public PushReaction getPushReaction() {
//                return PushReaction.DESTROY;
//            }
//        });
//    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && entity.ticksExisted % 10 == 0) {
            entity.attackEntityFrom(DamageSourcePneumaticCraft.ETCHING_ACID, 1);
        }
    }
}
