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
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class EntitySemiblockBase extends Entity implements ISemiBlock, IGUIButtonSensitive {
    private static final DataParameter<Integer> TIME_SINCE_HIT = EntityDataManager.createKey(EntitySemiblockBase.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE_TAKEN = EntityDataManager.createKey(EntitySemiblockBase.class, DataSerializers.FLOAT);

    private static final float MAX_HEALTH = 40.0F;

    private TileEntity cachedTE;
    private boolean shouldDropItem = true;
    private AxisAlignedBB blockBounds;
    private BlockPos blockPos;
    private Vector3d dropOffset = Vector3d.ZERO;

    EntitySemiblockBase(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    /**
     * Called by onRemovedFromWorld(). Override in subclasses if needed, but be sure to call the super() method.
     */
    protected void onBroken() {
        if (!world.isRemote) {
            if (shouldDropItem) {
                getDrops().forEach(this::dropItem);
            }
            if (world.isAreaLoaded(blockPos, 1)) {
                world.notifyNeighborsOfStateChange(blockPos, world.getBlockState(blockPos).getBlock());
            }
        }
    }

    private void dropItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(world, getPosX() + dropOffset.getX(), getPosY() + dropOffset.getY(), getPosZ() + dropOffset.getZ(), stack);
            itemEntity.setDefaultPickupDelay();
            if (captureDrops() != null)
                captureDrops().add(itemEntity);
            else
                world.addEntity(itemEntity);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (ticksExisted == 1) {
            // can't do this in onAddedToWorld() because querying the blockstate then can cause a deadlock
            // add a small outset so the entity covers the block and becomes interactable
            setBoundingBox(getBlockBounds().offset(blockPos));//.grow(0.01));
            // a semiblock entity doesn't move once added to world
        }

        if (this.getTimeSinceHit() > 0) {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }
        if (this.getDamageTaken() > 0.0F) {
            this.setDamageTaken(this.getDamageTaken() - 1.0F);
        }

        if (isAlive() && !canStay()) {
            remove();
        }
    }

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d hitVec, Hand hand) {
        Vector3d eye = player.getEyePosition(0f);
        Vector3d end = eye.add(player.getLookVec().normalize().scale(5f));
        RayTraceContext ctx = new RayTraceContext(eye, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player);
        BlockRayTraceResult brtr = player.world.rayTraceBlocks(ctx);

        if (brtr == null) {
            // shouldn't happen, but sanity checking...
            return ActionResultType.PASS;
        }

        if (player.getHeldItem(hand).getItem() == ModItems.LOGISTICS_CONFIGURATOR.get()) {
            if (player.isSneaking()) {
                removeSemiblock(player);
                return ActionResultType.SUCCESS;
            } else {
                if (onRightClickWithConfigurator(player, brtr.getFace())) {
                    player.getHeldItem(hand).getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                            .ifPresent(h -> h.addAir(-PneumaticValues.USAGE_LOGISTICS_CONFIGURATOR));
                    return ActionResultType.SUCCESS;
                } else {
                    return ActionResultType.PASS;
                }
            }
        } else {
            // allow right-clicks to pass through to the inventory block being covered
            if (player.isSneaking()) {
                ItemUseContext itemCtx = new ItemUseContext(player, hand, brtr);
                ActionResultType res = player.getHeldItem(hand).onItemUseFirst(itemCtx);
                return res == ActionResultType.PASS ? player.getHeldItem(hand).onItemUse(itemCtx) : res;
            } else {
                return getBlockState().onBlockActivated(world, player, hand, brtr);
            }
        }
    }

    @Override
    protected void registerData() {
        this.dataManager.register(TIME_SINCE_HIT, 0);
        this.dataManager.register(DAMAGE_TAKEN, 0.0F);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        // a semiblock is positioned when added to world, and not again
        if (!isAddedToWorld()) {
            super.setPosition(x, y, z);
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
        return world.getBlockState(blockPos);
    }

    @Override
    public TileEntity getCachedTileEntity() {
        if (cachedTE == null || cachedTE.isRemoved()) {
            cachedTE = world.getTileEntity(blockPos);
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
        return ForgeRegistries.ITEMS.getValue(getId());
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        Item item = getDroppedItem();
        if (item != null) {
            ItemStack stack = new ItemStack(getDroppedItem());
            CompoundNBT tag = new CompoundNBT();
            serializeNBT(tag);
            if (!tag.isEmpty()) stack.getOrCreateTag().put("EntityTag", tag); // see EntityType#applyItemNBT()
            drops.add(stack);
        }
        return drops;
    }

    /**
     * Get the bounding box for this entity, which by default is the bounding box of the block we're on.  This should
     * be in a 0->1 coordinate space; it will be offset by the block's blockpos to get the actual entity bounding box
     * as returned by {@link Entity#getBoundingBox()}.
     *
     * @return a bounding box
     */
    public AxisAlignedBB getBlockBounds() {
        if (blockBounds == null) {
            if (world == null || world.getBlockState(blockPos).isAir(world, blockPos)) {
                blockBounds = VoxelShapes.fullCube().getBoundingBox();
            } else {
                blockBounds = world.getBlockState(blockPos).getShape(world, blockPos).getBoundingBox();
            }
        }
        return blockBounds;
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
    public ResourceLocation getId() {
        return getType().getRegistryName();
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT tag) {
        return tag;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        serializeNBT(compound);
    }

    @Override
    public boolean isValid() {
        return isAlive();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!world.isRemote) {
            Direction dir = this instanceof IDirectionalSemiblock ? ((IDirectionalSemiblock) this).getSide() : null;
            if (SemiblockTracker.getInstance().putSemiblock(world, blockPos, this)) {
                MinecraftForge.EVENT_BUS.post(new SemiblockEvent.PlaceEvent(world.getWorld(), blockPos, this));
            } else {
                Log.error("found existing semiblock at %s, pos=%s, dir=%s", world, blockPos, dir);
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        if (!world.isRemote) {
            Direction dir = this instanceof IDirectionalSemiblock ? ((IDirectionalSemiblock) this).getSide() : null;
            SemiblockTracker.getInstance().clearSemiblock(world, blockPos, dir);
            MinecraftForge.EVENT_BUS.post(new SemiblockEvent.BreakEvent(world.getWorld(), blockPos, this));
        }

        onBroken();
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public int getTrackingId() {
        return isAddedToWorld() ? getEntityId() : -1;
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
        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        dropOffset = player.getPositionVec().subtract(this.getPositionVec()).normalize();
        this.remove();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!this.world.isRemote && this.isAlive()) {
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
            boolean isCreative = source.getTrueSource() instanceof PlayerEntity
                    && ((PlayerEntity)source.getTrueSource()).abilities.isCreativeMode;
            if (isCreative || this.getDamageTaken() > MAX_HEALTH) {
                shouldDropItem = !isCreative;
                remove();
            }
        }
        return true;
    }

    private void setDamageTaken(float damageTaken) {
        this.dataManager.set(DAMAGE_TAKEN, damageTaken);
    }

    public float getDamageTaken() {
        return this.dataManager.get(DAMAGE_TAKEN);
    }

    private void setTimeSinceHit(int timeSinceHit) {
        this.dataManager.set(TIME_SINCE_HIT, timeSinceHit);
    }

    public int getTimeSinceHit() {
        return this.dataManager.get(TIME_SINCE_HIT);
    }

    public boolean isAir() {
        return getBlockState().isAir(world, blockPos);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        // nothing
    }

    @Override
    public void writeToBuf(PacketBuffer payload) {
    }

    @Override
    public void readFromBuf(PacketBuffer payload) {
    }
}
