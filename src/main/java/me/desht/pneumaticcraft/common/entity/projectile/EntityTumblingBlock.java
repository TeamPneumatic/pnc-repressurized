package me.desht.pneumaticcraft.common.entity.projectile;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

/**
 * A bit like an EntityFallingBlock but tumbles as it flies, and tries to form a block on impact with any other
 * block, not just when it lands on top of another block.
 */
public class EntityTumblingBlock extends ThrowableEntity {
    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(EntityTumblingBlock.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<ItemStack> STATE_STACK = EntityDataManager.createKey(EntityTumblingBlock.class, DataSerializers.ITEMSTACK);
    private static FakePlayer fakePlayer;

    public EntityTumblingBlock(World worldIn) {
        super(ModEntityTypes.TUMBLING_BLOCK, worldIn);
    }

    public EntityTumblingBlock(World worldIn, double x, double y, double z, @Nonnull ItemStack stack) {
        super(ModEntityTypes.TUMBLING_BLOCK, worldIn);
        Validate.isTrue(!stack.isEmpty() && stack.getItem() instanceof BlockItem);

        this.preventEntitySpawning = true;
        this.setPosition(x, y + (double)((1.0F - this.getHeight()) / 2.0F), z);
        this.setMotion(0, 0, 0);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.setOrigin(new BlockPos(this));
        dataManager.set(STATE_STACK, stack);
    }

    public static Entity create(EntityType<Entity> entityEntityType, World world) {
        return new EntityTumblingBlock(world);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerData() {
        dataManager.register(ORIGIN, BlockPos.ZERO);
        dataManager.register(STATE_STACK, ItemStack.EMPTY);
    }

    @Override
    public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        // velocities etc. get set up in TileEntityAirCannon#launchEntity()
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
    }

    public ItemStack getStack() {
        return dataManager.get(STATE_STACK);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getOrigin()
    {
        return this.dataManager.get(ORIGIN);
    }

    private void setOrigin(BlockPos pos) {
        dataManager.set(ORIGIN, pos);
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.tick();  // handles nearly all of the in-flight logic

        if (!world.isRemote) {
            BlockPos blockpos1 = new BlockPos(this);
            if (!onGround && (ticksExisted > 100 && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || ticksExisted > 600)) {
                dropAsItem();
                remove();
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            if (result.getType() == RayTraceResult.Type.BLOCK) {
                remove();
                BlockRayTraceResult brtr = (BlockRayTraceResult) result;
                if (!tryPlaceAsBlock(brtr)) {
                    dropAsItem();
                }
            }
        }
    }

    private boolean tryPlaceAsBlock(BlockRayTraceResult brtr) {
        BlockPos pos0 = brtr.getPos();
        Direction face = brtr.getFace();
        ItemStack stack = getStack();
        PlayerEntity placer = getThrower() instanceof PlayerEntity ? (PlayerEntity) getThrower() : getFakePlayer();
        BlockState state = world.getBlockState(pos0);
        BlockItemUseContext ctx = new BlockItemUseContext(new ItemUseContext(placer, Hand.MAIN_HAND, brtr));
        BlockPos pos = state.isReplaceable(ctx) ? pos0 : pos0.offset(face);

        if (world.getBlockState(pos).isReplaceable(ctx)) {
            Block block = ((BlockItem)stack.getItem()).getBlock();
            BlockState newState = block.getStateForPlacement(ctx);
            return PneumaticCraftUtils.tryPlaceBlock(world, pos, placer, face, newState);
        }
        return false;
    }

    private void dropAsItem() {
        if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            entityDropItem(getStack().copy(), 0.0F);
        }
    }

    private PlayerEntity getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get((ServerWorld) world, new GameProfile(null, "[Tumbling Block]"));
            fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
        }
        fakePlayer.posX = posX;
        fakePlayer.posY = posY;
        fakePlayer.posZ = posZ;
        fakePlayer.setHeldItem(Hand.MAIN_HAND, getStack());
        return fakePlayer;
    }
}
