package me.desht.pneumaticcraft.common.entity.projectile;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

/**
 * A bit like an EntityFallingBlock but tumbles as it flies, and tries to form a block on impact with any other
 * block, not just when it lands on top of another block.
 */
public class EntityTumblingBlock extends EntityThrowable {
    private static final DataParameter<BlockPos> ORIGIN = EntityDataManager.createKey(EntityTumblingBlock.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<ItemStack> STATE_STACK = EntityDataManager.createKey(EntityTumblingBlock.class, DataSerializers.ITEM_STACK);
    private static FakePlayer fakePlayer;
    private ItemStack stack = ItemStack.EMPTY;

    public EntityTumblingBlock(World worldIn) {
        super(worldIn);
    }

    public EntityTumblingBlock(World worldIn, double x, double y, double z, @Nonnull ItemStack stack) {
        super(worldIn);

        Validate.isTrue(!stack.isEmpty() && stack.getItem() instanceof ItemBlock);

        this.stack = stack;
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        this.setPosition(x, y + (double)((1.0F - this.height) / 2.0F), z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.setOrigin(new BlockPos(this));
    }

    @Override
    protected void entityInit() {
        dataManager.register(ORIGIN, BlockPos.ORIGIN);
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
        return stack;
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getOrigin()
    {
        return this.dataManager.get(ORIGIN);
    }

    private void setOrigin(BlockPos pos) {
        dataManager.set(ORIGIN, pos);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (world.isRemote) {
            if (key == STATE_STACK) {
                stack = dataManager.get(STATE_STACK);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (ticksExisted == 1) {
            dataManager.set(STATE_STACK, stack);
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();  // handles nearly all of the in-flight logic

        if (!world.isRemote) {
            BlockPos blockpos1 = new BlockPos(this);
            if (!onGround && (ticksExisted > 100 && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || ticksExisted > 600)) {
                dropAsItem();
                setDead();
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                setDead();
                if (!tryPlaceAsBlock(result.getBlockPos(), result.sideHit)) {
                    dropAsItem();
                }
            }
        }
    }

    private boolean tryPlaceAsBlock(BlockPos pos0, EnumFacing side) {
        Block b = world.getBlockState(pos0).getBlock();
        BlockPos pos = b.isReplaceable(world, pos0) ? pos0 : pos0.offset(side);
        if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            EntityPlayer placer = thrower instanceof EntityPlayer ? (EntityPlayer) thrower : getFakePlayer();
            IBlockState newState = block.getStateForPlacement(world, pos, side, 0f, 0f, 0f, stack.getMetadata(), placer, EnumHand.MAIN_HAND);
            return world.setBlockState(pos, newState);
        }
        return false;
    }

    private void dropAsItem() {
        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
            entityDropItem(stack.copy(), 0.0F);
        }
    }

    private EntityPlayer getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get((WorldServer) world, new GameProfile(null, "[Tumbling Block]"));
            fakePlayer.connection = new FakeNetHandlerPlayerServer(FMLCommonHandler.instance().getMinecraftServerInstance(), fakePlayer);
        }
        fakePlayer.posX = posX;
        fakePlayer.posY = posY;
        fakePlayer.posZ = posZ;
        return fakePlayer;
    }
}
