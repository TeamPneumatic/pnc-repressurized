package me.desht.pneumaticcraft.common.entity.semiblock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;

import static me.desht.pneumaticcraft.common.config.PNCConfig.Common.Machines.cropSticksGrowthBoostChance;

public class EntityCropSupport extends EntitySemiblockBase {
    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(3 / 16D, 0D, 3 / 16D, 13 / 16D, 9 / 16D, 13 / 16D);

    public EntityCropSupport(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected AxisAlignedBB calculateBlockBounds() {
        return BOUNDS;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.rand.nextDouble() < cropSticksGrowthBoostChance && !getBlockState().isAir(world, getBlockPos())) {
            if (!world.isRemote) {
                getBlockState().tick((ServerWorld) world, getBlockPos(), world.rand);
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

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d hitVec, Hand hand) {
        BlockState state = getBlockState();
        if (state.getBlock().isAir(state, world, getBlockPos())) {
            // try a right click on the block below - makes it easier to plant crops in an empty crop support
            BlockPos below = getBlockPos().down();
            Vector3d eye = player.getEyePosition(0f);
            Vector3d end = Vector3d.copyCentered(below).add(0, 0.25, 0);
            RayTraceContext ctx = new RayTraceContext(eye, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player);
            BlockRayTraceResult brtr = player.world.rayTraceBlocks(ctx);
            if (brtr.getType() == RayTraceResult.Type.BLOCK && brtr.getPos().equals(below)) {
                return player.getHeldItem(hand).onItemUse(new ItemUseContext(player, hand, brtr));
            }
        }
        return super.applyPlayerInteraction(player, hitVec, hand);
    }
}
