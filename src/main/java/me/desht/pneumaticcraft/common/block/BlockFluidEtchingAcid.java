package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockFluidEtchingAcid extends BlockFluidPneumaticCraft {

    public BlockFluidEtchingAcid(Fluid fluid) {
        super(fluid, new MaterialLiquid(MaterialColor.EMERALD) {
            @Override
            public PushReaction getPushReaction() {
                return PushReaction.DESTROY;
            }
        });
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity && entity.ticksExisted % 10 == 0) {
            entity.attackEntityFrom(DamageSourcePneumaticCraft.ETCHING_ACID, 1);
        }
    }

}
