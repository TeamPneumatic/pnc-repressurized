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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.api.semiblock.SemiblockEvent;
import me.desht.pneumaticcraft.common.block.entity.IGUIButtonSensitive;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collection;
import java.util.Optional;

public abstract class AbstractSemiblockEntity extends Entity implements ISemiBlock, IGUIButtonSensitive {
    private static final EntityDataAccessor<Integer> TIME_SINCE_HIT = SynchedEntityData.defineId(AbstractSemiblockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE_TAKEN = SynchedEntityData.defineId(AbstractSemiblockEntity.class, EntityDataSerializers.FLOAT);

    private static final float MAX_HEALTH = 40.0F;

    private BlockEntity cachedTE;
    private boolean beingRemoved = false;
    private AABB blockBounds;
    private BlockPos blockPos;
    private Vec3 dropOffset = Vec3.ZERO;
    private Block lastBlock;  // to detect if the underlying block has changed

    AbstractSemiblockEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    private void dropItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level(), getX() + dropOffset.x(), getY() + dropOffset.y(), getZ() + dropOffset.z(), stack);
            itemEntity.setDefaultPickUpDelay();
            Collection<ItemEntity> capture = captureDrops();
            if (capture != null) {
                capture.add(itemEntity);
            } else {
                level().addFreshEntity(itemEntity);
            }
        }
    }

    @Override
    public Component getSemiblockDisplayName() {
        return new ItemStack(getDroppedItem()).getDisplayName();
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount == 1) {
            // can't do this in onAddedToWorld() because querying the blockstate then can cause a deadlock
            // add a small outset so the entity covers the block and becomes interactable
            setBoundingBox(getBlockBounds().move(blockPos));//.grow(0.01));
            // a semiblock entity doesn't move once added to world

            lastBlock = getBlockState().getBlock();
        }

        if (this.getTimeSinceHit() > 0) {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }
        if (this.getDamageTaken() > 0.0F) {
            this.setDamageTaken(this.getDamageTaken() - 1.0F);
        }

        if (!level().isClientSide && isAlive() && !canStay()) {
            beingRemoved = true;
            kill();
        }

        Block curBlock = getBlockState().getBlock();
        if (curBlock != lastBlock) {
            cachedTE = null;
            blockBounds = null;
            lastBlock = curBlock;
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
        Vec3 eye = player.getEyePosition(0f);
        Vec3 end = eye.add(player.getLookAngle().normalize().scale(5f));
        ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult brtr = player.level().clip(ctx);

        if (brtr.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        if (player.getItemInHand(hand).getItem() == ModItems.LOGISTICS_CONFIGURATOR.get() && !player.level().isClientSide) {
            if (player.isShiftKeyDown()) {
                killedByEntity(player);
                return InteractionResult.SUCCESS;
            } else {
                if (onRightClickWithConfigurator(player, brtr.getDirection())) {
                    PNCCapabilities.getAirHandler(player.getItemInHand(hand))
                            .ifPresent(h -> h.addAir(-PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR));
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.PASS;
                }
            }
        } else {
            // allow right-clicks to pass through to the inventory block being covered
            InteractionResult res = player.isShiftKeyDown() ? InteractionResult.PASS : getBlockState().useWithoutItem(level(), player, brtr);
            if (res.consumesAction() || res == InteractionResult.FAIL) {
                return res;
            }
            UseOnContext itemCtx = new UseOnContext(player, hand, brtr);
            res = player.getItemInHand(hand).onItemUseFirst(itemCtx);
            return res == InteractionResult.PASS ? player.getItemInHand(hand).useOn(itemCtx) : res;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TIME_SINCE_HIT, 0);
        builder.define(DAMAGE_TAKEN, 0.0F);
    }

    @Override
    public Level getWorld() {
        return level();
    }

    @Override
    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public void setPos(double x, double y, double z) {
        // a semiblock is positioned when added to world, and not again
        if (!isAddedToLevel()) {
            super.setPos(x, y, z);
            this.blockPos = BlockPos.containing(x, y, z);
        }
    }

    /**
     * Get the blockstate at the semiblock's position
     * @return the blockstate
     */
    public BlockState getBlockState() {
        return level().getBlockState(blockPos);
    }

    @Override
    public BlockEntity getCachedTileEntity() {
        if (!level().isLoaded(blockPos)) return null;

        if (cachedTE == null || cachedTE.isRemoved()) {
            cachedTE = level().getBlockEntity(blockPos);
        }
        return cachedTE;
    }

    /**
     * Get the semiblock item which will be dropped when this semiblock is removed.
     * By default, that item has the same registry name as the corresponding entity; this could be overridden but
     * probably shouldn't be, for the sake of clarity.
     *
     * @return the dropped item, or null if nothing should be dropped
     */
    protected Item getDroppedItem() {
        return BuiltInRegistries.ITEM.get(getSemiblockId());
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        Item item = getDroppedItem();
        if (item != null) {
            ItemStack stack = new ItemStack(getDroppedItem());
            CompoundTag tag = new CompoundTag();
            serializeNBT(tag, registryAccess());
            if (!tag.isEmpty()) {
                stack.set(ModDataComponents.SEMIBLOCK_DATA, CustomData.of(tag));
            }
            drops.add(stack);
        }
        return drops;
    }

    /**
     * Get the bounding box for this entity. Override {@link #calculateBlockBounds()} if you need a custom bounding box.
     *
     * @return a bounding box
     */
    public final AABB getBlockBounds() {
        // we can cache this because the bounding box won't change after placement
        if (blockBounds == null) {
            blockBounds = calculateBlockBounds();
        }
        return blockBounds;
    }

    /**
     * Get the bounding box for this entity, which by default is the bounding box of the block we're on.  This should
     * be in a 0->1 coordinate space; it will be offset by the block's blockpos to get the actual entity bounding box
     * as returned by {@link Entity#getBoundingBox()}.
     *
     * @return a bounding box
     */
    protected AABB calculateBlockBounds() {
        // default behaviour: try & fit around the block in this blockpos
        AABB aabb;
        if (level() != null) {
            VoxelShape shape = level().getBlockState(blockPos).getShape(level(), blockPos);
            aabb = shape.isEmpty() ? Shapes.block().bounds() : shape.bounds();
        } else {
            aabb = Shapes.block().bounds();
        }
        return aabb;
    }

    /**
     * Check if this semiblock can remain here.
     *
     * @return true if the semiblock can stay, false if it should be dropped
     */
    public boolean canStay() {
        return canPlace(null);
    }

    @Override
    public ResourceLocation getSemiblockId() {
        return PneumaticCraftUtils.getRegistryName(BuiltInRegistries.ENTITY_TYPE, getType()).orElseThrow();
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        return tag;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        serializeNBT(compound, registryAccess());
    }

    @Override
    public boolean isValid() {
        return isAlive();
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        Level level = level();
        if (!level.isClientSide) {
            if (SemiblockTracker.getInstance().putSemiblock(level, blockPos, this)) {
                NeoForge.EVENT_BUS.post(new SemiblockEvent.PlaceEvent(level, blockPos, this));
                level.markAndNotifyBlock(blockPos, level.getChunkAt(blockPos), getBlockState(), getBlockState(), Block.UPDATE_ALL, 512);
            } else {
                Direction dir = this instanceof IDirectionalSemiblock d ? d.getSide() : null;
                Log.error("SemiblockTracker: not overwriting existing semiblock at {}, pos={}, dir={}!", level, blockPos, dir);
            }
        }
    }

    @Override
    public void onRemovedFromLevel() {
        Level level = level();
        if (!level.isClientSide) {
            Direction dir = this instanceof IDirectionalSemiblock d ? d.getSide() : null;
            SemiblockTracker.getInstance().clearSemiblock(level, blockPos, dir);

            NeoForge.EVENT_BUS.post(new SemiblockEvent.BreakEvent(level, blockPos, this));

            if (beingRemoved) {
                getDrops().forEach(this::dropItem);
            }

            if (level.isLoaded(blockPos)) {
                level.markAndNotifyBlock(blockPos, level.getChunkAt(blockPos), getBlockState(), getBlockState(), Block.UPDATE_ALL, 512);
            }
        }

        doExtraCleanupTasks(beingRemoved);

        super.onRemovedFromLevel();
    }

    /**
     * Called by onRemovedFromWorld() to finalize semiblock removal. Override in subclasses.
     * @param removingSemiblock true if this semiblock is actually being removed from world, false if removing to chunk unloading
     */
    protected void doExtraCleanupTasks(boolean removingSemiblock) {
        // nothing
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public int getTrackingId() {
        return isAddedToLevel() ? getId() : -1;
    }

    @Override
    public boolean canCoexist(ISemiBlock otherSemiblock) {
        if (this instanceof IDirectionalSemiblock d1) {
            return !(otherSemiblock instanceof IDirectionalSemiblock d2) || d1.getSide() != d2.getSide();
        } else {
            return otherSemiblock instanceof IDirectionalSemiblock;
        }
    }

    @Override
    public void killedByEntity(Entity entity) {
        entity.level().playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        dropOffset = entity.position().subtract(this.position()).normalize();
        beingRemoved = true;
        kill();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || !(source.getDirectEntity() instanceof Player)) {
            return false;
        } else if (!this.level().isClientSide && this.isAlive()) {
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
            if (this.getDamageTaken() > MAX_HEALTH) {
                if (source.getEntity() != null) {
                    killedByEntity(source.getEntity());
                } else {
                    kill();
                }
            }
        }
        return true;
    }

    private void setDamageTaken(float damageTaken) {
        this.entityData.set(DAMAGE_TAKEN, damageTaken);
    }

    public float getDamageTaken() {
        return this.entityData.get(DAMAGE_TAKEN);
    }

    private void setTimeSinceHit(int timeSinceHit) {
        this.entityData.set(TIME_SINCE_HIT, timeSinceHit);
    }

    public int getTimeSinceHit() {
        return this.entityData.get(TIME_SINCE_HIT);
    }

    public boolean isAir() {
        return getBlockState().isAir();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        // do nothing, override in subclasses
    }

    @Override
    public void writeToBuf(RegistryFriendlyByteBuf payload) {
    }

    @Override
    public void readFromBuf(RegistryFriendlyByteBuf payload) {
    }

    @Override
    public <T> Optional<T> getSemiblockCapability(EntityCapability<T, Direction> capability, Direction direction) {
        return Optional.ofNullable(getCapability(capability, direction));
    }

    @Override
    public <T> Optional<T> getSemiblockCapability(EntityCapability<T, Void> capability) {
        return Optional.ofNullable(getCapability(capability));
    }
}
