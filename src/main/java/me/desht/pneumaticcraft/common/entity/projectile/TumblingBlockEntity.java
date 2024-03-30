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

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import org.apache.commons.lang3.Validate;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

/**
 * A bit like a FallingBlockEntity but tumbles as it flies, and tries to form a block on impact with any other
 * block, not just when it lands on top of another block.
 */
public class TumblingBlockEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<BlockPos> ORIGIN = SynchedEntityData.defineId(TumblingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<ItemStack> STATE_STACK = SynchedEntityData.defineId(TumblingBlockEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final GameProfile DEFAULT_FAKE_PROFILE = UUIDUtil.createOfflineProfile("Tumbling Block");

    private static final Vec3 Y_POS = new Vec3(0, 1, 0);

    public final Vector3f tumbleVec;  // used for rendering

    public TumblingBlockEntity(EntityType<TumblingBlockEntity> type, Level worldIn) {
        super(type, worldIn);
        this.tumbleVec = makeTumbleVec(worldIn, null);
    }

    public TumblingBlockEntity(Level worldIn, LivingEntity thrower, double x, double y, double z, @Nonnull ItemStack stack) {
        super(ModEntityTypes.TUMBLING_BLOCK.get(), worldIn);
        Validate.isTrue(!stack.isEmpty() && stack.getItem() instanceof BlockItem);

        setOwner(thrower);
        this.blocksBuilding = true;
        this.setPos(x, y + (double)((1.0F - this.getBbHeight()) / 2.0F), z);
        this.setDeltaMovement(0, 0, 0);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.tumbleVec = makeTumbleVec(worldIn, thrower);
        this.setOrigin(blockPosition());
        entityData.set(STATE_STACK, stack);
    }

    private Vector3f makeTumbleVec(Level world, LivingEntity thrower) {
        if (thrower != null) {
            return thrower.getLookAngle().cross(Y_POS).toVector3f();
        } else if (world != null && world.isClientSide) {
            return ClientUtils.getOptionalClientPlayer()
                    .map(p -> p.getLookAngle().cross(Y_POS).toVector3f())
                    .orElse(null);
        } else {
            return null;
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ORIGIN, BlockPos.ZERO);
        entityData.define(STATE_STACK, ItemStack.EMPTY);
    }

    @Override
    public void shootFromRotation(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        // do nothing, since velocities etc. get set up in ItemLaunching#launchEntity()
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
    }

    public ItemStack getStack() {
        return entityData.get(STATE_STACK);
    }

    public BlockPos getOrigin()
    {
        return this.entityData.get(ORIGIN);
    }

    private void setOrigin(BlockPos pos) {
        entityData.set(ORIGIN, pos);
    }

    @Override
    public void tick() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        super.tick();  // handles nearly all the in-flight logic

        if (!level().isClientSide) {
            BlockPos blockpos1 = blockPosition(); //new BlockPos(this);
            if (!onGround() && (tickCount > 100 && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || tickCount > 600)) {
                dropAsItem();
                discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide) {
            discard();
            if (result.getType() == HitResult.Type.BLOCK) {
                if (!tryPlaceAsBlock((BlockHitResult) result)) {
                    dropAsItem();
                }
            } else {
                dropAsItem();
            }
        }
    }

    private boolean tryPlaceAsBlock(BlockHitResult brtr) {
        ItemStack stack = getStack();
        if (!(stack.getItem() instanceof BlockItem)) {
            return false;
        }
        BlockPos pos0 = brtr.getBlockPos();
        Direction face = brtr.getDirection();
        Player placer = getOwner() instanceof Player p ? p : getFakePlayer();
        BlockState state = level().getBlockState(pos0);
        BlockPlaceContext ctx = new LocalBlockPlaceContext(new UseOnContext(placer, InteractionHand.MAIN_HAND, brtr));
        BlockPos pos = state.canBeReplaced(ctx) ? pos0 : pos0.relative(face);

        if (level().getBlockState(pos).canBeReplaced(ctx)) {
            BlockSnapshot snapshot = BlockSnapshot.create(level().dimension(), level(), pos);
            if (!EventHooks.onBlockPlace(placer, snapshot, face)) {
                InteractionResult res = ((BlockItem) stack.getItem()).place(ctx);
                return res == InteractionResult.SUCCESS || res == InteractionResult.CONSUME;
            }
        }
        return false;
    }

    private void dropAsItem() {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(getStack().copy(), 0.0F);
        }
    }

    private Player getFakePlayer() {
        FakePlayer fakePlayer = FakePlayerFactory.get((ServerLevel) level(), DEFAULT_FAKE_PROFILE);
        fakePlayer.setPos(getX(), getY(), getZ());
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, getStack());
        return fakePlayer;
    }

    /**
     * Stores a copy of the item being used, so the player's held version doesn't get modified when
     * {@link BlockItem#place(BlockPlaceContext)} is called by {@link #tryPlaceAsBlock(BlockHitResult)}
     * (the item has already been taken from the player, when the entity was created)
     */
    private static class LocalBlockPlaceContext extends BlockPlaceContext {
        private final ItemStack stack;

        public LocalBlockPlaceContext(UseOnContext context) {
            super(context);
            stack = context.getItemInHand().copy();
        }

        @Override
        public ItemStack getItemInHand() {
            return stack == null ? ItemStack.EMPTY : stack;
        }
    }
}
