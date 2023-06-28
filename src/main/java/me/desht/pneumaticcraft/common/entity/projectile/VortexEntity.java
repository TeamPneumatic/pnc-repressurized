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

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.network.NetworkHooks;

public class VortexEntity extends ThrowableProjectile {
    private int hitCounter = 0;

    // clientside: rendering X offset of vortex, depends on which hand the vortex was fired from
    private float renderOffsetX = -Float.MAX_VALUE;

    public VortexEntity(EntityType<? extends VortexEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();

        // onImpact() is no longer called for blocks with no collision box, like shrubs & crops, as of MC 1.16.2
        if (!level().isClientSide) {
            BlockPos.betweenClosedStream(getBoundingBox())
                    .filter(pos -> vortexBreakable(level().getBlockState(pos).getBlock()))
                    .forEach(this::handleVortexCollision);
        }

        setDeltaMovement(getDeltaMovement().scale(0.95));
        if (getDeltaMovement().lengthSqr() < 0.1D) {
            discard();
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

    @Override
    public float getGravity() {
        return 0;
    }

    @Override
    protected void onHit(HitResult rtr) {
        if (rtr.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) rtr).getEntity();
            entity.setDeltaMovement(entity.getDeltaMovement().add(this.getDeltaMovement().add(0, 0.4, 0)));
            ItemStack shears = new ItemStack(Items.SHEARS);
            // getOwner = getShooter
            if (entity instanceof LivingEntity) {
                Player shooter = getOwner() instanceof Player ? (Player) getOwner() : null;
                if (shooter != null) shears.getItem().interactLivingEntity(shears, shooter, (LivingEntity) entity, InteractionHand.MAIN_HAND);
            }
        } else if (rtr.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) rtr).getBlockPos();
            Block block = level().getBlockState(pos).getBlock();
            if (vortexBreakable(block)) {
                if (!level().isClientSide) {
                    handleVortexCollision(pos);
                }
            } else {
                discard();
            }
        }
        hitCounter++;
        if (hitCounter > 20) discard();
    }

    private void handleVortexCollision(BlockPos pos) {
        level().destroyBlock(pos, true);
        setDeltaMovement(getDeltaMovement().scale(0.85D));
    }

    private boolean vortexBreakable(Block block) {
        return block instanceof IPlantable || block instanceof LeavesBlock || block instanceof WebBlock || block instanceof SnowLayerBlock;
    }

    @Override
    protected void defineSynchedData() {
    }
}
