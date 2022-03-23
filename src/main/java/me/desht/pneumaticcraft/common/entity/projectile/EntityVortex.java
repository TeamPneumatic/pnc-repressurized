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

package me.desht.pneumaticcraft.common.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.WebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityVortex extends ThrowableEntity {
    private int hitCounter = 0;

    // clientside: rendering X offset of vortex, depends on which hand the vortex was fired from
    private float renderOffsetX = -Float.MAX_VALUE;

    public EntityVortex(EntityType<? extends EntityVortex> type, World world) {
        super(type, world);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();

        // onImpact() is no longer called for blocks with no collision box, like shrubs & crops, as of MC 1.16.2
        if (!level.isClientSide) {
            BlockPos.betweenClosedStream(getBoundingBox())
                    .filter(pos -> vortexBreakable(level.getBlockState(pos).getBlock()))
                    .forEach(this::handleVortexCollision);
        }

        setDeltaMovement(getDeltaMovement().scale(0.95));
        if (getDeltaMovement().lengthSqr() < 0.1D) {
            remove();
        }
    }

    public boolean hasRenderOffsetX() {
        return renderOffsetX > -Float.MAX_VALUE;
    }

    public float getRenderOffsetX() {
        return renderOffsetX;
    }

    public void setRenderOffsetX(float renderOffsetX) {
        this.renderOffsetX = renderOffsetX;
    }

    private boolean tryCutPlants(BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (vortexBreakable(block)) {
            level.destroyBlock(pos, true);
            return true;
        }
        return false;
    }

    @Override
    public float getGravity() {
        return 0;
    }

    @Override
    protected void onHit(RayTraceResult rtr) {
        if (rtr.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) rtr).getEntity();
            entity.setDeltaMovement(entity.getDeltaMovement().add(this.getDeltaMovement().add(0, 0.4, 0)));
            ItemStack shears = new ItemStack(Items.SHEARS);
            // getOwner = getShooter
            if (entity instanceof LivingEntity) {
                PlayerEntity shooter = getOwner() instanceof PlayerEntity ? (PlayerEntity) getOwner() : null;
                if (shooter != null) shears.getItem().interactLivingEntity(shears, shooter, (LivingEntity) entity, Hand.MAIN_HAND);
            }
        } else if (rtr.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) rtr).getBlockPos();
            Block block = level.getBlockState(pos).getBlock();
            if (vortexBreakable(block)) {
                if (!level.isClientSide) {
                    handleVortexCollision(pos);
                }
            } else {
                remove();
            }
        }
        hitCounter++;
        if (hitCounter > 20) remove();
    }

    private void handleVortexCollision(BlockPos pos) {
        BlockPos.Mutable mPos = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());
        if (tryCutPlants(pos)) {
            int plantsCut = 1;
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        mPos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        if (tryCutPlants(mPos)) plantsCut++;
                    }
                }
            }
            // slow the vortex down for each plant it broke
            double mult = Math.pow(0.85D, plantsCut);
            setDeltaMovement(getDeltaMovement().scale(mult));
        }
    }

    private boolean vortexBreakable(Block block) {
        return block instanceof IPlantable || block instanceof LeavesBlock || block instanceof WebBlock;
    }

    @Override
    protected void defineSynchedData() {

    }
}
