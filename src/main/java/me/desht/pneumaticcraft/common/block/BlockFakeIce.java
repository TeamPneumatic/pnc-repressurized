package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BreakableBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFakeIce extends BreakableBlock {
    public BlockFakeIce() {
        super(Block.Properties.from(Blocks.ICE).doesNotBlockMovement());
        setRegistryName("fake_ice");
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityCollision(state, worldIn, pos, entityIn);

        if (entityIn instanceof LivingEntity) {
            ((LivingEntity) entityIn).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 40, 10));
        }
        if (worldIn.rand.nextInt(5) == 0) {
            double amount = PNCConfig.Common.Minigun.freezingAmmoFakeIceDamage;
            if (entityIn.isImmuneToFire()) amount *= 1.5;
            entityIn.attackEntityFrom(DamageSourcePneumaticCraft.FREEZING, (float) amount);
        }
    }
}
