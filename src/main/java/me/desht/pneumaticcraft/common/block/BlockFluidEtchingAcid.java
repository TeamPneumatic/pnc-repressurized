package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockFluidEtchingAcid extends BlockFluidPneumaticCraft {

    public BlockFluidEtchingAcid(Fluid fluid) {
        super(fluid, new MaterialLiquid(MapColor.EMERALD) {
            @Override
            public EnumPushReaction getMobilityFlag() {
                return EnumPushReaction.DESTROY;
            }
        });
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityLivingBase && entity.ticksExisted % 10 == 0) {
            entity.attackEntityFrom(DamageSourcePneumaticCraft.ETCHING_ACID, 1);
        }
    }

}
