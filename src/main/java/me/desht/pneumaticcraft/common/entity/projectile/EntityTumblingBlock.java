package me.desht.pneumaticcraft.common.entity.projectile;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

/**
 * A bit like an EntityFallingBlock but tumbles as it flies, and tries to form a block on impact with any other
 * block, not just when it lands on top of another block.
 */
public class EntityTumblingBlock extends ThrowableEntity {
    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.defineId(EntityTumblingBlock.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<ItemStack> STATE_STACK = EntityDataManager.defineId(EntityTumblingBlock.class, DataSerializers.ITEM_STACK);
    private static FakePlayer fakePlayer;

    private static final Vector3d Y_POS = new Vector3d(0, 1, 0);

    public final Vector3f tumbleVec;  // used for rendering

    public EntityTumblingBlock(EntityType<EntityTumblingBlock> type, World worldIn) {
        super(type, worldIn);
        this.tumbleVec = makeTumbleVec(worldIn, null);
    }

    public EntityTumblingBlock(World worldIn, LivingEntity thrower, double x, double y, double z, @Nonnull ItemStack stack) {
        super(ModEntities.TUMBLING_BLOCK.get(), worldIn);
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

    private Vector3f makeTumbleVec(World world, LivingEntity thrower) {
        if (thrower != null) {
            return new Vector3f(thrower.getLookAngle().cross(Y_POS));
        } else if (world != null && world.isClientSide) {
            PlayerEntity player = ClientUtils.getClientPlayer();
            return player == null ? null : new Vector3f(player.getLookAngle().cross(Y_POS));
        } else {
            return null;
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ORIGIN, BlockPos.ZERO);
        entityData.define(STATE_STACK, ItemStack.EMPTY);
    }

    // shoot()
    @Override
    public void shootFromRotation(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        // velocities etc. get set up in TileEntityAirCannon#launchEntity()
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

        super.tick();  // handles nearly all of the in-flight logic

        if (!level.isClientSide) {
            BlockPos blockpos1 = blockPosition(); //new BlockPos(this);
            if (!onGround && (tickCount > 100 && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || tickCount > 600)) {
                dropAsItem();
                remove();
            }
        }
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (!level.isClientSide) {
            remove();
            if (result.getType() == RayTraceResult.Type.BLOCK) {
                if (!tryPlaceAsBlock((BlockRayTraceResult) result)) {
                    dropAsItem();
                }
            } else {
                dropAsItem();
            }
        }
    }

    private boolean tryPlaceAsBlock(BlockRayTraceResult brtr) {
        ItemStack stack = getStack();
        if (!(stack.getItem() instanceof BlockItem)) {
            return false;
        }
        BlockPos pos0 = brtr.getBlockPos();
        Direction face = brtr.getDirection();
        // getOwner = getThrower
        PlayerEntity placer = getOwner() instanceof PlayerEntity ? (PlayerEntity) getOwner() : getFakePlayer();
        BlockState state = level.getBlockState(pos0);
        BlockItemUseContext ctx = new LocalBlockItemUseContext(new ItemUseContext(placer, Hand.MAIN_HAND, brtr));
        BlockPos pos = state.canBeReplaced(ctx) ? pos0 : pos0.relative(face);

        if (level.getBlockState(pos).canBeReplaced(ctx)) {
            BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
            if (!ForgeEventFactory.onBlockPlace(placer, snapshot, face)) {
                ActionResultType res = ((BlockItem) stack.getItem()).place(ctx);
                return res == ActionResultType.SUCCESS || res == ActionResultType.CONSUME;
            }
        }
        return false;
    }

    private void dropAsItem() {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(getStack().copy(), 0.0F);
        }
    }

    private PlayerEntity getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get((ServerWorld) level, new GameProfile(null, "[Tumbling Block]"));
            fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
        }
        fakePlayer.setPos(getX(), getY(), getZ());
        fakePlayer.setItemInHand(Hand.MAIN_HAND, getStack());
        return fakePlayer;
    }

    /**
     * Stores a copy of the item being used, so the player's held version doesn't get modified when
     * {@link BlockItem#place(BlockItemUseContext)} is called by {@link #tryPlaceAsBlock(BlockRayTraceResult)}
     * (the item has already been taken from the player, when the entity was created)
     */
    private static class LocalBlockItemUseContext extends BlockItemUseContext {
        private final ItemStack stack;

        public LocalBlockItemUseContext(ItemUseContext context) {
            super(context);
            stack = context.getItemInHand().copy();
        }

        @Override
        public ItemStack getItemInHand() {
            return stack == null ? ItemStack.EMPTY : stack;
        }
    }
}
