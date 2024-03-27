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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.IPlantable;

public class CropSupportEntity extends AbstractSemiblockEntity {
    private static final AABB BOUNDS = new AABB(3 / 16D, 0D, 3 / 16D, 13 / 16D, 9 / 16D, 13 / 16D);

    public CropSupportEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected AABB calculateBlockBounds() {
        return BOUNDS;
    }

    @Override
    public void tick() {
        super.tick();

        Level level = level();
        if (level.random.nextDouble() < ConfigHelper.common().machines.cropSticksGrowthBoostChance.get() && !getBlockState().isAir()) {
            if (!level.isClientSide) {
                getBlockState().randomTick((ServerLevel) level, getBlockPos(), level.random);
            } else {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean canPlace(Direction facing) {
        BlockState state = getBlockState();
        return (state.isAir() || state.getBlock() instanceof IPlantable) && canStay();
    }

    @Override
    public boolean canStay() {
        BlockState state = getBlockState();
        if (!state.isAir()) {
            return true;
        }

        BlockPos posBelow = getBlockPos().relative(Direction.DOWN);
        BlockState stateBelow = level().getBlockState(posBelow);
        return !stateBelow.isAir();
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
        BlockState state = getBlockState();
        if (state.isAir()) {
            // try a right click on the block below - makes it easier to plant crops in an empty crop support
            BlockPos below = getBlockPos().below();
            Vec3 eye = player.getEyePosition(0f);
            Vec3 end = Vec3.atCenterOf(below).add(0, 0.25, 0);
            ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            BlockHitResult brtr = player.level().clip(ctx);
            if (brtr.getType() == HitResult.Type.BLOCK && brtr.getBlockPos().equals(below)) {
                return player.getItemInHand(hand).useOn(new UseOnContext(player, hand, brtr));
            }
        }
        return super.interactAt(player, hitVec, hand);
    }
}
