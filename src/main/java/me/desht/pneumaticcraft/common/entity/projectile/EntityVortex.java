package me.desht.pneumaticcraft.common.entity.projectile;

import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

public class EntityVortex extends ThrowableEntity {
    private int hitCounter = 0;

    // clientside: rendering X offset of vortex, depends on which hand the vortex was fired from
    private float renderOffsetX = -Float.MAX_VALUE;

    public EntityVortex(World world, LivingEntity thrower) {
        super(ModEntities.VORTEX, thrower, world);
    }

    public EntityVortex(World world) {
        super(ModEntities.VORTEX, world);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static EntityVortex create(EntityType<Entity> type, World world) {
        return new EntityVortex(world);
    }

    @Override
    public void tick() {
        super.tick();
        setMotion(getMotion().scale(0.95));
        if (getMotion().lengthSquared() < 0.1D) {
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
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IPlantable || block instanceof LeavesBlock) {
            world.destroyBlock(pos, true);
            return true;
        }
        return false;
    }

    @Override
    public float getGravityVelocity() {
        return 0;
    }

    @Override
    protected void onImpact(RayTraceResult rtr) {
        if (rtr.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) rtr).getEntity();
            entity.setMotion(entity.getMotion().add(this.getMotion()));
            if (!entity.world.isRemote && entity instanceof IShearable) {
                IShearable shearable = (IShearable) entity;
                BlockPos pos = new BlockPos(posX, posY, posZ);
                if (shearable.isShearable(ItemStack.EMPTY, world, pos)) {
                    List<ItemStack> drops = shearable.onSheared(ItemStack.EMPTY, world, pos, 0);
                    for (ItemStack stack : drops) {
                        PneumaticCraftUtils.dropItemOnGround(stack, world, entity.posX, entity.posY, entity.posZ);
                    }
                }
            }
        } else if (rtr.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) rtr).getPos();
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof IPlantable || block instanceof LeavesBlock) {
                if (!world.isRemote) {
                    BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(pos);
                    if (tryCutPlants(pos)) {
                        int plantsCut = 1;
                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (x == 0 && y == 0 && z == 0) continue;
                                    mPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                                    if (tryCutPlants(mPos)) plantsCut++;
                                }
                            }
                        }
                        // slow the vortex down for each plant it broke
                        double mult = Math.pow(0.8D, plantsCut);
                        setMotion(getMotion().scale(mult));
                    }
                }
            } else {
                remove();
            }
        }
        hitCounter++;
        if (hitCounter > 20) remove();
    }

    @Override
    protected void registerData() {

    }
}
