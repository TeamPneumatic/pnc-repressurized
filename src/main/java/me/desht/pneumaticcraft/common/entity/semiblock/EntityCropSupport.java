package me.desht.pneumaticcraft.common.entity.semiblock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import static me.desht.pneumaticcraft.common.config.PNCConfig.Common.Machines.cropSticksGrowthBoostChance;

public class EntityCropSupport extends EntitySemiblockBase {
    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(3 / 16D, 0D, 3 / 16D, 13 / 16D, 9 / 16D, 13 / 16D);

    public EntityCropSupport(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public AxisAlignedBB getBlockBounds() {
        return BOUNDS;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.rand.nextDouble() < cropSticksGrowthBoostChance && !getBlockState().isAir(world, getBlockPos())) {
            if (!world.isRemote) {
                getBlockState().tick(world, getBlockPos(), world.rand);
            } else {
                world.addParticle(ParticleTypes.HAPPY_VILLAGER, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean canPlace(Direction facing) {
        BlockState state = getBlockState();
        return (state.getBlock().isAir(state, world, getBlockPos()) || state.getBlock() instanceof IPlantable) && canStay();
    }

    @Override
    public boolean canStay() {
        BlockState state = getBlockState();
        if (!state.getBlock().isAir(state, world, getBlockPos())) {
            return true;
        }

        BlockPos posBelow = getBlockPos().offset(Direction.DOWN);
        BlockState stateBelow = world.getBlockState(posBelow);
        return !stateBelow.getBlock().isAir(stateBelow, world, posBelow);
    }
}
