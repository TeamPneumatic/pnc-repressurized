/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
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

        if (level.random.nextDouble() < ConfigHelper.common().machines.cropSticksGrowthBoostChance.get() && !getBlockState().isAir(level, getBlockPos())) {
            if (!level.isClientSide) {
                getBlockState().tick((ServerWorld) level, getBlockPos(), level.random);
            } else {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean canPlace(Direction facing) {
        BlockState state = getBlockState();
        return (state.getBlock().isAir(state, level, getBlockPos()) || state.getBlock() instanceof IPlantable) && canStay();
    }

    @Override
    public boolean canStay() {
        BlockState state = getBlockState();
        if (!state.getBlock().isAir(state, level, getBlockPos())) {
            return true;
        }

        BlockPos posBelow = getBlockPos().relative(Direction.DOWN);
        BlockState stateBelow = level.getBlockState(posBelow);
        return !stateBelow.getBlock().isAir(stateBelow, level, posBelow);
    }

    @Override
    public ActionResultType interactAt(PlayerEntity player, Vector3d hitVec, Hand hand) {
        BlockState state = getBlockState();
        if (state.getBlock().isAir(state, level, getBlockPos())) {
            // try a right click on the block below - makes it easier to plant crops in an empty crop support
            BlockPos below = getBlockPos().below();
            Vector3d eye = player.getEyePosition(0f);
            Vector3d end = Vector3d.atCenterOf(below).add(0, 0.25, 0);
            RayTraceContext ctx = new RayTraceContext(eye, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player);
            BlockRayTraceResult brtr = player.level.clip(ctx);
            if (brtr.getType() == RayTraceResult.Type.BLOCK && brtr.getBlockPos().equals(below)) {
                return player.getItemInHand(hand).useOn(new ItemUseContext(player, hand, brtr));
            }
        }
        return super.interactAt(player, hitVec, hand);
    }
}
