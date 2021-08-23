package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.api.semiblock.SemiblockEvent;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class EntitySemiblockBase extends Entity implements ISemiBlock, IGUIButtonSensitive {
    private static final DataParameter<Integer> TIME_SINCE_HIT = EntityDataManager.defineId(EntitySemiblockBase.class, DataSerializers.INT);
    private static final DataParameter<Float> DAMAGE_TAKEN = EntityDataManager.defineId(EntitySemiblockBase.class, DataSerializers.FLOAT);

    private static final float MAX_HEALTH = 40.0F;

    private TileEntity cachedTE;
    private boolean shouldDropItem = true;
    private AxisAlignedBB blockBounds;
    private BlockPos blockPos;
    private Vector3d dropOffset = Vector3d.ZERO;
    private Block lastBlock;  // to detect if the underlying block has changed

    EntitySemiblockBase(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    /**
     * Called by onRemovedFromWorld(). Override in subclasses if needed, but be sure to call the super() method.
     */
    protected void onBroken() {
        if (!level.isClientSide) {
            if (shouldDropItem) {
                getDrops().forEach(this::dropItem);
            }
            if (level.isAreaLoaded(blockPos, 1)) {
                level.updateNeighborsAt(blockPos, level.getBlockState(blockPos).getBlock());
            }
        }
    }

    private void dropItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, getX() + dropOffset.x(), getY() + dropOffset.y(), getZ() + dropOffset.z(), stack);
            itemEntity.setDefaultPickUpDelay();
            if (captureDrops() != null)
                captureDrops().add(itemEntity);
            else
                level.addFreshEntity(itemEntity);
        }
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

        if (!level.isClientSide && isAlive() && !canStay()) {
            remove();
        }

        Block curBlock = getBlockState().getBlock();
        if (curBlock != lastBlock) {
            cachedTE = null;
            blockBounds = null;
            lastBlock = curBlock;
        }
    }

    @Override
    public ActionResultType interactAt(PlayerEntity player, Vector3d hitVec, Hand hand) {
        Vector3d eye = player.getEyePosition(0f);
        Vector3d end = eye.add(player.getLookAngle().normalize().scale(5f));
        RayTraceContext ctx = new RayTraceContext(eye, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player);
        BlockRayTraceResult brtr = player.level.clip(ctx);

        if (brtr == null) {
            // shouldn't happen, but sanity checking...
            return ActionResultType.PASS;
        }

        if (player.getItemInHand(hand).getItem() == ModItems.LOGISTICS_CONFIGURATOR.get()) {
            if (player.isShiftKeyDown()) {
                removeSemiblock(player);
                return ActionResultType.SUCCESS;
            } else {
                if (onRightClickWithConfigurator(player, brtr.getDirection())) {
                    player.getItemInHand(hand).getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                            .ifPresent(h -> h.addAir(-PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR));
                    return ActionResultType.SUCCESS;
                } else {
                    return ActionResultType.PASS;
                }
            }
        } else {
            // allow right-clicks to pass through to the inventory block being covered
            ItemUseContext itemCtx = new ItemUseContext(player, hand, brtr);
            ActionResultType res = player.isShiftKeyDown() ? ActionResultType.PASS : getBlockState().use(level, player, hand, brtr);
            if (res.consumesAction() || res == ActionResultType.FAIL) return res;
            res = player.getItemInHand(hand).onItemUseFirst(itemCtx);
            return res == ActionResultType.PASS ? player.getItemInHand(hand).useOn(itemCtx) : res;
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TIME_SINCE_HIT, 0);
        this.entityData.define(DAMAGE_TAKEN, 0.0F);
    }

    @Override
    public World getWorld() {
        return level;
    }

    @Override
    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public void setPos(double x, double y, double z) {
        // a semiblock is positioned when added to world, and not again
        if (!isAddedToWorld()) {
            super.setPos(x, y, z);
            this.blockPos = new BlockPos(x, y, z);
        }
    }

    @Override
    public float getBrightness() {
        // cheat a bit here - semiblocks on solid blocks will be unlit otherwise
        // instead we use the brightness at the top of the world (hoping there isn't a block there...)
        return ClientUtils.getBrightnessAtWorldHeight();
    }

    /**
     * Get the blockstate at the semiblock's position
     * @return the blockstate
     */
    public BlockState getBlockState() {
        return level.getBlockState(blockPos);
    }

    @Override
    public TileEntity getCachedTileEntity() {
        if (cachedTE == null || cachedTE.isRemoved()) {
            cachedTE = level.getBlockEntity(blockPos);
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
        return ForgeRegistries.ITEMS.getValue(getSemiblockId());
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        Item item = getDroppedItem();
        if (item != null) {
            ItemStack stack = new ItemStack(getDroppedItem());
            CompoundNBT tag = new CompoundNBT();
            serializeNBT(tag);
            if (!tag.isEmpty()) stack.getOrCreateTag().put(NBTKeys.ENTITY_TAG, tag); // see EntityType#applyItemNBT()
            drops.add(stack);
        }
        return drops;
    }

    /**
     * Get the bounding box for this entity. Override {@link #calculateBlockBounds()} if you need a custom bounding box.
     *
     * @return a bounding box
     */
    public final AxisAlignedBB getBlockBounds() {
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
    protected AxisAlignedBB calculateBlockBounds() {
        // default behaviour: try & fit around the block in this blockpos
        AxisAlignedBB aabb;
        if (level != null) {
            VoxelShape shape = level.getBlockState(blockPos).getShape(level, blockPos);
            aabb = shape.isEmpty() ? VoxelShapes.block().bounds() : shape.bounds();
        } else {
            aabb = VoxelShapes.block().bounds();
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
        return getType().getRegistryName();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT tag) {
        return tag;
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        serializeNBT(compound);
    }

    @Override
    public boolean isValid() {
        return isAlive();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!level.isClientSide) {
            Direction dir = this instanceof IDirectionalSemiblock ? ((IDirectionalSemiblock) this).getSide() : null;
            if (SemiblockTracker.getInstance().putSemiblock(level, blockPos, this)) {
                MinecraftForge.EVENT_BUS.post(new SemiblockEvent.PlaceEvent(level, blockPos, this));
            } else {
                Log.error("found existing semiblock at %s, pos=%s, dir=%s", level, blockPos, dir);
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        if (!level.isClientSide) {
            Direction dir = this instanceof IDirectionalSemiblock ? ((IDirectionalSemiblock) this).getSide() : null;
            SemiblockTracker.getInstance().clearSemiblock(level, blockPos, dir);
            MinecraftForge.EVENT_BUS.post(new SemiblockEvent.BreakEvent(level, blockPos, this));
        }

        onBroken();
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
        return isAddedToWorld() ? getId() : -1;
    }

    @Override
    public boolean canCoexist(ISemiBlock otherSemiblock) {
        if (this instanceof IDirectionalSemiblock) {
            return !(otherSemiblock instanceof IDirectionalSemiblock)
                    || ((IDirectionalSemiblock) this).getSide() != ((IDirectionalSemiblock) otherSemiblock).getSide();
        } else {
            return otherSemiblock instanceof IDirectionalSemiblock;
        }
    }

    @Override
    public void removeSemiblock(PlayerEntity player) {
        player.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
        dropOffset = player.position().subtract(this.position()).normalize();
        this.remove();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || !(source.getDirectEntity() instanceof PlayerEntity)) {
            return false;
        } else if (!this.level.isClientSide && this.isAlive()) {
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
            boolean isCreative = source.getEntity() instanceof PlayerEntity
                    && ((PlayerEntity)source.getEntity()).abilities.instabuild;
            if (isCreative || this.getDamageTaken() > MAX_HEALTH) {
                shouldDropItem = !isCreative;
                remove();
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
        return getBlockState().isAir(level, blockPos);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        // nothing
    }

    @Override
    public void writeToBuf(PacketBuffer payload) {
    }

    @Override
    public void readFromBuf(PacketBuffer payload) {
    }
}
